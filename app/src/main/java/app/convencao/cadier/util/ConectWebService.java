package app.convencao.cadier.util;

import android.util.Log;
import android.util.MalformedJsonException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by DrGreend on 28/03/2018.
 */

public class ConectWebService {
    private static final String USER_AGENT = "Mozilla/5.0";

    public String request(String stringUrl) {
        HttpURLConnection conn = null;
        BufferedReader in = null;
        try {
            URL obj = new URL(stringUrl);
            conn = (HttpURLConnection) obj.openConnection();

            conn.setRequestMethod("GET");

            conn.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                in.close();
                return null;
            }
        } catch (MalformedJsonException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public String send(String url, String tipo, Map<String, String> urlParameters){
        BufferedReader in = null;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod(tipo);
            //con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String joiner = "";
            int counter = 0;
            for (Map.Entry<String, String> entry : urlParameters.entrySet()) {
                counter++;
                joiner += URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8");
                if (counter < urlParameters.size()) {
                    joiner += "&";
                }
            }
            byte[] out = joiner.getBytes();
            int length = out.length;

            con.setFixedLengthStreamingMode(length);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.connect();
            try (OutputStream os = con.getOutputStream()) {
                os.write(out);
            }

            int responseCode = con.getResponseCode();

            if(responseCode != 200 || responseCode != 401) {

                in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                return "errocon";
            }

        } catch(Exception ex){
            return null;
        }
    }
}
