package com.example.myapplication;

import android.os.AsyncTask;
import com.example.myapplication.HttpUtility;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtility {

    public static void sendHttpRequest(final Context context, final String path) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    // 아두이노 웹 서버 주소 설정 (아두이노의 고정 IP 주소)
                    URL url = new URL("http://192.168.123.108" + path);

                    int serverPort = 80; // 아두이노의 포트 번호 (80번 포트)
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // 응답 코드 확인
                    int responseCode = connection.getResponseCode();

                    // 응답 데이터 읽기
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream is = connection.getInputStream();
                        int bytesRead;
                        byte[] buffer = new byte[1024];
                        StringBuilder response = new StringBuilder();
                        while ((bytesRead = is.read(buffer)) != -1) {
                            response.append(new String(buffer, 0, bytesRead));
                        }
                        is.close();
                        return response.toString();
                    } else {
                        return "HTTP error code: " + responseCode;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                // 응답 결과를 처리
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        };

        task.execute();
    }
}
