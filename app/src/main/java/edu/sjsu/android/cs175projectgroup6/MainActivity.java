package edu.sjsu.android.cs175projectgroup6;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import edu.sjsu.android.cs175projectgroup6.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final Fragment wordJump = new WordJump();
    private final Fragment pronounceThisWord = new PronounceThisWord();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.wordJumpFragment.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordJumpActivity.class);
            startActivity(intent);
        });

        binding.pronounceWordFragment.setOnClickListener(v -> {
            Intent intent = new Intent(this, PronounceThisWordActivity.class);
            startActivity(intent);
        });

//        // Hide fragment container until a game is selected
//        binding.flFragment.setVisibility(View.GONE);
//
//        // Set up button click listeners
//        binding.wordJumpFragment.setOnClickListener(v -> launchGameFragment(wordJump, "Starting Word Jump"));
//        binding.pronounceWordFragment.setOnClickListener(v -> launchGameFragment(pronounceThisWord, "Starting Pronounce This Word"));
    }

//    private void launchGameFragment(Fragment fragment, String toastMessage) {
//        // Show the fragment container
//        binding.flFragment.setVisibility(View.VISIBLE);
//
//        // Replace current fragment
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.flFragment, fragment)
//                .addToBackStack(null)
//                .commit();
//
//        // Show user feedback
//        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
//    }
}
