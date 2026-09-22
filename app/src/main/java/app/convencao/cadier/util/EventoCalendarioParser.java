package app.convencao.cadier.util;

import app.convencao.cadier.modelo.OcorrenciaEventoCalendario;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Converte um JSONObject vindo de EventoCalendario/Ocorrencias ou EventoCalendario/MinhasAulas
 * (OcorrenciaEventoCalendarioDto do backend novo, JSON em camelCase) para o modelo de domínio.
 */
public class EventoCalendarioParser {

    public static OcorrenciaEventoCalendario paraOcorrencia(JSONObject ocorrencia) {
        return new OcorrenciaEventoCalendario(
                ocorrencia.optInt("idEvento"),
                ocorrencia.optString("titulo", ""),
                ocorrencia.isNull("local") ? null : ocorrencia.optString("local"),
                ocorrencia.isNull("descricao") ? null : ocorrencia.optString("descricao"),
                parseDataHoraOuNull(ocorrencia.optString("dataHoraOcorrencia", null)),
                parseDataHoraOuNull(ocorrencia.optString("dataHoraFimOcorrencia", null)),
                ocorrencia.isNull("nomeCurso") ? null : ocorrencia.optString("nomeCurso"),
                ocorrencia.isNull("linkReuniao") ? null : ocorrencia.optString("linkReuniao")
        );
    }

    private static Date parseDataHoraOuNull(String iso) {
        if (iso == null || iso.isEmpty() || iso.equalsIgnoreCase("null")) return null;
        try {
            // Datas do backend novo vêm em ISO 8601 (ex: "2026-09-22T14:00:00"), possivelmente com
            // frações de segundo - usamos só os 19 primeiros caracteres (data + hora até o segundo).
            String semFracao = iso.length() >= 19 ? iso.substring(0, 19) : iso;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return sdf.parse(semFracao);
        } catch (Exception e) {
            return null;
        }
    }
}
