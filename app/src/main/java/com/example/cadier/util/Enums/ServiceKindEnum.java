package com.example.cadier.util.Enums;

public enum ServiceKindEnum {
    Mensalidade, Outros;

    public static ServiceKindEnum fromInteger(int x) {
        switch(x) {
            case 0:
                return Mensalidade;
            case 1:
                return Outros;
        }
        return null;
    }
}
