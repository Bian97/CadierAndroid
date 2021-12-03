package app.convencao.cadier.util.Enums;

public enum StatusEnum {
    Ativo, Inativo, ProcessoInterno, Desligado, Pendencia, Excluido;

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
            case 4:
                return Pendencia;
            case 5:
                return Excluido;
        }
        return null;
    }
}