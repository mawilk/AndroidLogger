package pl.edu.agh.loggerclient;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Wilk.
 */

public class ConnectionService extends Service {

    public static final long SEND_LOG_INTERVAL = 10 * 1000; //30s

    private final IBinder binder = new ConnectionBinder();
    private Handler handler = new Handler();
    private Timer timer;

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
                    Intent intent = new Intent(getBaseContext(), LoggerService.class);
                    intent.setAction(LoggerService.ACTION_LOG);
                    intent.putExtra(LoggerService.EXTRA_LOG, "Apr 25 12:12:12 host user: message");
                    startService(intent);
                }
            });
        }
    }

    public void startLogging() {
        Log.d("ConnectionService", "Logging started");
        timer = new Timer();
        timer.scheduleAtFixedRate(new SendLogTask(), 0, SEND_LOG_INTERVAL);
    }

    public void stopLogging() {
        Log.d("ConnectionService", "Logging stopped");
        timer.cancel();
    }
}
