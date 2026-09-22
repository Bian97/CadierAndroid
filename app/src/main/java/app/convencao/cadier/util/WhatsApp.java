package app.convencao.cadier.util;

import android.content.ActivityNotFoundException;
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
    private static final String PACOTE_WHATSAPP = "com.whatsapp";
    private static final String PACOTE_WHATSAPP_BUSINESS = "com.whatsapp.w4b";

    /**
     * Abre o app do WhatsApp direto (não o navegador) tentando primeiro o WhatsApp comum, depois
     * o WhatsApp Business, e só cai pro link https://wa.me (que decide sozinho pra onde ir) se
     * nenhum dos dois estiver instalado.
     */
    public static void abrirChatSecretaria(Context context, String mensagem) {
        String texto = (mensagem != null && !mensagem.isEmpty()) ? "?text=" + Uri.encode(mensagem) : "";
        Uri uri = Uri.parse("whatsapp://send?phone=" + NUMERO_SECRETARIA + texto);

        if (abrirComPacote(context, uri, PACOTE_WHATSAPP)) return;
        if (abrirComPacote(context, uri, PACOTE_WHATSAPP_BUSINESS)) return;

        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + NUMERO_SECRETARIA + texto)));
    }

    private static boolean abrirComPacote(Context context, Uri uri, String pacote) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(pacote);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
