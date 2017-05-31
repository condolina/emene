package com.energymeasures.ucheudeh.emene;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class StartUpApp extends AppCompatActivity {

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartUpApp.context = getApplicationContext();
        setContentView(R.layout.activity_start_up_app);
        String message = "Java Serialization - with Normal Read. Uche ";

        TextView textView = new TextView(this);
        textView.setText(message);
        setContentView(textView);
    }
}
