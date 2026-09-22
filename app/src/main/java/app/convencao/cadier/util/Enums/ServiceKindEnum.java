package app.convencao.cadier.util.Enums;

public enum ServiceKindEnum {
    Mensalidade, Outros;

    // Backend novo (Cadier.Model.Enums.TipoServicoEnum) tem muito mais tipos de serviço que o
    // antigo (Loja inteira: credenciais, cursos, certificados etc.) - aqui só distinguimos
    // Mensalidade (id 2) do resto, mesma simplificação binária que o app já fazia. Nunca retorna
    // null (diferente da versão antiga) porque as telas chamam ".toString()" direto em cima do
    // retorno - um id desconhecido virando Outros evita crash.
    private static final int ID_TIPO_SERVICO_MENSALIDADE = 2;

    public static ServiceKindEnum fromInteger(int x) {
        return x == ID_TIPO_SERVICO_MENSALIDADE ? Mensalidade : Outros;
    }
}
