package com.example.notification_sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class nitification extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nitification);
            textView = findViewById(R.id.txt1);
            String data = getIntent().getStringExtra("data");
            textView.setText(data);
    }
}