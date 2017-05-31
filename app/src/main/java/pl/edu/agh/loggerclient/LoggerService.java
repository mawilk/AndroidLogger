package pl.edu.agh.loggerclient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.net.*;

/**
 * Created by zivsegal on 4/3/14.
 * Modified by wilk
 */
public class LoggerService extends IntentService{
    private static String TAG = "LoggerService";

    public enum LogMod {
        silent,
        active
    }
    private static LogMod mode = LogMod.silent;

    public static final String ACTION_LOG = "LOGGER_SERVICE_ACTION_LOG";
    public static final String ACTION_SET_MODE = "LOGGER_SERVICE_ACTION_SET_MODE";
    public static final String ACTION_FLUSH = "LOGGER_SERVICE_ACTION_FLUSH_MODE";

    public static final String EXTRA_LOG = "EXTRA_LOG";
    public static final String EXTRA_MODE = "EXTRA_MODE";

    private static final String LOGSTASH_SERVER_URL = "http://192.168.0.182"; // SET PROPER URL
    private static final int LOGSTASH_UDP_JSON_PORT = 5000;
    private static final String LOGSTASH_FILE= "logstash_logs";

    public LoggerService() {
        super("LoggerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Start this service to perform a writing action with the given parameters. If
     * the service is already performing a task this action will be queued.*
     *
     * @param context
     * @param log the log row to be written
     */
    public static void addLog(Context context, String log) {
        Intent intent = new Intent(context, LoggerService.class);
        intent.setAction(ACTION_LOG);
        intent.putExtra(EXTRA_LOG, log);

        context.startService(intent);
    }

    /**
     * Start this service to change the way the service behaves. If
     * the service is already performing a task this action will be queued.
     *
     * @param context
     * @param newMode the new mode ordinal to be set
     */
    public static void changeMode(Context context, LogMod newMode) {
        Intent intent = new Intent(context, LoggerService.class);
        intent.setAction(ACTION_SET_MODE);
        intent.putExtra(EXTRA_MODE, newMode.ordinal());

        context.startService(intent);
    }

    /**
     * Start service to flush existing logfile.
     * If the service is already performing a task this action will be queued.
     * @param context
     */
    public static void flushLogs(Context context) {
        Intent intent = new Intent(context, LoggerService.class);
        intent.setAction(ACTION_FLUSH);

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (action != null) {
            if (action.equalsIgnoreCase(ACTION_LOG)) {
                String log = intent.getStringExtra(EXTRA_LOG);
                if (TextUtils.isEmpty(log)) return;
                Log.d(TAG, "mode:"+this.mode+". got log:"+log);

                switch(this.mode){
                    case silent:
                        writeLogToFile(log);
                        break;
                    case active:
                        sendLogToServer(log);
                        break;
                    default:
                        break;
                }
            } else if (action.equalsIgnoreCase(ACTION_FLUSH)) {
                flushLogToServer();
            } else if (action.equalsIgnoreCase(ACTION_SET_MODE)) {
                int newMode = intent.getIntExtra(EXTRA_MODE, LogMod.silent.ordinal());
                setLogMode(LogMod.values()[newMode]);
            }
        }
    }

    private void sendLogToServer(String logStr) {
        if (logStr == null) return;
        DatagramSocket socket;
        InetAddress host;
        try {
            socket = new DatagramSocket();
            if (socket == null) return;
            host = InetAddress.getByName(new URL(LOGSTASH_SERVER_URL).getHost());
        } catch (SocketException e) {
            Log.d(TAG, "couldn't send log:"+e.toString());
            return;
        } catch (UnknownHostException e) {
            Log.d(TAG, "couldn't send log:"+e.toString());
            return;
        } catch (MalformedURLException e) {
            Log.d(TAG, "couldn't send log:"+e.toString());
            return;
        }

        int msg_length = logStr.length();
        byte []message = logStr.getBytes();
        if (host != null) {
            DatagramPacket p = new DatagramPacket(message, msg_length, host, LOGSTASH_UDP_JSON_PORT);
            try {
                socket.send(p);
            } catch (IOException e) {
                Log.d(TAG, "couldn't send:"+e.toString());
                return;
            }
        }
    }

    private void writeLogToFile(String log) {
        String fileName = LOGSTASH_FILE;
        BufferedWriter bw = null;
        try {
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_APPEND);
            DataOutputStream out = new DataOutputStream(outputStream);
            bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write(log);
            bw.newLine();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "couldn't write log:"+e.toString());
        } catch (IOException e) {
            Log.d(TAG, "couldn't write log:"+e.toString());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    Log.d(TAG, "failed to close BufferedWriter:"+e.toString());
                }
            }
        }
    }

    private void setLogMode(LogMod newMode) {
        if (newMode == this.mode) return;
        LogMod oldMode = this.mode;
        this.mode = newMode;
        if (oldMode == LogMod.silent && newMode == LogMod.active) {
            // activating the logging, send all the accumulated logs
            flushLogToServer();
        }
    }

    private void flushLogToServer() {
        String fileName = LOGSTASH_FILE;
        sendLogFile(fileName);
        deleteFile(fileName);
    }

    /**
     * Sends a log file to the server, line by line - each line is a separate log.
     * @param fileName log file name
     */
    private void sendLogFile(String fileName) {
        FileInputStream fstream;
        try {
            fstream = openFileInput(fileName);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "couldn't open log file" + e.toString());
            return;
        }
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            String log = "";
            while ((log = br.readLine()) != null) {
                sendLogToServer(log);
            }
        } catch (IOException e) {
            Log.d(TAG, "couldn't send log to server:" + e.toString());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                Log.d(TAG, "Failed to close BufferedReader:" + e.toString());
            }
        }
    }
}