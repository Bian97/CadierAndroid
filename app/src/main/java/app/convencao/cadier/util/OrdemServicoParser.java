package app.convencao.cadier.util;

import app.convencao.cadier.modelo.ServiceOrder;
import app.convencao.cadier.util.Enums.ServiceKindEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;

/**
 * Converte um objeto do array devolvido por OrdemServico/PorPessoaFisica/{id} (backend novo, ver
 * PegarOrdemServicoViewModel) para o modelo ServiceOrder que as telas de pedidos (TabScheduled,
 * TabPrevious, FragmentMonthly) já usavam - usado nos 3 lugares pra não repetir esse mapeamento.
 * "tipoServico" e "atendente" agora vêm como objeto aninhado (não mais como int/id cru), e
 * "creditoAnterior"/"deposito" continuam existindo no backend novo só por compatibilidade (hoje
 * sempre 0).
 */
public class OrdemServicoParser {
    public static ServiceOrder paraServiceOrder(JSONObject pedido) throws JSONException {
        int idTipoServico = pedido.isNull("tipoServico") ? -1 : pedido.getJSONObject("tipoServico").optInt("idTipoServico", -1);

        return new ServiceOrder(
                pedido.optInt("idOrdem"),
                pedido.optInt("idPessoaFisica"),
                pedido.isNull("atendente") ? 0 : pedido.getJSONObject("atendente").optInt("idAtendente"),
                pedido.optString("servico", ""),
                pedido.isNull("obs") ? null : pedido.optString("obs"),
                parseDataOuNull(pedido.optString("dataPedido", null)),
                parseDataOuNull(pedido.optString("dataFeito", null)),
                parseDataOuNull(pedido.optString("dataEntregue", null)),
                pedido.isNull("quemLevou") ? null : pedido.optString("quemLevou"),
                (float) pedido.optDouble("valor", 0),
                (float) pedido.optDouble("pago", 0),
                (float) pedido.optDouble("creditoAnterior", 0),
                (float) pedido.optDouble("deposito", 0),
                ServiceKindEnum.fromInteger(idTipoServico),
                parseDataOuNull(pedido.optString("mensalidade", null))
        );
    }

    private static Date parseDataOuNull(String iso) {
        if (iso == null || iso.isEmpty() || iso.equalsIgnoreCase("null")) return null;
        try {
            // Datas do backend novo vêm em ISO 8601 (ex: "2020-05-10T00:00:00") - Date.valueOf só
            // entende "yyyy-MM-dd".
            return Date.valueOf(iso.length() >= 10 ? iso.substring(0, 10) : iso);
        } catch (Exception e) {
            return null;
        }
    }
}
