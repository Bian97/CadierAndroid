package app.convencao.cadier.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Porta pro Android do algoritmo de recorte+melhoria de foto já aprovado no site
 * (Site/cadierSite/src/util/cropImage.js, usado por CropperFoto3x4.js). Mesma ordem de aplicação
 * (brilho -> contraste -> saturação -> nitidez), mesmas fórmulas e mesmos ranges, pra que o
 * resultado da Foto 3x4 enviada pelo app fique visualmente equivalente ao enviado pelo site.
 */
public class ImagemAjusteUtil {

    /** Ajustes de imagem - 100/100/100/0 é o "padrão neutro" (equivalente a AJUSTES_PADRAO no JS). */
    public static class Ajustes implements Serializable {
        public int brilho = 100;   // 50-150
        public int contraste = 100; // 50-150
        public int cores = 100;     // 0-200 (saturação)
        public int nitidez = 0;     // 0-100

        public boolean ehPadrao() {
            return brilho == 100 && contraste == 100 && cores == 100 && nitidez == 0;
        }
    }

    private ImagemAjusteUtil() {
    }

    // --- Brilho / Contraste / Saturação via ColorMatrix ---------------------------------------

    // Brilho é multiplicativo, igual ao JS (r = r/255 * b).
    private static ColorMatrix matrizBrilho(int brilhoPct) {
        float b = brilhoPct / 100f;
        return new ColorMatrix(new float[]{
                b, 0, 0, 0, 0,
                0, b, 0, 0, 0,
                0, 0, b, 0, 0,
                0, 0, 0, 1, 0
        });
    }

    // Contraste: r = r*k + (0.5 - 0.5k), reescrito em escala 0-255 -> offset = 127.5*(1-k).
    private static ColorMatrix matrizContraste(int contrastePct) {
        float k = contrastePct / 100f;
        float offset = 127.5f * (1 - k);
        return new ColorMatrix(new float[]{
                k, 0, 0, 0, offset,
                0, k, 0, 0, offset,
                0, 0, k, 0, offset,
                0, 0, 0, 1, 0
        });
    }

    // ColorMatrix.setSaturation() usa os mesmos coeficientes de luminância (0.213/0.715/0.072) que
    // a matriz de saturação do cropImage.js - não precisa reescrever a matriz na mão.
    private static ColorMatrix matrizSaturacao(int coresPct) {
        ColorMatrix m = new ColorMatrix();
        m.setSaturation(coresPct / 100f);
        return m;
    }

