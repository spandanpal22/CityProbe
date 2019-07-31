package com.mnk.env;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FetchActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    Spinner spinnerLocation, spinnerCollege, spinnerClassName, spinnerDataType;
    TextView textView_data;
    EditText editText_Date;
    String location_info, college_info, classroom_info, date_info, class_poll_info;

    FirebaseDatabase firebaseDatabase_ref;
    DatabaseReference databaseReference;
    List<DataModel> dataModelList;
    private Intent intent=null;
    List<ClassRoomData> classRoomDataList;
    FileOutputStream fileOutputStream;
    private static int STORAGE_PERMISSION_CODE = 1;
    //String str="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch);

        spinnerLocation = findViewById(R.id.spinner_ind_out);
        spinnerCollege = findViewById(R.id.spinner_college);
        spinnerDataType = findViewById(R.id.spinner_class_poll);
        editText_Date = findViewById(R.id.editText_date);
        spinnerClassName = findViewById(R.id.spinner_class_name);
        textView_data = findViewById(R.id.show_data_text_format);

        intent = new Intent(this, ShowData.class);

        dataModelList = new ArrayList<>();
        classRoomDataList = new ArrayList<>();
        FirebaseApp.initializeApp(this);
        firebaseDatabase_ref=FirebaseDatabase.getInstance();

        findViewById(R.id.fetch_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData();
            }
        });



        editText_Date.setText(DateFormat.getDateInstance().format(Calendar.getInstance().getTime()));
    }

    public void showData()
    {
        location_info = spinnerLocation.getSelectedItem().toString().trim();
        college_info = spinnerCollege.getSelectedItem().toString().trim();
        classroom_info = spinnerClassName.getSelectedItem().toString().trim();
        date_info = editText_Date.getText().toString().trim();
        class_poll_info = spinnerDataType.getSelectedItem().toString().trim();

/*        intent.putExtra("location_info1", location_info);
        intent.putExtra("college_info1", college_info);
        intent.putExtra("classroom_info1", classroom_info);
        intent.putExtra("date_info1", date_info);
        intent.putExtra("class_poll_info1", class_poll_info);

        startActivity(intent);*/
        databaseReference=firebaseDatabase_ref.getReference(location_info).child(college_info).child(classroom_info).child(class_poll_info).child(date_info);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataModelList.clear();
                classRoomDataList.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    if(class_poll_info.equals("Classroom Data"))
                    {
                        ClassRoomData classRoomData = dataSnapshot1.getValue(ClassRoomData.class);
                        classRoomDataList.add(classRoomData);
                    }
                    else if(class_poll_info.equals("Pollution Data"))
                    {
                        DataModel dataModel = dataSnapshot1.getValue(DataModel.class);
                        dataModelList.add(dataModel);
                    }
                }
                if(class_poll_info.equals("Classroom Data"))
                {
                    ClassRoomData classRoomData;
                    String str="Start_Time" + "\t" + "End_Time" + "\t" + "Class_Status" + "\t" +
                            "Occupants" + "\t" + "Doors" + "\t" + "Windows" + "\t" + "AC" + "\t" + "Fans" + "\n\n";
                    for(int i=0;i<classRoomDataList.size();i++) {
                        classRoomData = classRoomDataList.get(i);
                        str+=Integer.toString(i+1)+ "->\t" +classRoomData.getStart_Time() + "\t" + classRoomData.getEnd_Time() + "\t" + classRoomData.getClass_Status() + "\t" +
                                classRoomData.getOccupants() + "\t" +
                                classRoomData.getDoors() + "\t" + classRoomData.getWindows() + "\t" +
                                classRoomData.getAC() + "\t" + classRoomData.getFans()+"\n";
                    }
                    String filename = date_info + "_" + location_info + "_" + college_info + "_" + classroom_info + "_" + class_poll_info;
                    saveDataInFile(filename, str);
                }
                else if(class_poll_info.equals("Pollution Data"))
                {
                    DataModel dataModel;
                    String str="Current_Date_time" + "\t" + "Latitude" + "\t" + "Longitude" + "\t" +
                            "Temperature" + "\t" + "CO2" + "\t" + "CO" + "\t" + "NO2" + "\t" + "PM1" + "\t" + "PM10" + "\t" + "PM2.5" + "\t" + "Humidity" + "\n\n";;
                    for(int i=0;i<dataModelList.size();i++) {
                        dataModel = dataModelList.get(i);
                        str+=Integer.toString(i+1)+ "->\t" +dataModel.getCurrent_Date_time()+"\t"+dataModel.getLat()+"\t"+dataModel.getLang()+"\t"+
                                dataModel.getTemperature()+"\t"+dataModel.getCo2()+"\t"+dataModel.getCo()+"\t"+dataModel.getNo2()+"\t"+dataModel.getPm1()+"\t"+dataModel.getPm10()+"\t"+
                                dataModel.getPm25()+"\t"+dataModel.getHumidity() + "\n";
                    }
                    String filename = date_info + "_" + location_info + "_" + college_info + "_" + classroom_info + "_" + class_poll_info;
                    saveDataInFile(filename, str);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FetchActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showDateDialog(View view) {
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "date picker");
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this).setTitle("Permission Needed").setMessage("This Permission is needed because this and that")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(FetchActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(FetchActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    public void saveDataInFile(String filename, String str)
    {
        String tempDate = filename+".txt";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/ENV");
                if (!dir.exists())
                    dir.mkdir();
                File file = new File(dir, tempDate);
                try {
                    fileOutputStream = new FileOutputStream(file,false);
                    fileOutputStream.write(str.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                requestStoragePermission();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c =  Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DATE, dayOfMonth);
        String cuu = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());
        editText_Date.setText(cuu);
    }
}
