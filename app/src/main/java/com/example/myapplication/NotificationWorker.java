package com.example.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.github.muddz.styleabletoast.StyleableToast;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 원하는 작업을 수행
        showNotification();
        sendHttpRequest("/ledOn");
        return Result.success();
    }

    private void showNotification() {
        // 알람을 울리는 코드를 여기에 추가
        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 1000}; // 진동 패턴 설정 (0초 대기, 1초 진동, 1초 대기, 1초 진동, ...)
            vibrator.vibrate(pattern, -1); // 진동 패턴 실행, -1은 반복 없음을 의미
        }
    }
    private void sendHttpRequest(final String path) {
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
                StyleableToast.makeText(NotificationWorker.this.getApplicationContext(), result, R.style.mystyle).show();
            }
        };

        task.execute();
    }
}
