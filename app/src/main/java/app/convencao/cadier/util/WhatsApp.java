package app.convencao.cadier.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Canal de contato com a Secretaria da CADIER - não existe chat em tempo real no sistema; esse é
 * o canal oficial já usado em todo o site React (BotaoFlutuanteWhatsApp.js/FaleConosco.js,
 * NUMERO_WHATSAPP = "5521965598833"). Mantemos o mesmo número aqui pra consistência.
 */
public class WhatsApp {
    private static final String NUMERO_SECRETARIA = "5521965598833";

    public static void abrirChatSecretaria(Context context, String mensagem) {
        String url = "https://wa.me/" + NUMERO_SECRETARIA;
        if (mensagem != null && !mensagem.isEmpty()) {
            url += "?text=" + Uri.encode(mensagem);
        }
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
