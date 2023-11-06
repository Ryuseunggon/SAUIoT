package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.TimeInfo;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import io.github.muddz.styleabletoast.StyleableToast;

public class MainActivity extends AppCompatActivity {

    private Button btnOn;
    private Button btnOff;
    private Button Rst;
    private TextView txt2, sendButton;
    private Switch Sw1;
    private EditText ssidEditText; // SSID 입력란
    private EditText passwordEditText; // 비밀번호 입력란
    private TimePicker timePicker;
    private Button setButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timePicker = findViewById(R.id.timePicker);
        setButton = findViewById(R.id.setButton);

        Rst = findViewById(R.id.Rst);
        
        ssidEditText = findViewById(R.id.id); // SSID 입력란
        passwordEditText = findViewById(R.id.pass); // 비밀번호 입력란
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);
        txt2 = findViewById(R.id.txt2);
        Sw1 = findViewById(R.id.Sw1);
        sendButton = findViewById(R.id.sendButton);

        Rst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHttpRequest("/Rst");
            }
        });

        Sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendHttpRequest("/buzOn");
                    Sw1.setText("경고음 켜짐");
                } else {
                    sendHttpRequest("/buzOff");
                    Sw1.setText("경고음 꺼짐");
                }
            }
        });

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHttpRequest("/ledOn");
                txt2.setText("작동 유무 : On");
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHttpRequest("/ledOff");
                txt2.setText("작동 유무 : Off");
            }
        });

        // "Send" 버튼 클릭 이벤트 핸들러 추가

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = ssidEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // SSID와 비밀번호를 아두이노로 전송하는 AsyncTask 시작
                new SendWifiInfoTask().execute(ssid, password);
            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

                if (delay < 0) {
                    delay += 24 * 60 * 60 * 1000; // 음수인 경우 다음 날로 설정
                }
                selectedTimes.add(new TimeInfo(hour, minute));
                String timeMessage = "Selected Time: " + hour + ":" + minute;
                Toast.makeText(MainActivity.this, timeMessage, Toast.LENGTH_SHORT).show();
                scheduleNotification(delay);
            }
        });

        TextView viewTimeButton = findViewById(R.id.viewTimeButton);
        viewTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 설정한 시간 정보를 가져와서 새 화면으로 전달
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();

                Intent intent = new Intent(MainActivity.this, NewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedTimes", selectedTimes);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }

    private ArrayList<TimeInfo> selectedTimes = new ArrayList<>();
    private void scheduleNotification ( long delay){
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
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
                StyleableToast.makeText(MainActivity.this, result, R.style.mystyle).show();
            }
        };

        task.execute();
    }

    private class SendWifiInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ssid = params[0];
            String password = params[1];

            try {
                // 아두이노 웹 서버 주소 설정 (아두이노의 고정 IP 주소)
                URL url = new URL("http://192.168.123.108/setWifi");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // POST 데이터 전송
                String postData = "ssid=" + ssid + "&password=" + password;

                connection.getOutputStream().write(postData.getBytes("UTF-8"));

                // 응답 코드 확인
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 성공적으로 와이파이 정보를 아두이노로 전송한 경우
                    return "와이파이 정보가 업데이트되었습니다.";
                } else {
                    return "HTTP 오류 코드: " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "오류: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // 결과 처리 (예: Toast 메시지 표시)
            StyleableToast.makeText(MainActivity.this, result,R.style.mystyle).show();
        }
    }
}
