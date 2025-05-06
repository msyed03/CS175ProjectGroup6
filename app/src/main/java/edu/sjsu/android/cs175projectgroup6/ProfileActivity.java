package edu.sjsu.android.cs175projectgroup6;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageButton backBtn = findViewById(R.id.button_back_profile);
        backBtn.setOnClickListener(v -> finish());

        TextView highScoreView = findViewById(R.id.tvHighScore);
        ImageView duckImage = findViewById(R.id.duckGrowth);

        SharedPreferences prefs = getSharedPreferences("WordJumpPrefs", MODE_PRIVATE);
        int high = prefs.getInt("highscore", 0);
        highScoreView.setText("Word Jump High Score: " + high);

        // Set duck image based on high score
        int duckImageRes;
        if (high < 100) {
            duckImageRes = R.drawable.duck_growth_1;
        } else if (high < 200) {
            duckImageRes = R.drawable.duck_growth_2;
        } else if (high < 400) {
            duckImageRes = R.drawable.duck_growth_3;
        } else if (high < 600) {
            duckImageRes = R.drawable.duck_growth_4;
        } else if (high < 800) {
            duckImageRes = R.drawable.duck_growth_5;
        } else {
            duckImageRes = R.drawable.duck_growth_6;
        }

        duckImage.setImageResource(duckImageRes);
    }
}