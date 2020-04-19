package com.example.cadier.util;

import android.util.Log;
import android.util.MalformedJsonException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by DrGreend on 28/03/2018.
 */

public class ConectaWebService {
    private int TIMEOUT_MILLISEC = 300000;
    private static final String USER_AGENT = "Mozilla/5.0";

    public String request(String stringUrl) {
        HttpURLConnection conn = null;
        BufferedReader in = null;
        try {
            URL obj = new URL(stringUrl);
            conn = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            conn.setRequestMethod("GET");

            //add request header
            conn.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("CONECTOU! " + responseCode);
            } else {
                System.out.println("DEU RUIM!!");
            }

            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
            return response.toString();
        } catch (MalformedJsonException e) {
            System.out.println("Erro: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.out.println("Erro: " + e.getMessage());
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
                System.out.println("Erro!! " + e.getMessage());
            }
        }
    }

    public String send(String url, String tipo, String urlParameters) {
        HttpsURLConnection conn = null;
        BufferedReader in = null;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod(tipo);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("XAMPSON", String.valueOf(responseCode));
                if(response.toString().equalsIgnoreCase("erroli")) {
                    return "erroli";
                } else {
                    System.out.println(response.toString());
                    return response.toString();
                }
            } else if(responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                Log.d("XAMPSON", String.valueOf(responseCode));
                return "errocon";
            } else {
                return response.toString();
            }

        } catch (MalformedJsonException e) {
            System.out.println("ErroJSON: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.out.println("ErroJSON: " + e.getMessage());
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
                System.out.println("Erro!! " + e.getMessage());
                return null;
            }
        }
    }
}
