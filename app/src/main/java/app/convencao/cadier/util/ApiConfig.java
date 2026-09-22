package app.convencao.cadier.util;

import app.convencao.cadier.BuildConfig;

/**
 * URL base do backend novo (Cadier.API, .NET) - substitui o antigo sistema PHP em
 * cadier.com.br/WS e cadier.com.br/api/*.php. O Caddy da VM remove o prefixo "/api" antes de
 * encaminhar pro .NET (mesmo padrão usado pelo front-end React, VITE_API_URL=.../api), então
 * toda rota de controller (ex: "autenticacao/login") é chamada aqui como
 * BASE_URL + "autenticacao/login".
 */
public class ApiConfig {
    public static final String BASE_URL = "https://cadier.com.br/api/";

    // Toda rota do backend novo exige "x-api-key" (ver Cadier.Core/ApiKeyAuthenticationHandler.cs) -
    // sem ele, até o login é rejeitado com 401 antes de chegar no controller, já que
    // AutenticacaoController aceita autenticação por JwtBearer OU ApiKey, e antes de logar não existe
    // JWT ainda. Mesma chave estática (única, não por cliente) que o site React já usa em
    // apiClient.js (VITE_API_KEY). Vem de local.properties via BuildConfig (ver app/build.gradle) -
    // não fica hardcoded aqui pra não commitar o segredo no repositório.
    public static final String API_KEY = BuildConfig.API_KEY;
}
