package app.convencao.cadier.util.Enums;

// Espelha Cadier.Model.Enums.CondicaoEnum (backend novo) - os ids antigos (0-3, 5) continuavam
// valendo, só "Pendencia" (id 4) foi descontinuado no backend (nunca mais é enviado) e 3 valores
// novos foram acrescentados (Falecido, AguardandoAprovacaoDocumentos, Inadimplente).
public enum StatusEnum {
    Ativo, Inativo, ProcessoInterno, Desligado, Excluido, Falecido, AguardandoAprovacaoDocumentos, Inadimplente;

    public static StatusEnum fromInteger(int x) {
        switch(x) {
            case 0:
                return Ativo;
            case 1:
                return Inativo;
            case 2:
                return ProcessoInterno;
            case 3:
                return Desligado;
            case 5:
                return Excluido;
            case 6:
                return Falecido;
            case 7:
                return AguardandoAprovacaoDocumentos;
            case 8:
                return Inadimplente;
        }
        return null;
    }
}