package pl.edu.agh.loggerclient;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * Created by Wilk.
 */

public class ConnectionService extends Service {

    public static final long SEND_LOG_INTERVAL = 30 * 1000;
    public static final String TAG = "ConnectionService";

    private final IBinder binder = new ConnectionBinder();
    private boolean running = false;

    private Handler handler = new Handler();

    Pattern batteryPattern = Pattern.compile("BatteryService: level:");
    Pattern screenPattern = Pattern.compile("PowerManagerService: ((Going to sleep)|(Waking up))");
    Pattern connectionPattern = Pattern.compile("ConnectivityService: notifyType CAP_CHANGED for NetworkAgentInfo");

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
                    Log.d(TAG, "Sending log");
                    LoggerService.flushLogs(getBaseContext());
                }
            });
        }
    }

    class CollectLogTask extends AsyncTask<String, Void, Integer> {

        private String dateString;

        public CollectLogTask(String date) {
            this.dateString = date;
        }

        @Override
        protected Integer doInBackground(String[] params) {
            try {
                Log.d(TAG, "Starting log collection");

                String[] command = new String[]{"logcat", "-T", dateString, "-s", TextUtils.join(" ", params)};

                Process process = Runtime.getRuntime().exec(command);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                running = true;
                String line = "";
                while ((line = bufferedReader.readLine()) != null && running) {
                    if (screenPattern.matcher(line).find() || batteryPattern.matcher(line).find() || connectionPattern.matcher(line).find()) {
                        Log.d(TAG, line);
                        LoggerService.addLog(getBaseContext(), line);
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "Collecting logs failed.");
                this.cancel(true);
            }

            return 0;
        }
    }

    public void startLogging(Timer timer, String dateString) {
        Log.d(TAG, "Logging started");

        new CollectLogTask(dateString).execute("BatteryService", "ConnectivityService", "PowerManagerService");

        timer.scheduleAtFixedRate(new SendLogTask(), SEND_LOG_INTERVAL, SEND_LOG_INTERVAL);
    }

    public void stopLogging() {
        Log.d(TAG, "Logging stopped");
        running = false;
    }
}
