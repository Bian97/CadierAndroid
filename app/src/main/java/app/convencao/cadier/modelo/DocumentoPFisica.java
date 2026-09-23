package app.convencao.cadier.modelo;

import app.convencao.cadier.util.Enums.StatusDocumentoEnum;
import app.convencao.cadier.util.Enums.TipoDocumentoEnum;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Espelha PegarDocumentoPFisicaViewModel (Cadier.API) - resposta de
 * GET DocumentoPFisica/PorFiliado/{idPfi}. O backend sempre devolve os 6 tipos fixos, mesmo os
 * ainda não enviados (com idDocumento=0 e status sintético Pendente/NaoDigitalizado).
 */
public class DocumentoPFisica implements Serializable {
    private int idDocumento;
    private int idPessoaFisica;
    private TipoDocumentoEnum tipoDocumento;
    private StatusDocumentoEnum status;
    private String nomeArquivoOriginal;
    private Date dataEnvio;
    private Date dataRevisao;
    private String motivoRejeicao;

    public int getIdDocumento() {
        return idDocumento;
    }

    public int getIdPessoaFisica() {
        return idPessoaFisica;
    }

    public TipoDocumentoEnum getTipoDocumento() {
        return tipoDocumento;
    }

    public StatusDocumentoEnum getStatus() {
        return status;
    }

    public String getNomeArquivoOriginal() {
        return nomeArquivoOriginal;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }

    public Date getDataRevisao() {
        return dataRevisao;
    }

    public String getMotivoRejeicao() {
        return motivoRejeicao;
    }

    public static DocumentoPFisica fromJson(JSONObject json) {
        DocumentoPFisica d = new DocumentoPFisica();
        d.idDocumento = json.optInt("idDocumento", 0);
        d.idPessoaFisica = json.optInt("idPessoaFisica", 0);
        d.tipoDocumento = TipoDocumentoEnum.fromInteger(json.optInt("tipoDocumento"));
        d.status = StatusDocumentoEnum.fromInteger(json.optInt("status"));
        d.nomeArquivoOriginal = json.isNull("nomeArquivoOriginal") ? null : json.optString("nomeArquivoOriginal", null);
        d.dataEnvio = parseDataOuNull(json.optString("dataEnvio", null));
        d.dataRevisao = parseDataOuNull(json.optString("dataRevisao", null));
        d.motivoRejeicao = json.isNull("motivoRejeicao") ? null : json.optString("motivoRejeicao", null);
        return d;
    }

    /** Cria o slot sintético "Pendente" pra um tipo que não veio na resposta da API. */
    public static DocumentoPFisica pendenteSintetico(TipoDocumentoEnum tipo) {
        DocumentoPFisica d = new DocumentoPFisica();
        d.tipoDocumento = tipo;
        d.status = StatusDocumentoEnum.Pendente;
        return d;
    }

    private static Date parseDataOuNull(String texto) {
        if (texto == null || texto.isEmpty()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(texto);
        } catch (Exception e) {
            return null;
        }
    }
}
