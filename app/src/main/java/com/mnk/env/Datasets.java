package com.mnk.env;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.mnk.env.Bluetooth_set.STATE_CONNECTED;
import static com.mnk.env.Bluetooth_set.STATE_CONNECTING;
import static com.mnk.env.Bluetooth_set.STATE_CONNECTION_FAILED;
import static com.mnk.env.Bluetooth_set.STATE_LISTNING;
import static com.mnk.env.Bluetooth_set.STATE_MESSAGE_RECEIVED;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class Datasets extends AppCompatActivity implements View.OnClickListener {

    BluetoothSocket socket;
    Datasets.SendReceive sendReceive;
    BluetoothAdapter bluetoothAdapter;
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String APP_NAME="IndoAir";
    File file;

    private ToneGenerator toneGenerator;
    int snooze_count = 0;

    FirebaseDatabase database;
    DatabaseReference Ref;
    DatabaseReference classInfoRef;

    Constants constants=null;
    static int startHandler=0;
    int sms_send_time = 0;
    TextView data_msg,alertmsg;
    private String InstituteName, ClassRoomId, Occupants, AC, Fans, Window, Doors, StartTime, EndTime;
    private String date;
    static int start=0;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datasets);

        start=1;

        data_msg = findViewById(R.id.data_msg);
        alertmsg = findViewById(R.id.alertmsg);
        status = findViewById(R.id.status);

        findViewById(R.id.start_time).setOnClickListener(this);
        findViewById(R.id.change_time).setOnClickListener(this);
        findViewById(R.id.end_time).setOnClickListener(this);

        data_msg.setMovementMethod(new ScrollingMovementMethod());
        constants = new Constants();

        date = DateFormat.getDateInstance().format(Calendar.getInstance().getTime());

        FirebaseApp.initializeApp(this);
        database=FirebaseDatabase.getInstance();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            InstituteName = bundle.getString(constants.INSTITUTE_ID);
            ClassRoomId = bundle.getString(constants.CLASSROOM_ID);
            Occupants = bundle.getString(constants.OCCUPANTS_ID);
            AC = bundle.getString(constants.AC_ID);
            Fans = bundle.getString(constants.FANS_ID);
            Doors = bundle.getString(constants.DOORS_ID);
            Window = bundle.getString(constants.WINDOW_ID);
            StartTime = bundle.getString(constants.START_TIME_ID);
            EndTime = bundle.getString(constants.END_TIME_ID);
            /*+ AC+ Fans+ Doors+ Window+ + null+ class_status*/

            //Toast.makeText(this, "H: "+StartTime, Toast.LENGTH_SHORT).show();
            //String date = DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
            classInfoRef=database.getReference("Indoor").child(InstituteName)
                    .child(ClassRoomId).child("Classroom Data")
                    .child(date);
            String timeStamp = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
            Toast.makeText(this, "Time: "+timeStamp, Toast.LENGTH_SHORT).show();

            if (Occupants == null)
                class_status = "Class Not Started";
            if(StartTime==null)
                StartTime=timeStamp;
            classInfoRef.push().setValue(new ClassRoomData(class_status, Doors, AC, StartTime, EndTime, Window,Occupants, Fans));



        }
        if(InstituteName==null || ClassRoomId==null)
        {
         Ref=  database.getReference("default").child("Pollution Data");
        }
        else {
            Ref = database.getReference("Indoor").child(InstituteName)
                    .child(ClassRoomId).child("Pollution Data")
                    .child(date);
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Datasets.ClientClass clientClass = new Datasets.ClientClass(Bluetooth_set.connectbt);
        clientClass.start();
    }

        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (start == 1) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String currentDate = sdf.format(new Date());
                    String filename = currentDate;
                    switch (msg.what) {
                        case STATE_LISTNING:
                            status.setText("Listning");
                            break;
                        case STATE_CONNECTING:
                            status.setText("Connecting");
                            break;
                        case STATE_CONNECTED:
                            status.setText("Connected");
                            break;
                        case STATE_CONNECTION_FAILED:
                            status.setText("Connection_Failed");
                            break;
                        case STATE_MESSAGE_RECEIVED:
                            String readBuffr = (String) msg.obj;

                            String tempMsg = readBuffr;

                            String path = Environment.getExternalStorageDirectory() + "/BluetoothApp/";
                            file = new File(path);
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            RandomAccessFile raf;
                            try {
                                raf = new RandomAccessFile(file + "/Filename.txt", "rw");
                                raf.seek(raf.length());
                                raf.write(tempMsg.getBytes());
                                raf.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            String[] arr_data = tempMsg.split(",");
                            String dispaly_data = data_msg.getText().toString() + "\n" + tempMsg;

                            data_msg.setText(dispaly_data);


                            AQI aqi = new AQI();
                            String worning_msg = "";
                            if (arr_data.length > 9) {
                                String _id = Ref.push().getKey();
                                Ref.child(_id).setValue(new DataModel(_id, java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()), String.valueOf(0.00), String.valueOf(0.00).trim(), arr_data[0].trim(), arr_data[1].trim(), arr_data[2].trim(), arr_data[3].trim(), arr_data[4].trim(), arr_data[5].trim(), arr_data[6].trim(), arr_data[7].trim(), arr_data[8].trim(), arr_data[9].trim()));
                                worning_msg += aqi.aqiTest((float) Double.parseDouble(arr_data[4].trim()), 0, 50, 51, 100, 101, 250, 251, 350, 351, 430, "PM10");
                                worning_msg += aqi.aqiTest(Float.parseFloat(arr_data[3].trim()), 0, 30, 31, 60, 61, 90, 91, 120, 121, 250, "PM2.5");
                                worning_msg += aqi.aqiTest(Float.parseFloat(arr_data[7].trim()), 0.0f, 1.0f, 1.1f, 2.0f, 2.1f, 10.0f, 10.0f, 17.0f, 17.0f, 34.0f, "CO");
                                worning_msg += aqi.aqiTest(Float.parseFloat(arr_data[6].trim()), 0, 40, 41, 80, 81, 180, 181, 280, 281, 400, "CO2");
                                if (worning_msg.trim() != null && worning_msg.trim() != "" && snooze_count == 0) {
                                    alertmsg.setText(worning_msg);
//                        //Toast.makeText(MainActivity.this, "" + worning_msg, Toast.LENGTH_SHORT).show();
                                    playTone();
                                    if (sms_send_time == 120) {
                                        //SmsManager smsManager = SmsManager.getDefault();
                                        //smsManager.sendTextMessage("+919434789009", null, "alert sms:"+worning_msg, null, null);
                                        //  sms_send_time=0;

                                    } else {
                                        sms_send_time++;

                                    }
//
                                    if (worning_msg == "" || worning_msg == null) {
                                        alertmsg.setText("");

                                    }
                                    if (snooze_count != 0) {
                                        snooze_count--;
                                    }
                                }
                            }
                    }
                }
                    return true;
                }
        });

        void playTone () {

        try {

            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            }
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 900);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGenerator != null) {
                        toneGenerator.release();
                        toneGenerator = null;
                    }
                }

            }, 900);
        } catch (Exception e) {
            Log.d("ex", "Exception while playing sound:" + e);
        }
    }

    String class_status=null;

    @Override
    public void onClick(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.starttime_dialog,null);
        dialogBuilder.setView(dialogView);
        final TextView textViewTime = dialogView.findViewById(R.id.textViewTime);
        final EditText editTextOccupants = dialogView.findViewById(R.id.occupants);
        final EditText editTextAC = dialogView.findViewById(R.id.ac);
        final EditText editTextFans = dialogView.findViewById(R.id.fan);
        final EditText editTextWindow = dialogView.findViewById(R.id.window);
        final EditText editTextDoors = dialogView.findViewById(R.id.door);
        final EditText editTextStartTime = dialogView.findViewById(R.id.start_time);
        final Button saveClassInfo = dialogView.findViewById(R.id.saveClassInfo);

        switch (v.getId())
        {
            case R.id.start_time:
                textViewTime.setText(R.string.class_started);
                class_status = "Class Started";
                break;

            case R.id.change_time:
                textViewTime.setText(R.string.class_middle);
                class_status = "Changes in Middle of Class";
                break;

            case R.id.end_time:
                textViewTime.setText(R.string.class_ended);
                class_status = "Class Ended";
                break;
        }
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        saveClassInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Occupants = editTextOccupants.getText().toString().trim();
                AC = editTextAC.getText().toString().trim();
                Fans = editTextFans.getText().toString().trim();
                Window = editTextWindow.getText().toString().trim();
                Doors = editTextDoors.getText().toString().trim();
                StartTime = editTextStartTime.getText().toString().trim();
                String s = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

                EndTime = null;
                switch (v.getId())
                {
                    case R.id.change_time:
                        StartTime = null;
                        EndTime = null;
                        break;

                    case R.id.end_time:
                        StartTime = null;
                        EndTime = StartTime;
                        break;
                }

                classInfoRef=database.getReference("Indoor").child(InstituteName).child(ClassRoomId).child("Classroom Data").child(date);
                ClassRoomData classRoomInfo = new ClassRoomData( class_status, Doors, AC, StartTime, EndTime, Window,Occupants, Fans);
                classInfoRef.push().setValue(classRoomInfo);

                alertDialog.dismiss();
            }
        });
    }

    public void syncClock(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.clock_sync,null);
        dialogBuilder.setView(dialogView);
        final EditText editTextWriteMessage = dialogView.findViewById(R.id.writeMessage);
        final Button buttonSyncClock = dialogView.findViewById(R.id.sendMessage);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonSyncClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editTextWriteMessage.getText().toString();
                sendReceive.write(message);
                alertDialog.dismiss();
            }
        });
    }

    private class ClientClass extends Thread {
        BluetoothDevice device;

        public ClientClass(BluetoothDevice device1) {
            device = device1;
            socket = Bluetooth_set.socket;
        }

        public void run() {
            Message message = Message.obtain();
            message.what = STATE_CONNECTED;
            handler.sendMessage(message);
            sendReceive = new Datasets.SendReceive(Bluetooth_set.socket);
            sendReceive.start();
        }
    }

    private class Serverclass extends Thread {
        private BluetoothServerSocket serverSocket;
        public Serverclass() {
            try { serverSocket=bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME,Bluetooth_set.MY_UUID);
            } catch (IOException e) { e.printStackTrace(); }
        }

        @Override
        public void run() {
            BluetoothSocket socket=null;
            while (socket==null) {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message1=Message.obtain();
                    message1.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message1);
                }
                if (socket!=null) {
                    Message message1=Message.obtain();
                    message1.what=STATE_CONNECTING;
                    handler.sendMessage(message1);

                    sendReceive=new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }

    public void btnsnooze(View view) { snooze_count=60; }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        InputStream inputStream;
        OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;

            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) { e.printStackTrace(); }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            final byte delimiter = 10;
            int readBufferPosition = 0;
            while (true) {

                try {
                    int bytesAvailable = inputStream.available();

                    if (bytesAvailable > 0) {
                        byte[] packetByte = new byte[bytesAvailable];
                        inputStream.read(packetByte);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetByte[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes);
                                readBufferPosition = 0;

                                handler.obtainMessage(STATE_MESSAGE_RECEIVED, data).sendToTarget();
                            } else
                            {
                                buffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(String str) {
            try {
                outputStream.write(str.getBytes());
            } catch (Exception e) { e.printStackTrace(); }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        start=0;
    }


    @Override
    protected void onPause() {
        super.onPause();
    }
}