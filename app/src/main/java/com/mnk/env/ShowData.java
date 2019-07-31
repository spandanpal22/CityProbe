package com.mnk.env;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowData extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase_ref;
    DatabaseReference databaseReference;
    List<DataModel> dataModelList;
    String location_info, college_info, classroom_info, date_info, class_poll_info;

    TextView showData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        showData=findViewById(R.id.show_data_text);

        dataModelList = new ArrayList<>();
        FirebaseApp.initializeApp(this);
        firebaseDatabase_ref=FirebaseDatabase.getInstance();
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            location_info= bundle.getString("location_info1");
            college_info= bundle.getString("college_info1");
            classroom_info= bundle.getString("classroom_info1");
            date_info= bundle.getString("date_info1");
            class_poll_info= bundle.getString("class_poll_info1");


        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
