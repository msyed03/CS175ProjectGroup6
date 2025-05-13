package edu.sjsu.android.cs175projectgroup6;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private int total = 0;
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

//        TextView pronounceScoreView = findViewById(R.id.tvPronounceHighScore);
//        SharedPreferences pronouncePrefs = getSharedPreferences("PronouncePrefs", MODE_PRIVATE);
//        int pronounceHigh = pronouncePrefs.getInt("highscore", 0);
//        pronounceScoreView.setText("Pronounce This Word High Score: " + pronounceHigh);
//
//        total = high + pronounceHigh;
        total = high;

        // Set duck image based on high score
        int duckImageRes;
        if (total < 100) {
            duckImageRes = R.drawable.duck_growth_1;
        } else if (total < 200) {
            duckImageRes = R.drawable.duck_growth_2;
        } else if (total < 400) {
            duckImageRes = R.drawable.duck_growth_3;
        } else if (total < 600) {
            duckImageRes = R.drawable.duck_growth_4;
        } else if (total < 800) {
            duckImageRes = R.drawable.duck_growth_5;
        } else {
            duckImageRes = R.drawable.duck_growth_6;
        }

        duckImage.setImageResource(duckImageRes);
    }
}