package pl.edu.agh.loggerclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

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
                if(!isStarted) {
                    start();
                } else {
                    stop();
                }
            }
        });
    }

    private void start() {
        mainBtn.setText("Stop");
        isStarted = true;
    }

    private void stop() {
        mainBtn.setText("Start");
        isStarted = false;
    }
}
