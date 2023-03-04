package com.example.trycalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    ArrayList<Schedule> arrayList;
    RecyclerView normal_rec;
    private ScheduleAdapter adapter;
    private ArrayList<Schedule> scheduleArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        normal_rec = findViewById(R.id.normal_rec);
        normal_rec.setLayoutManager(new LinearLayoutManager(this));
        scheduleArrayList = new ArrayList<>();
        adapter = new ScheduleAdapter(this, scheduleArrayList, this);
        normal_rec.setAdapter(adapter);
//        normal_rec.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
//        createListData();

        calendar = (CalendarView) findViewById(R.id.calendar);
        editText = (TextView) findViewById(R.id.date_view);

        Long currentTime = System.currentTimeMillis();
//        calendar.setMinDate(currentTime);
//        calendar.setDate(currentTime);


        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

                date = dayOfMonth + "-" + (month + 1) + "-" + year;
                databaseReference = FirebaseDatabase.getInstance().getReference("Calendar").child(date);
                editText.setText(date);
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
                        databaseReference.child("10:00").setValue(0);
                        databaseReference.child("12:00").setValue(0);
                        databaseReference.child("08:00").setValue(0);
                    }
                    else
                        Toast.makeText(MainActivity.this, "המטווח אינו פעיל בשבת", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "error- canceled. try again", Toast.LENGTH_SHORT).show();
            }
        });
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                scheduleArrayList.clear();
//                if(snapshot.getValue() != null) {
////                    Toast.makeText(MainActivity.this, "exist", Toast.LENGTH_SHORT).show();
//
//
//                }
//                else {
//                    scheduleArrayList.clear();
//                    adapter.notifyDataSetChanged();
//                }
////                    Toast.makeText(MainActivity.this, "nope", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    //    public void buttonSaveEvent(View view) {
//        Things thing = new Things("a", "b");
//        databaseReference.child(date).setValue(thing.getA()+" "+ thing.getB());
//        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
//    }
    public void buttonConfirm(View view) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Schedule schedule = new Schedule(dataSnapshot.getKey(), Integer.parseInt(dataSnapshot.getValue().toString()));
                        scheduleArrayList.add(schedule);

//                        Toast.makeText(MainActivity.this, ""+schedule, Toast.LENGTH_SHORT).show();
//                        Toast.makeText(MainActivity.this, ""+dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
//                        Toast.makeText(MainActivity.this, ""+dataSnapshot.getValue(), Toast.LENGTH_SHORT).show();
                    }
                    adapter.notifyDataSetChanged();
                } else
                    Toast.makeText(MainActivity.this, "error in getting data", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "error- canceled. try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

//    private void createListData() {
//        Schedule schedule = new Schedule("08:00", 9);
//        scheduleArrayList.add(schedule);
//        adapter.notifyDataSetChanged();
//    }

    private String getMonthFormat(int month) {
        switch (month) {
            case 1:
                return "ינואר";
            case 2:
                return "פברואר";
            case 3:
                return "מרץ";
            case 4:
                return "אפריל";
            case 5:
                return "מאי";
            case 6:
                return "יוני";
            case 7:
                return "יולי";
            case 8:
                return "אוגוסט";
            case 9:
                return "ספטמבר";
            case 10:
                return "אוקטובר";
            case 11:
                return "נובמבר";
            case 12:
                return "דצמבר";

        }
        return "Error";
    }

    @Override
    public void onItemClicked(Schedule schedule) {
        Toast.makeText(this, "" + schedule, Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_spot, null, false);
        builder.setView(dialogView);
        AlertDialog ad = builder.create();
        Button btnYes = dialogView.findViewById(R.id.buttonYes);
        Button btnNo = dialogView.findViewById(R.id.buttonNo);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });
        ad.show();
    }
}