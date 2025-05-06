package edu.sjsu.android.cs175projectgroup6;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageButton backBtn = findViewById(R.id.button_back_profile);
        backBtn.setOnClickListener(v -> finish());

        TextView highScoreView = findViewById(R.id.tvHighScore);
        SharedPreferences prefs = getSharedPreferences(
                "WordJumpPrefs", MODE_PRIVATE);
        int high = prefs.getInt("highscore", 0);
        highScoreView.setText("Word Jump High Score: " + high);
    }
}