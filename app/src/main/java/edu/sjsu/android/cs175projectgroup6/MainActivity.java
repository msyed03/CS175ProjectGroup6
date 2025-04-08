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
    Fragment pronounceThisWord = new PronounceThisWord();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        EdgeToEdge.enable(this);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction
                .replace(R.id.flFragment, wordJump)
                .addToBackStack(null)
                .commit();

        binding.wordJumpFragment.setOnClickListener(this::switchToWordJump);
        binding.pronounceWordFragment.setOnClickListener(this::switchToPronounceWord);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void switchToWordJump(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction
                .replace(R.id.flFragment, wordJump)
                .addToBackStack(null)
                .commit();
        Toast.makeText(this, "Switched to Word Jump", Toast.LENGTH_SHORT).show();
    }

    public void switchToPronounceWord(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction
                .replace(R.id.flFragment, pronounceThisWord)
                .addToBackStack(null)
                .commit();
        Toast.makeText(this, "Switched to Pronounce This Word", Toast.LENGTH_SHORT).show();
    }
}