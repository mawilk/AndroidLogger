package pl.edu.agh.loggerclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ConnectionService connectionService;
    private boolean bound = false;

    private Button mainBtn;
    private boolean isStarted = false;

    private EditText ipEditText;

    private Timer timer;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainBtn = (Button) findViewById(R.id.main_button);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStarted) {
                    start();
                } else {
                    stop();
                }
            }
        });

        ipEditText = (EditText) findViewById(R.id.ipEditText);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!bound) {
            Intent intent = new Intent(MainActivity.this, ConnectionService.class);
            this.bindService(intent, logicConnection, Context.BIND_AUTO_CREATE);
        }

        setButtonLabel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            bound = false;
            this.unbindService(logicConnection);
        }
    }

    private ServiceConnection logicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.ConnectionBinder binder = (ConnectionService.ConnectionBinder) service;
            connectionService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectionService = null;
            bound = false;
        }
    };

    private void start() {
        isStarted = true;
        ipEditText.setEnabled(false);
        ipEditText.setInputType(InputType.TYPE_NULL);

        setButtonLabel();

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();

        String lastLogDate = DateHelper.getLastSavedDateOrDefault(preferences);

        LoggerService.setIp(ipEditText.getText().toString());

        connectionService.startLogging(timer, lastLogDate);
    }

    private void stop() {
        isStarted = false;
        ipEditText.setEnabled(true);
        ipEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        setButtonLabel();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        connectionService.stopLogging();
    }

    private void setButtonLabel() {
        mainBtn.setText(getStateLabel());
    }

    private String getStateLabel() {
        if (isStarted) {
            return "Stop";
        }

        return "Start";
    }
}