package com.example.trycalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SelectListener {

    CalendarView calendar;
    TextView editText;
    DatabaseReference databaseReference;
    String date;
    String simpleDate = "";
    RecyclerView normal_rec;
    private ScheduleAdapter scheduleAdapter;
    private ArrayList<Schedule> scheduleArrayList;

    MyDatabaseHelper myDB;
    ArrayList<String> book_id, book_date, book_time;
    CustomAdapterSQLite customAdapterSQLite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        normal_rec = findViewById(R.id.normal_rec);
        normal_rec.setLayoutManager(new LinearLayoutManager(this));
        scheduleArrayList = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(this, scheduleArrayList, this);
        normal_rec.setAdapter(scheduleAdapter);
//        normal_rec.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
//        createListData();

        calendar = (CalendarView) findViewById(R.id.calendar);
        editText = (TextView) findViewById(R.id.date_view);

        storeDataInArrays();
        customAdapterSQLite = new CustomAdapterSQLite(MainActivity.this, book_id, book_date, book_time);

        Long currentTime = System.currentTimeMillis();
//        calendar.setMinDate(currentTime);
//        calendar.setDate(currentTime);


        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

                date = dayOfMonth + "-" + (month + 1) + "-" + year;
                databaseReference = FirebaseDatabase.getInstance().getReference("Calendar").child(date);
                editText.setText("?????????????? ????????????: " + date);
                simpleDate = "D" + dayOfMonth + "M" + (month + 1) + "Y" + year;
                calendarClicked(isSaturday(dayOfMonth, month, year));
            }
        });


//        Button button = findViewById(R.id.button4);
//        button.setOnClickListener(view -> {
//            createListData();
//        });
    }

    private boolean isSaturday(int day, int month, int year) {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(new Date(year, month, day - 1));
        return calendar2.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
    }

    private void calendarClicked(boolean isSaturday) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scheduleArrayList.clear();
                if (snapshot.getValue() != null) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Schedule schedule = new Schedule(dataSnapshot.getKey(), Integer.parseInt(dataSnapshot.getValue().toString()));
                        scheduleArrayList.add(schedule);

                    }

                } else {
                    if (!isSaturday) {
                        databaseReference.child("10:00").setValue(12);
                        databaseReference.child("12:00").setValue(12);
                        databaseReference.child("08:00").setValue(12);
                    } else
                        Toast.makeText(MainActivity.this, "???????????? ???????? ???????? ????????", Toast.LENGTH_SHORT).show();
                }
                scheduleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "error- canceled. try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //    public void buttonSaveEvent(View view) {
//        Things thing = new Things("a", "b");
//        databaseReference.child(date).setValue(thing.getA()+" "+ thing.getB());
//        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
//    }
    public void buttonConfirm(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_spot, null, false);
        builder.setView(dialogView);
        AlertDialog ad = builder.create();
        Button btnYes = dialogView.findViewById(R.id.buttonYes);
        Button btnNo = dialogView.findViewById(R.id.buttonNo);

        //        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.getValue() != null) {
//
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        Schedule schedule = new Schedule(dataSnapshot.getKey(), Integer.parseInt(dataSnapshot.getValue().toString()));
//                        scheduleArrayList.add(schedule);
//
////                        Toast.makeText(MainActivity.this, ""+schedule, Toast.LENGTH_SHORT).show();
////                        Toast.makeText(MainActivity.this, ""+dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
////                        Toast.makeText(MainActivity.this, ""+dataSnapshot.getValue(), Toast.LENGTH_SHORT).show();
//                    }
//                    adapter.notifyDataSetChanged();
//                } else
//                    Toast.makeText(MainActivity.this, "error in getting data", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MainActivity.this, "error- canceled. try again", Toast.LENGTH_SHORT).show();
//            }
//        });

    }

//    private void createListData() {
//        Schedule schedule = new Schedule("08:00", 9);
//        scheduleArrayList.add(schedule);
//        adapter.notifyDataSetChanged();
//    }

    private String getMonthFormat(int month) {
        switch (month) {
            case 1:
                return "??????????";
            case 2:
                return "????????????";
            case 3:
                return "??????";
            case 4:
                return "??????????";
            case 5:
                return "??????";
            case 6:
                return "????????";
            case 7:
                return "????????";
            case 8:
                return "????????????";
            case 9:
                return "????????????";
            case 10:
                return "??????????????";
            case 11:
                return "????????????";
            case 12:
                return "??????????";

        }
        return "Error";
    }

    @Override
    public void onItemClicked(Schedule schedule) {
        if (schedule.getPeople() != 0) {
            if (isAvailable()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_spot, null, false);
                builder.setView(dialogView);
                AlertDialog ad = builder.create();
                ad.setCancelable(false);
                Button btnYes = dialogView.findViewById(R.id.buttonYes);
                Button btnNo = dialogView.findViewById(R.id.buttonNo);
                TextView textView = dialogView.findViewById(R.id.textView);
                textView.setText("???????????? ???????????? ????????????" + "\n" + date + " " + "????????" + " " + schedule.getHour() + "?");

                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(MainActivity.this);
                        myDB.addBook(simpleDate.trim(), schedule.getHour().trim());
                        storeDataInArrays();
                        databaseReference.child("" + schedule.getHour()).setValue(schedule.getPeople() - 1);
                        ad.dismiss();
                    }
                });
                btnNo.setOnClickListener(view -> ad.dismiss());
                ad.show();
            } else
                Toast.makeText(MainActivity.this, "?????? ???????? ?????????? ?????????? ??????", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(MainActivity.this, "?????? ?????? ???????????? ????????????", Toast.LENGTH_SHORT).show();
    }

    void storeDataInArrays() {
        myDB = new MyDatabaseHelper(MainActivity.this);
        book_time = new ArrayList<>();
        book_id = new ArrayList<>();
        book_date = new ArrayList<>();
        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0)
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        else
            while (cursor.moveToNext()) {
                book_id.add(cursor.getString(0));
                book_date.add(cursor.getString(1));
                book_time.add(cursor.getString(2));
            }
    }

    public boolean isAvailable() {
        if (book_date == null || book_date.size() == 0)
            return true;
        String st = simpleDate.substring(simpleDate.indexOf("M") + 1, simpleDate.indexOf("Y"));
        for (String s : book_date) {
            String temp = s.substring(s.indexOf("M") + 1, s.indexOf("Y"));
            if (temp.equals(st))
                return false;
        }
        return true;
    }
}