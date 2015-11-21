package com.aiworker.vreeg;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    public static Handler mUiHandler = null;
    public final static String EXTRA_MESSAGE = "com.aiworker.vreeg.MESSAGE";
    public static String userName="", userMail="", strStart="",strStop="";
    public static String userActivity="";
    private static final String TAG = "MainActivity";
    public static final String tService = "toggleButtonService";

    TextView tv_Med, tv_Att, tv_NeuroskyStatus, tv_StartStatus;
    public static int At=42, Med=42;
    public String ServiceRunningFlag = "stoped";
    public static String NeuroskyStatus = "Neurosky disconnected";
    EditText etv_mail;

    ToggleButton serviceOnOff;
    public static boolean State_serviceOnOff;
    String Key_State_serviceOnOff;

    ImageButton ibStart, ibStop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tv_Att = (TextView) findViewById(R.id.Att_label);
        tv_Med = (TextView) findViewById(R.id.Med_lable);
        tv_NeuroskyStatus = (TextView) findViewById(R.id.NeuroskyStatus);
        tv_StartStatus = (TextView) findViewById(R.id.tv_start_status);

        serviceOnOff = (ToggleButton) findViewById(R.id.switch_service);

        ibStart = (ImageButton) findViewById(R.id.ib_start);
        ibStop = (ImageButton) findViewById(R.id.ib_stop);

        etv_mail = (EditText) findViewById(R.id.et_email);


        mUiHandler = new Handler() // Receive messages from service class
        {
            public void handleMessage(Message msg)
            {
                switch(msg.what)
                {
                    case 0:
                        // add the status which came from service and show on GUI
                        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                        break;

                    case 1:
                        NeuroskyStatus = msg.obj.toString();
                        tv_NeuroskyStatus.setText(NeuroskyStatus);

                        if(NeuroskyStatus.equals("Neurosky connected") ){
                            State_serviceOnOff = true;
                            serviceOnOff.setChecked(State_serviceOnOff);
                            tv_NeuroskyStatus.setText(NeuroskyStatus);
                        }
                        if(NeuroskyStatus.equals("Neurosky connecting . . .") ){
                            State_serviceOnOff = true;
                            serviceOnOff.setChecked(State_serviceOnOff);
                            tv_NeuroskyStatus.setText(NeuroskyStatus);
                        }
                        if(NeuroskyStatus.equals("Neurosky disconnected") ||
                                NeuroskyStatus.equals("Neurosky not paired") ||
                                NeuroskyStatus.equals("Neurosky was not found")    ){
                            State_serviceOnOff = false; serviceOnOff.setChecked(State_serviceOnOff);
                            ServiceOff();
                        }

                        break;

                    case 2:
                        At = msg.arg1; tv_Att.setText(String.valueOf(At));
                        Med = msg.arg2; tv_Med.setText(String.valueOf(Med));
                        NeuroskyStatus = msg.obj.toString();
                        tv_NeuroskyStatus.setText("Neurosky connected");

                        break;

                    case 3: // service Destroyed
                        tv_Att.setText("-");
                        tv_Med.setText("-");
                        break;

                    case 4: // BT is OFF
                        NeuroskyStatus = msg.obj.toString();
                        tv_NeuroskyStatus.setText(NeuroskyStatus);
                        State_serviceOnOff = false; serviceOnOff.setChecked(State_serviceOnOff);
                        ServiceOff();
                        break;

                    default:
                        break;
                }
            }
        };

        ibStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userMail = etv_mail.getText().toString();
                tv_StartStatus.setText(userMail);
            }
        });

        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userMail = "";
                etv_mail.setText("");
                tv_StartStatus.setText("anonymous user");}
        });

    }

    //start the service
    public void onClickStartServie(View V)
    {
        //start the service from here //eegService is your service class name
        startService(new Intent(this, eegService.class));
        ServiceRunningFlag = "running";

    }
    //Stop the started service
    public void onClickStopService(View V)
    {
        //Stop the running service from here//eegService is your service class name
        //Service will only stop if it is already running.
        stopService(new Intent(this, eegService.class));
        ServiceRunningFlag = "stoped";
    }


    public void onSwitchClickedService(View view) {
        // Is the toggle on?
//	    boolean on = serviceOnOff.isChecked();
        serviceOnOff.setText("");
        State_serviceOnOff = serviceOnOff.isChecked();

        if (State_serviceOnOff) {
            // -- manage icon
            serviceOnOff.setBackgroundResource(R.drawable.service_on);
            serviceOnOff.setTextColor(Color.WHITE);
            serviceOnOff.setText("ON");

            ServiceRunningFlag = "running";

            Intent serviceIntent = new Intent(this, eegService.class);
            serviceIntent.putExtra("UserName", userName);
            serviceIntent.putExtra("UserActivity", userActivity);
            startService(serviceIntent);

            //-- for glass only
//			Intent intent = new Intent(this, ProcessingMindOS.class);
//			startActivity(intent);

        } else { //Service will only stop if it is already running
            serviceOnOff.setBackgroundResource(R.drawable.service_off);
            serviceOnOff.setTextColor(Color.GRAY);
            serviceOnOff.setText("OFF");

            stopService(new Intent(this, eegService.class));
            ServiceRunningFlag = "stoped";
        }

    }

    public void ServiceOff() {
        // -- Service will only stop if it is already running
        stopService(new Intent(this, eegService.class));
        ServiceRunningFlag = "stoped";
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.e("onStop", "MainActivity");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("onPause", "MainActivity");

        // -- service
        SharedPreferences ss1 = getSharedPreferences(tService, 0);
        SharedPreferences.Editor ee1 = ss1.edit();
        ee1.putBoolean(Key_State_serviceOnOff, State_serviceOnOff); ee1.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Log.e("onStart", "MainActivity");

    }

    @Override
    public void onResume() {
        super.onResume();

       // Log.e("onResume", "MainActivity: restore settings");
        // -- service
        SharedPreferences ss1 = getSharedPreferences(tService, 0);
        State_serviceOnOff =  ss1.getBoolean(Key_State_serviceOnOff, false);
        serviceOnOff.setChecked(State_serviceOnOff);

        if (State_serviceOnOff) {
            // -- manage icon
            serviceOnOff.setBackgroundResource(R.drawable.service_on);
            serviceOnOff.setTextColor(Color.WHITE);
            serviceOnOff.setText("ON");
        } else {
            serviceOnOff.setBackgroundResource(R.drawable.service_off);
            serviceOnOff.setTextColor(Color.GRAY);
            serviceOnOff.setText("OFF");
        }

    }

}