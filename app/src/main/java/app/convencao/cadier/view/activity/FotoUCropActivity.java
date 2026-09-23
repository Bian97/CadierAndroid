package app.convencao.cadier.view.activity;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.yalantis.ucrop.UCropActivity;

import app.convencao.cadier.R;
import app.convencao.cadier.util.InsetsUtil;

/**
 * UCropActivity da lib não trata as barras do sistema do Android 15+ (edge-to-edge) - a barra de
 * topo do recorte ficava por baixo da barra de status. Essa subclasse só aplica a margem das
 * barras em volta da tela; o fundo da janela em teal faz a área da barra de status combinar com a
 * barra de ferramentas do recorte (ver options.setToolbarColor em FotoCropActivity).
 */
public class FotoUCropActivity extends UCropActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.cadier_teal));
        InsetsUtil.aplicar(this, 0);
    }
}
