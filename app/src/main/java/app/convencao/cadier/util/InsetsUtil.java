package app.convencao.cadier.util;

import android.app.Activity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.R;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Com targetSdk 35+, o Android 15+ desenha o app por baixo da barra de status e da barra de
 * navegação (edge-to-edge) - telas com barra de topo própria (sem ActionBar/Toolbar do sistema)
 * ficavam com o botão "Voltar" embaixo da barra de status, e botões no fim da tela embaixo da barra
 * de navegação. Aqui a barra de topo é esticada pra cobrir a barra de status, e o resto do conteúdo
 * ganha margem em volta (incluindo o teclado, que no edge-to-edge não redimensiona mais a janela).
 */
public final class InsetsUtil {
    private InsetsUtil() {
    }

    /** idBarraTopo = id da barra teal própria da tela, ou 0 se a tela não tiver uma. */
    public static void aplicar(Activity activity, int idBarraTopo) {
        View raiz = activity.findViewById(android.R.id.content);
        View barra = idBarraTopo != 0 ? activity.findViewById(idBarraTopo) : null;

        int alturaBarra = 0;
        if (barra != null) {
            TypedValue tv = new TypedValue();
            if (activity.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                alturaBarra = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
            }
        }
        final int alturaBase = alturaBarra;

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets sistema = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(sistema.left, barra != null ? 0 : sistema.top, sistema.right, sistema.bottom);
            if (barra != null) {
                ViewGroup.LayoutParams lp = barra.getLayoutParams();
                lp.height = alturaBase + sistema.top;
                barra.setLayoutParams(lp);
                barra.setPadding(0, sistema.top, 0, 0);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(raiz);
    }
}
