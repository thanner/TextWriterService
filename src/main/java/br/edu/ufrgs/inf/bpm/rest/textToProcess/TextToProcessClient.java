package br.edu.ufrgs.inf.bpm.rest.textToProcess;

import de.hpi.bpt.process.Process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TextToProcessClient {

    /**
     private final String USER_AGENT = "Mozilla/5.0";

     public String sendGet(String urlCallWS, String verb) {
     StringBuffer response = new StringBuffer();
     try {
     URL url = new URL(urlCallWS);
     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
     connection.setRequestMethod(verb);
     connection.setRequestProperty("User-Agent", USER_AGENT);
     int responseCode = connection.getResponseCode();

     BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

     String aux;
     while ((aux = br.readLine()) != null) {
     response.append(aux);
     }
     br.close();

     } catch (java.io.IOException e) {
     e.printStackTrace();
     }
     return response.toString();
     }

     public String sendText(String urlCall, String text){
     // Process
     StringBuilder response = new StringBuilder();
     try {
     URL url = new URL(urlCall);
     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
     connection.setDoOutput(true);
     connection.setRequestMethod("POST");
     connection.setRequestProperty("Content-Type", "text/plain");

     OutputStream os = connection.getOutputStream();
     os.write(text.getBytes());
     os.flush();

     if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
     throw new RuntimeException("Failed : HTTP error code : "
     + connection.getResponseCode());
     }
     BufferedReader br = new BufferedReader(new InputStreamReader(
     (connection.getInputStream())));

     String aux;
     System.out.println("Output from Server .... \n");
     while ((aux = br.readLine()) != null) {
     response.append(aux);
     }
     connection.disconnect();

     } catch(IOException e){
     e.printStackTrace();
     }
     return response.toString();
     }
     **/

    public String getProcess(String text) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(TextToProcessURLs.GetProcessURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/plain");

            OutputStream os = connection.getOutputStream();
            os.write(text.getBytes());
            os.flush();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + connection.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (connection.getInputStream())));

            String aux;
            System.out.println("Output from Server .... \n");
            while ((aux = br.readLine()) != null) {
                response.append(aux);
            }
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

}
