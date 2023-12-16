package lk.dharanimart.mobile.Communication;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Communication {
    public static String send(String RequestSrl, String jsonString, String token){
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        Log.d("MY_LOG", "API [" + RequestSrl + "]" + ": REQUEST" + jsonString);

        try {
            URL url = new URL(RequestSrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Cookie", "token="+ token +";");
            connection.setDoOutput(true);
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.writeBytes(jsonString);
                outputStream.flush();


            }
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            Log.d("MY_LOG", "REQUEST" + jsonString);

        } catch (IOException e) {
            Log.d("MY_LOG", "B " + e.getMessage() + ":- " + response.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("MY_LOG", "RESPONSE" + response.toString());
        return response.toString();
    }

    public static String getLocalUrl(String requestUrl) {
        String url = requestUrl;
        try {

            SendAsync sendAsync = new SendAsync();
            return sendAsync.execute(url).get();
        }catch (Exception e){
            Log.d("GET_URL",e.getMessage());
        }
        return "";
    }

    static class SendAsync extends AsyncTask<String,Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    reader.close();
                    return content.toString();
                } else {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }
    }
}
