package pl.edu.agh.loggerclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private ConnectionService connectionService;
    private boolean bound = false;

    private Button mainBtn;
    private boolean isStarted = false;

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
    }

    @Override
    protected void onStart() {
        Log.d("MainActivity", "started");
        super.onStart();

        if(!bound) {
            Intent intent = new Intent(MainActivity.this, ConnectionService.class);
            this.bindService(intent, logicConnection, Context.BIND_AUTO_CREATE);
            Log.d("MainActivity", "service binded");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(bound) {
            bound = false;
            this.unbindService(logicConnection);
            Log.d("MainActivity", "service stopped");
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
        mainBtn.setText("Stop");
        isStarted = true;
        connectionService.startLogging();
    }

    private void stop() {
        mainBtn.setText("Start");
        isStarted = false;
        connectionService.stopLogging();
    }
}
