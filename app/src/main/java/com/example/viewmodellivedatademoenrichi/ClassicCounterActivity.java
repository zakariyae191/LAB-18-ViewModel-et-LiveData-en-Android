package com.example.viewmodellivedatademoenrichi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ClassicCounterActivity extends AppCompatActivity {

    private static final String COUNT_KEY = "count_key";


    private int count = 0;

    private TextView tvCount;
    private Button btnIncrement;
    private Button btnDecrement;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCount = findViewById(R.id.tvCount);
        btnIncrement = findViewById(R.id.btnIncrement);
        btnDecrement = findViewById(R.id.btnDecrement);
        btnReset = findViewById(R.id.btnReset);


        if (savedInstanceState != null) {
            count = savedInstanceState.getInt(COUNT_KEY, 0);
        }

        btnIncrement.setOnClickListener(v -> {
            count++;
            updateUI();
        });

        btnDecrement.setOnClickListener(v -> {
            count--;
            updateUI();
        });

        btnReset.setOnClickListener(v -> {
            count = 0;
            updateUI();
        });

        updateUI();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(COUNT_KEY, count);
    }

    private void updateUI() {
        tvCount.setText(String.valueOf(count));
    }
}
