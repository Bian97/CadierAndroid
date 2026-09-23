package app.convencao.cadier.util.Enums;

// Espelha Cadier.Model.Enums.TipoDocumentoPFisicaEnum (backend novo) - os 6 documentos exigidos na
// filiação. A ordem dos values() é a mesma ordem de exibição usada no site (useTiposDocumento).
public enum TipoDocumentoEnum {
    IdentidadeComCpfFrente(1),
    ComprovanteResidencia(2),
    Foto3x4(3),
    CertidaoCasamentoOuNascimento(4),
    ComprovanteCargo(5),
    IdentidadeComCpfVerso(6);

    private final int id;

    TipoDocumentoEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TipoDocumentoEnum fromInteger(int x) {
        for (TipoDocumentoEnum t : values()) {
            if (t.id == x) return t;
        }
        return null;
    }

    public String getLabelPtBr() {
        switch (this) {
            case IdentidadeComCpfFrente: return "Identidade com CPF (Frente)";
            case ComprovanteResidencia: return "Comprovante de Residência";
            case Foto3x4: return "Foto 3x4";
            case CertidaoCasamentoOuNascimento: return "Certidão de Casamento ou Nascimento";
            case ComprovanteCargo: return "Comprovante de Cargo";
            case IdentidadeComCpfVerso: return "Identidade com CPF (Verso)";
            default: return toString();
        }
    }
}
