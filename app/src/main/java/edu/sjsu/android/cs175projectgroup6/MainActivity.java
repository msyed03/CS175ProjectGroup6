package edu.sjsu.android.cs175projectgroup6;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.sjsu.android.cs175projectgroup6.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment wordJump = new WordJump();
    private WordJumpGameView gameView;
    Fragment pronounceThisWord = new PronounceThisWord();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1) don't swap in any fragment here
        // 2) hide the fragment container so you just see the two buttons
        binding.flFragment.setVisibility(View.GONE);

        // wire up the two buttons:
        binding.wordJumpFragment.setOnClickListener(this::switchToWordJump);
        binding.pronounceWordFragment.setOnClickListener(this::switchToPronounceWord);
    }

    public void switchToWordJump(View view) {
        // make the container visible the first time:
        binding.flFragment.setVisibility(View.VISIBLE);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flFragment, wordJump)
                .addToBackStack(null)
                .commit();

        Toast.makeText(this, "Starting Word Jump", Toast.LENGTH_SHORT).show();
    }

    public void switchToPronounceWord(View view) {
        binding.flFragment.setVisibility(View.VISIBLE);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flFragment, pronounceThisWord)
                .addToBackStack(null)
                .commit();

        Toast.makeText(this, "Starting Pronounce This Word", Toast.LENGTH_SHORT).show();
    }
}