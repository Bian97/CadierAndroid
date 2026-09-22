package app.convencao.cadier.modelo;

import java.io.Serializable;
import java.util.Date;

/**
 * Uma ocorrência de evento do calendário do backend novo (Cadier.API) - tanto eventos
 * institucionais (nomeCurso nulo, vindos de EventoCalendario/Ocorrencias) quanto aulas de curso
 * (nomeCurso preenchido, vindos de EventoCalendario/MinhasAulas, já filtradas pelo backend pelas
 * matrículas da pessoa logada).
 */
public class OcorrenciaEventoCalendario implements Serializable {
    private int idEvento;
    private String titulo;
    private String local;
    private String descricao;
    private Date dataHoraOcorrencia;
    private Date dataHoraFimOcorrencia;
    private String nomeCurso;
    private String linkReuniao;

    public OcorrenciaEventoCalendario(int idEvento, String titulo, String local, String descricao,
                                       Date dataHoraOcorrencia, Date dataHoraFimOcorrencia,
                                       String nomeCurso, String linkReuniao) {
        this.idEvento = idEvento;
        this.titulo = titulo;
        this.local = local;
        this.descricao = descricao;
        this.dataHoraOcorrencia = dataHoraOcorrencia;
        this.dataHoraFimOcorrencia = dataHoraFimOcorrencia;
        this.nomeCurso = nomeCurso;
        this.linkReuniao = linkReuniao;
    }

    public int getIdEvento() {
        return idEvento;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getLocal() {
        return local;
    }

    public String getDescricao() {
        return descricao;
    }

    public Date getDataHoraOcorrencia() {
        return dataHoraOcorrencia;
    }

    public Date getDataHoraFimOcorrencia() {
        return dataHoraFimOcorrencia;
    }

    public String getNomeCurso() {
        return nomeCurso;
    }

    public boolean isAula() {
        return nomeCurso != null && !nomeCurso.isEmpty();
    }

    public String getLinkReuniao() {
        return linkReuniao;
    }
}
