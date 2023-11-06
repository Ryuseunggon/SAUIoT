package com.example.myapplication;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import java.lang.reflect.Type;
import java.util.Calendar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

public class NewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TimeAdapter timeAdapter;
    private ArrayList<TimeInfo> selectedTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        recyclerView = findViewById(R.id.timeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        selectedTimes = new ArrayList<>();  // 선택한 시간 정보를 저장할 ArrayList

        timeAdapter = new TimeAdapter(selectedTimes);
        recyclerView.setAdapter(timeAdapter);

        // 인텐트에서 시간 정보를 가져와 ArrayList에 추가
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            ArrayList<TimeInfo> receivedTimes = (ArrayList<TimeInfo>) bundle.getSerializable("selectedTimes");
            if (receivedTimes != null) {
                selectedTimes.addAll(receivedTimes);
                timeAdapter.notifyDataSetChanged();
            }
        }

        // RecyclerView의 아이템을 삭제하는 리스너 추가
        timeAdapter.setOnItemClickListener(new TimeAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                selectedTimes.remove(position);
                timeAdapter.notifyDataSetChanged();
                deleteItemAndSaveState(position);
            }
        });
    }

    private void deleteItemAndSaveState(int position) {
        // 삭제할 아이템의 시간 정보를 가져옴
        TimeInfo timeInfo = selectedTimes.get(position);

        // 삭제할 아이템을 RecyclerView에서 제거
        timeAdapter.removeItem(position);

        // 선택한 시간 리스트를 업데이트
        selectedTimes.remove(position);

        // 여기에서 수정된 목록(selectedTimes)을 저장하도록 추가 코드 필요 (예: SharedPreferences 또는 파일 등을 사용하여 저장)
        saveListToStorage(selectedTimes);
    }

    private void saveListToStorage(ArrayList<TimeInfo> list) {
        // SharedPreferences를 사용하여 목록을 저장
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // ArrayList를 JSON 문자열로 변환하여 저장
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("timeList", json);
        editor.apply();
    }

    private ArrayList<TimeInfo> loadListFromStorage() {
        // SharedPreferences를 사용하여 저장된 목록을 불러옴
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("timeList", null);

        if (json != null) {
            // JSON 문자열을 ArrayList로 변환
            Type type = new TypeToken<ArrayList<TimeInfo>>() {}.getType();
            return new Gson().fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    private void deleteItemAndCancelNotification(int position) {
        // 삭제할 아이템의 시간 정보를 가져옴
        TimeInfo timeInfo = selectedTimes.get(position);

        // 삭제할 아이템을 RecyclerView에서 제거
        timeAdapter.removeItem(position);

        // 해당 시간에 동작 예정이었던 작업을 취소
        cancelNotification(timeInfo);

        // 선택한 시간 리스트를 업데이트
        selectedTimes.remove(position);
    }
    private void cancelNotification(TimeInfo timeInfo) {
        // 해당 시간에 동작 예정이었던 작업을 취소하는 코드를 작성
        // 이 부분은 실제로 예정된 작업을 취소하려면 해당 작업의 식별자 또는 태그 등을 사용해야 합니다.
        // 아래는 예시 코드이며, 실제로 취소해야 하는 작업에 따라 다를 수 있습니다.

        // 예시: 선택한 시간 정보를 기반으로 해당 작업을 취소
        int hour = timeInfo.getHour();
        int minute = timeInfo.getMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            delay += 24 * 60 * 60 * 1000; // 음수인 경우 다음 날로 설정
        }

        // 작업을 취소하는 코드 (예: WorkManager 작업을 취소)
        WorkManager.getInstance(this).cancelAllWorkByTag("TAG_FOR_NOTIFICATION_" + hour + "_" + minute);
    }
}
