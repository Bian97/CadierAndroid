package app.convencao.cadier.util.Enums;

// Espelha Cadier.Model.Enums.StatusDocumentoEnum (backend novo).
public enum StatusDocumentoEnum {
    Pendente(0),
    Enviado(1),
    Aprovado(2),
    Rejeitado(3),
    NaoDigitalizado(4);

    private final int id;

    StatusDocumentoEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static StatusDocumentoEnum fromInteger(int x) {
        for (StatusDocumentoEnum s : values()) {
            if (s.id == x) return s;
        }
        return Pendente; // o backend sempre manda um dos 5 valores - nunca deveria cair aqui
    }

    public String getLabelPtBr() {
        switch (this) {
            case Pendente: return "Pendente";
            case Enviado: return "Enviado";
            case Aprovado: return "Aprovado";
            case Rejeitado: return "Rejeitado";
            case NaoDigitalizado: return "Não Digitalizado";
            default: return toString();
        }
    }
}
