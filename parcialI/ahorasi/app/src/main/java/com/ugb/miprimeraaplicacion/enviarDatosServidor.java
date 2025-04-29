package com.ugb.miprimeraaplicacion;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log; // Importa la clase Log

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class enviarDatosServidor extends AsyncTask<String, String, String> {
    Context context;
    String respuesta = "";
    HttpURLConnection httpURLConnection;

    public enviarDatosServidor(Context context) {
        this.context = context;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // Puedes manejar la respuesta aquí, si es necesario.
        // Por ejemplo, mostrar un Toast o actualizar la UI.
    }

    @Override
    protected String doInBackground(String... parametros) {
        String jsonResponse = "";
        String jsonDatos = parametros[0]; // Datos JSON a enviar
        String metodo = parametros[1];     // Método HTTP (POST, GET, PUT, DELETE)
        String _url = parametros[2];      // URL del servidor

        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(_url);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod(metodo);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Authorization", "Basic " + utilidades.credencialesCodificadas);

            // Enviar los datos al servidor
            Writer writer = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));
            writer.write(jsonDatos);
            writer.close();

            // Obtener/leer la respuesta del servidor
            InputStream inputStream = httpURLConnection.getInputStream();
            if (inputStream == null) {
                return null; // No hubo respuesta
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


            // Procesar la respuesta del servidor
            String linea;
            StringBuilder stringBuilder = new StringBuilder(); // Usar StringBuilder para eficiencia
            while ((linea = bufferedReader.readLine()) != null) {
                stringBuilder.append(linea);
            }
            if (stringBuilder.length() == 0) {
                return null; // Respuesta vacía
            }
            jsonResponse = stringBuilder.toString();

        } catch (Exception e) {
            Log.e("enviarDatosServidor", "Error: " + e.getMessage()); // Log del error
            return "Error: " + e.getMessage();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    Log.e("enviarDatosServidor", "Error al cerrar bufferedReader: " + e.getMessage());
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return jsonResponse;
    }
}