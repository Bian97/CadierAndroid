package app.convencao.cadier.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by DrGreend on 28/03/2018.
 * Reescrito pra falar com o backend novo (Cadier.API, JSON + JWT) em vez do antigo (PHP,
 * form-urlencoded, sem autenticação). Consolidado em cima do OkHttp (já era dependência, usado
 * em ProfileEditActivity) em vez do HttpURLConnection cru que essa classe usava antes, porque
 * agora toda chamada autenticada precisa do header "Authorization: Bearer <token>".
 */
public class ConectWebService {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private Request.Builder authorized(String url, String token) {
        // "x-api-key" é obrigatório em toda chamada (ver ApiConfig.API_KEY) - inclusive no login,
        // antes de existir token. Depois de logado, o Bearer também vai junto (não custa nada mandar
        // os dois, e evita qualquer chamada esquecida de token que dependeria só da ApiKey).
        Request.Builder builder = new Request.Builder().url(url).header("x-api-key", ApiConfig.API_KEY);
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    /** GET autenticado - devolve o corpo da resposta como String, ou null em erro/falha de rede. */
    public String get(String url, String token) {
        Request request = authorized(url, token).get().build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            String texto = body != null ? body.string() : null;
            if (!response.isSuccessful()) return null;
            return texto;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** GET autenticado que devolve os bytes crus (download de arquivo/imagem), ou null em erro. */
    public byte[] getBytes(String url, String token) {
        Request request = authorized(url, token).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return null;
            return response.body().bytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * POST/PATCH com corpo JSON (não mais form-urlencoded como no sistema antigo). "corpoJson" já
     * deve vir pronto (org.json.JSONObject.toString()). Devolve o corpo da resposta em caso de
     * sucesso (2xx), "errocon" em caso de erro HTTP (mesma sinalização que as telas antigas já
     * verificavam), ou null em falha de rede.
     */
    public String sendJson(String url, String metodo, String corpoJson, String token) {
        RequestBody body = RequestBody.create(corpoJson, JSON);
        Request request = authorized(url, token).method(metodo, body).build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String texto = responseBody != null ? responseBody.string() : null;
            if (!response.isSuccessful()) return "errocon";
            return texto;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Devolve o código de status HTTP de um POST/PATCH JSON, ou -1 em falha de rede. */
    public int sendJsonStatus(String url, String metodo, String corpoJson, String token) {
        RequestBody body = RequestBody.create(corpoJson, JSON);
        Request request = authorized(url, token).method(metodo, body).build();
        try (Response response = client.newCall(request).execute()) {
            return response.code();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
