package edu.sjsu.android.cs175projectgroup6;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WordJump#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordJump extends Fragment implements WordJumpGameView.GameEventListener {
    private WordJumpGameView gameView;
    public WordJump() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate our new layout
        View root = inflater.inflate(R.layout.fragment_word_jump, container, false);

        // 1) add the GameView into the container
        FrameLayout gameContainer = root.findViewById(R.id.game_container);
        gameView = new WordJumpGameView(requireContext());
        gameView.setGameEventListener(this);
        gameContainer.addView(gameView);

        //Pause
        ImageButton pauseBtn = root.findViewById(R.id.button_pause);
        pauseBtn.setOnClickListener(v -> {
            if (gameView.isPaused()) {
                gameView.resume();
                // back to pause icon
                pauseBtn.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                gameView.pause();
                // show play icon
                pauseBtn.setImageResource(android.R.drawable.ic_media_play);
            }
        });

        // Exit
        ImageButton exitBtn = root.findViewById(R.id.button_exit);
        exitBtn.setOnClickListener(v -> {
            requireActivity().finish();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        //gameView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //gameView.pause();
    }

    @Override
    public void onRequestRestart() {
        // replace the FL container with a fresh fragment
        FragmentTransaction ft = requireActivity()
                .getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, new WordJump());
        ft.commit();
    }
}