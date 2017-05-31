package pl.edu.agh.loggerclient;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Wilk.
 */

public class ConnectionService extends Service {

    public static final long SEND_LOG_INTERVAL = 20 * 1000; //30s

    private final IBinder binder = new ConnectionBinder();
    private Timer sendingTimer;
    private boolean running = false;

    private Handler handler = new Handler();

    public class ConnectionBinder extends Binder {
        ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class SendLogTask extends TimerTask {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("ConnectionService", "Sending log");
                    LoggerService.flushLogs(getBaseContext());
                }
            });
        }
    }

    class CollectLogTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String[] params) {
            try {
                Log.d("ConnectionService", "Starting log collection");

                StringBuilder command = new StringBuilder("logcat");
                if(params.length > 0) {
                    command.append(" -s ");
                    command.append(" -v year ");
                    command.append(params[0]);
                }

                Process process = Runtime.getRuntime().exec(command.toString());
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                running = true;
                String line = "";
                while ((line = bufferedReader.readLine()) != null && running) {
                    Log.d("ConnectionService", line);
                    LoggerService.addLog(getBaseContext(), line);
                }
            }
            catch (IOException e) {
                Log.d("ConnectionService", "Collecting logs failed.");
                this.cancel(true);
            }

            return 0;
        }
    }

    public void startLogging() {
        Log.d("ConnectionService", "Logging started");
        new CollectLogTask().execute("FunnyLogs");

        if(sendingTimer != null) {
            sendingTimer.cancel();
        }

        sendingTimer = new Timer();
        sendingTimer.scheduleAtFixedRate(new SendLogTask(), SEND_LOG_INTERVAL, SEND_LOG_INTERVAL);
    }

    public void stopLogging() {
        Log.d("ConnectionService", "Logging stopped");
        running = false;
        sendingTimer.cancel();
    }
}
