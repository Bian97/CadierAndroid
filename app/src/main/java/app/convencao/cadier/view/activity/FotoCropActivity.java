package app.convencao.cadier.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.yalantis.ucrop.UCrop;

import app.convencao.cadier.R;
import app.convencao.cadier.util.ImagemAjusteUtil;
import app.convencao.cadier.util.InsetsUtil;
import app.convencao.cadier.util.ImagemAjusteUtil.Ajustes;

import java.io.File;
import java.io.IOException;

/**
 * Tela compartilhada de recorte + melhoria da Foto 3x4 - usada tanto por FragmentDocumentos quanto
 * por ProfileEditActivity (ver EXTRA_IMAGEM_URI/EXTRA_CAMINHO_FOTO_FINAL). O recorte em si é feito
 * pela uCrop (aspecto 3:4 fixo); depois disso, essa Activity assume pra aplicar a mesma lógica de
 * melhoria de imagem já aprovada no site (ver ImagemAjusteUtil), sem depender de nenhuma lib nova
 * pra isso - só android.graphics puro.
 */
public class FotoCropActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGEM_URI = "extra_imagem_uri";
    public static final String EXTRA_CAMINHO_FOTO_FINAL = "extra_caminho_foto_final";

    private static final int ASPECT_X = 3;
    private static final int ASPECT_Y = 4;

    private Bitmap bitmapRecortado;   // resultado do crop, em resolução plena - nunca mexido diretamente
    private Bitmap bitmapPreviaBase;  // cópia reduzida, só pra prévia rápida dos sliders
    private final Ajustes ajustes = new Ajustes();

    private ImageView imageViewPrevia;
    private SeekBar seekBarBrilho, seekBarContraste, seekBarCores, seekBarNitidez;
    private TextView textViewLabelBrilho, textViewLabelContraste, textViewLabelCores, textViewLabelNitidez;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Uri imagemOriginal = getIntent().getParcelableExtra(EXTRA_IMAGEM_URI);
            if (imagemOriginal == null) {
                finish();
                return;
            }
            iniciarCrop(imagemOriginal);
        }
    }

    private void iniciarCrop(Uri imagemOriginal) {
        try {
            File arquivoDestino = new File(getCacheDir(), "crop_" + System.currentTimeMillis() + ".jpg");
            // uCrop (BitmapLoadTask.copyFile) escreve o resultado com "new File(uri.getPath())" -
            // com um Uri content:// do FileProvider isso vira o caminho literal do segmento da URI
            // (ex.: "/cache/arquivo.jpg", raiz do sistema, sem permissão -> EACCES). Precisa ser um
            // Uri file:// de verdade para bater com o path real. Isso não dispara
            // FileUriExposedException porque o Uri viaja como extra do Intent (Parcelable comum),
            // não via Intent.setData()/setDataAndType() - só esses dois é que o StrictMode verifica.
            Uri destinoCrop = Uri.fromFile(arquivoDestino);

            UCrop.Options options = new UCrop.Options();
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
            options.setCompressionQuality(100); // sem perda extra aqui - a compressão final acontece em ImagemAjusteUtil
            options.setFreeStyleCropEnabled(false); // aspecto 3:4 travado, igual ao site
            options.setHideBottomControls(false);
            options.setToolbarColor(ContextCompat.getColor(this, R.color.cadier_teal));
            options.setStatusBarColor(ContextCompat.getColor(this, R.color.cadier_teal_dark));
            options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.cadier_text_on_dark));

            // getIntent() + setClass em vez de start(): troca pra FotoUCropActivity, que trata as
            // barras do sistema do Android 15+ (ver comentário lá).
            Intent intentCrop = UCrop.of(imagemOriginal, destinoCrop)
                    .withAspectRatio(ASPECT_X, ASPECT_Y)
                    .withMaxResultSize(1500, 2000)
                    .withOptions(options)
                    .getIntent(this);
            intentCrop.setClass(this, FotoUCropActivity.class);
            startActivityForResult(intentCrop, UCrop.REQUEST_CROP);
        } catch (Exception e) {
            // Sem isso, qualquer falha ao montar/lançar o crop (Uri sem permissão, FileProvider mal
            // configurado, etc.) fechava a tela silenciosamente sem dizer o motivo.
            e.printStackTrace();
            Toast.makeText(this, "Não foi possível abrir o recorte: " + e.getMessage(), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != UCrop.REQUEST_CROP) return;

        if (resultCode == RESULT_OK && data != null) {
            Uri resultadoCrop = UCrop.getOutput(data);
            if (resultadoCrop == null) {
                Toast.makeText(this, "Não foi possível recortar a foto.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            try {
                bitmapRecortado = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultadoCrop));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bitmapRecortado == null) {
                Toast.makeText(this, "Não foi possível carregar a foto recortada.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            mostrarTelaDeAjustes();
        } else {
            if (resultCode == UCrop.RESULT_ERROR && data != null) {
                Throwable erro = UCrop.getError(data);
                if (erro != null) {
                    erro.printStackTrace();
                    Toast.makeText(this, "Erro ao recortar a foto: " + erro.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void mostrarTelaDeAjustes() {
        setContentView(R.layout.activity_foto_crop_ajustes);
        InsetsUtil.aplicar(this, R.id.barraTopo);

        int larguraPrevia = Math.min(bitmapRecortado.getWidth(), 600);
        int alturaPrevia = Math.round(larguraPrevia * (float) bitmapRecortado.getHeight() / bitmapRecortado.getWidth());
        bitmapPreviaBase = Bitmap.createScaledBitmap(bitmapRecortado, larguraPrevia, alturaPrevia, true);

        TextView textViewVoltar = findViewById(R.id.textViewVoltarAjusteFoto);
        imageViewPrevia = findViewById(R.id.imageViewPreviaAjuste);
        textViewLabelBrilho = findViewById(R.id.textViewLabelBrilho);
        textViewLabelContraste = findViewById(R.id.textViewLabelContraste);
        textViewLabelCores = findViewById(R.id.textViewLabelCores);
        textViewLabelNitidez = findViewById(R.id.textViewLabelNitidez);
        seekBarBrilho = findViewById(R.id.seekBarBrilho);
        seekBarContraste = findViewById(R.id.seekBarContraste);
        seekBarCores = findViewById(R.id.seekBarCores);
        seekBarNitidez = findViewById(R.id.seekBarNitidez);
        Button buttonMelhorarAuto = findViewById(R.id.buttonMelhorarFotoAuto);
        Button buttonDesfazer = findViewById(R.id.buttonDesfazerMelhoria);
        Button buttonConfirmar = findViewById(R.id.buttonConfirmarFoto);

        textViewVoltar.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        configurarSliders();
        aplicarAjustesNasBarras();
        atualizarPrevia();

        buttonMelhorarAuto.setOnClickListener(v -> {
            Ajustes sugestao = ImagemAjusteUtil.sugerirAjustesAutomaticos(bitmapPreviaBase);
            ajustes.brilho = sugestao.brilho;
            ajustes.contraste = sugestao.contraste;
            ajustes.cores = sugestao.cores;
            ajustes.nitidez = sugestao.nitidez;
            aplicarAjustesNasBarras();
            atualizarPrevia();
        });

        buttonDesfazer.setOnClickListener(v -> {
            ajustes.brilho = 100;
            ajustes.contraste = 100;
            ajustes.cores = 100;
            ajustes.nitidez = 0;
            aplicarAjustesNasBarras();
            atualizarPrevia();
        });

        buttonConfirmar.setOnClickListener(v -> new GerarFotoFinalTask().execute());
    }

    /** Sincroniza as 4 SeekBars (cada uma com sua própria escala) com os valores atuais de `ajustes`. */
    private void aplicarAjustesNasBarras() {
        seekBarBrilho.setProgress(ajustes.brilho - 50);       // 50-150 -> progress 0-100
        seekBarContraste.setProgress(ajustes.contraste - 50); // 50-150 -> progress 0-100
        seekBarCores.setProgress(ajustes.cores);              // 0-200 -> progress 0-200
        seekBarNitidez.setProgress(ajustes.nitidez);          // 0-100 -> progress 0-100
        atualizarLabels();
    }

    private void atualizarLabels() {
        textViewLabelBrilho.setText("Brilho: " + ajustes.brilho + "%");
        textViewLabelContraste.setText("Contraste: " + ajustes.contraste + "%");
        textViewLabelCores.setText("Cores: " + ajustes.cores + "%");
        textViewLabelNitidez.setText("Nitidez: " + ajustes.nitidez + "%");
    }

    private void configurarSliders() {
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                int id = seekBar.getId();
                if (id == R.id.seekBarBrilho) ajustes.brilho = progress + 50;
                else if (id == R.id.seekBarContraste) ajustes.contraste = progress + 50;
                else if (id == R.id.seekBarCores) ajustes.cores = progress;
                else if (id == R.id.seekBarNitidez) ajustes.nitidez = progress;
                atualizarLabels();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Reprocessa a prévia só ao soltar o dedo - reprocessar a cada pixel arrastado
                // deixaria o slider engasgado.
                atualizarPrevia();
            }
        };
        seekBarBrilho.setOnSeekBarChangeListener(listener);
        seekBarContraste.setOnSeekBarChangeListener(listener);
        seekBarCores.setOnSeekBarChangeListener(listener);
        seekBarNitidez.setOnSeekBarChangeListener(listener);
    }

    private void atualizarPrevia() {
        Bitmap previa = ImagemAjusteUtil.aplicarTom(bitmapPreviaBase, ajustes.brilho, ajustes.contraste, ajustes.cores);
        previa = ImagemAjusteUtil.aplicarNitidez(previa, ajustes.nitidez);
        imageViewPrevia.setImageBitmap(previa);
    }

    private class GerarFotoFinalTask extends AsyncTask<Void, Void, File> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(FotoCropActivity.this, "Processando", "Aguarde um instante...", false, false);
        }

        @Override
        protected File doInBackground(Void... voids) {
            try {
                return ImagemAjusteUtil.gerarArquivoFinal(FotoCropActivity.this, bitmapRecortado, ajustes);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(File arquivoFinal) {
            super.onPostExecute(arquivoFinal);
            progressDialog.dismiss();
            if (arquivoFinal == null) {
                Toast.makeText(FotoCropActivity.this, "Não foi possível processar a foto.", Toast.LENGTH_SHORT).show();
                return;
            }
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_CAMINHO_FOTO_FINAL, arquivoFinal.getAbsolutePath()));
            finish();
        }
    }
}