    private static Bitmap desenharComFiltro(Bitmap origem, ColorMatrix matriz) {
        Bitmap saida = Bitmap.createBitmap(origem.getWidth(), origem.getHeight(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(new ColorMatrixColorFilter(matriz));
        new Canvas(saida).drawBitmap(origem, 0, 0, paint);
        return saida; // escrita em bitmap 8-bit já clampa 0-255, como o JS faz manualmente
    }

    /** Aplica brilho -> contraste -> saturação, nessa ordem, exatamente como o site. */
    public static Bitmap aplicarTom(Bitmap origem, int brilho, int contraste, int cores) {
        if (brilho == 100 && contraste == 100 && cores == 100) return origem;
        Bitmap e1 = desenharComFiltro(origem, matrizBrilho(brilho));
        Bitmap e2 = desenharComFiltro(e1, matrizContraste(contraste));
        return desenharComFiltro(e2, matrizSaturacao(cores));
    }

    // --- Nitidez via kernel de convolução em cruz (unsharp mask) -------------------------------

    /**
     * Kernel em cruz (não 3x3 cheio): centro = 1+4a, os 4 vizinhos diretos = -a, onde
     * a = (nitidez/100)*0.6 - mesmo teto propositalmente baixo do site pra evitar halo nas bordas
     * do rosto. Borda da imagem tratada com "clamp to edge" (repete o pixel da borda).
     */
    public static Bitmap aplicarNitidez(Bitmap origem, int nitidezPct) {
        if (nitidezPct <= 0) return origem;
        float a = (nitidezPct / 100f) * 0.6f;
        int w = origem.getWidth(), h = origem.getHeight();
        int[] src = new int[w * h];
        origem.getPixels(src, 0, w, 0, 0, w, h);
        int[] dst = new int[w * h];
        for (int y = 0; y < h; y++) {
            int yUp = (y > 0 ? y - 1 : y) * w;
            int yC = y * w;
            int yDown = (y < h - 1 ? y + 1 : y) * w;
            for (int x = 0; x < w; x++) {
                int xL = x > 0 ? x - 1 : x;
                int xR = x < w - 1 ? x + 1 : x;
                int cen = src[yC + x], up = src[yUp + x], down = src[yDown + x], left = src[yC + xL], right = src[yC + xR];
                int r = canal(cen, 16, up, down, left, right, a);
                int g = canal(cen, 8, up, down, left, right, a);
                int b = canal(cen, 0, up, down, left, right, a);
                dst[yC + x] = (0xFF << 24) | (r << 16) | (g << 8) | b;
            }
        }
        Bitmap saida = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        saida.setPixels(dst, 0, w, 0, 0, w, h);
        return saida;
    }

    private static int canal(int centro, int shift, int cima, int baixo, int esq, int dir, float a) {
        int c = (centro >> shift) & 0xFF;
        int u = (cima >> shift) & 0xFF;
        int d = (baixo >> shift) & 0xFF;
        int e = (esq >> shift) & 0xFF;
        int r = (dir >> shift) & 0xFF;
        float valor = c * (1 + 4 * a) - a * (u + d + e + r);
        return Math.max(0, Math.min(255, Math.round(valor)));
    }

    // --- "Melhorar automaticamente" -----------------------------------------------------------

    /**
     * Mesma heurística do site: reduz a imagem a no máx. 256px, calcula luminância média
     * (0.299R+0.587G+0.114B) e desvio-padrão numa amostra reduzida, e sugere os 4 ajustes.
     */
    public static Ajustes sugerirAjustesAutomaticos(Bitmap origem) {
        float escala = Math.min(1f, 256f / Math.max(origem.getWidth(), origem.getHeight()));
        int w = Math.max(1, Math.round(origem.getWidth() * escala));
        int h = Math.max(1, Math.round(origem.getHeight() * escala));
        Bitmap amostra = Bitmap.createScaledBitmap(origem, w, h, true);
        int[] px = new int[w * h];
        amostra.getPixels(px, 0, w, 0, 0, w, h);

        double soma = 0, somaQuad = 0;
        for (int p : px) {
            double lum = (0.299 * ((p >> 16) & 0xFF) + 0.587 * ((p >> 8) & 0xFF) + 0.114 * (p & 0xFF)) / 255.0;
            soma += lum;
            somaQuad += lum * lum;
        }
        double media = soma / px.length;
        double desvio = Math.sqrt(Math.max(0, somaQuad / px.length - media * media));

        Ajustes ajustes = new Ajustes();
        ajustes.brilho = Math.round((float) Math.max(80, Math.min(135, 100 + (0.5 - media) * 120)));
        ajustes.contraste = desvio < 0.18 ? 120 : 108;
        ajustes.cores = 108;
        ajustes.nitidez = 40;
        return ajustes;
    }

    // --- Geração do arquivo final --------------------------------------------------------------

    /**
     * Recorte já resolvido (crop) -> resize máx. 2000px de altura -> fundo branco -> ajustes ->
     * JPEG qualidade 92 em cache. Mesmo pipeline final do site.
     */
    public static File gerarArquivoFinal(Context contexto, Bitmap recortada, Ajustes ajustes) throws IOException {
        Bitmap redimensionada = recortada.getHeight() > 2000
                ? Bitmap.createScaledBitmap(recortada,
                Math.round(recortada.getWidth() * 2000f / recortada.getHeight()), 2000, true)
                : recortada;

        Bitmap comFundo = Bitmap.createBitmap(redimensionada.getWidth(), redimensionada.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(comFundo);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(redimensionada, 0, 0, null);

        Bitmap resultado = comFundo;
        if (ajustes != null && !ajustes.ehPadrao()) {
            resultado = aplicarTom(resultado, ajustes.brilho, ajustes.contraste, ajustes.cores);
            resultado = aplicarNitidez(resultado, ajustes.nitidez);
        }

        File saida = new File(contexto.getCacheDir(), "foto3x4_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(saida)) {
            resultado.compress(Bitmap.CompressFormat.JPEG, 92, fos);
        }
        return saida;
    }
}
