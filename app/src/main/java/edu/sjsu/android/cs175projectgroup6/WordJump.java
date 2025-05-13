package edu.sjsu.android.cs175projectgroup6;

import android.app.AlertDialog;
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
    private AlertDialog pauseDialog;

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

        // show instructions before the game starts
        showInstructionsDialog();
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
                pauseBtn.setImageResource(android.R.drawable.ic_media_pause);
                if (pauseDialog != null && pauseDialog.isShowing()) {
                    pauseDialog.dismiss();
                }
            } else {
                gameView.pause();
                pauseBtn.setImageResource(android.R.drawable.ic_media_play);
                showPauseDialog(pauseBtn);
            }
        });

        // Exit
        ImageButton exitBtn = root.findViewById(R.id.button_exit);
        exitBtn.setOnClickListener(v -> {
            requireActivity().finish();
        });

        return root;
    }

    private void showInstructionsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("How to Play")
                .setMessage("Tap the correct verb conjugation to help the bird jump to the next platform.\n\nIf you choose wrong, the platform breaks and it's game over!\n\nTap pause to take a break.")
                .setCancelable(false)
                .setPositiveButton("Start Game", (dialog, which) -> {
                    gameView.initGame(); // START GAME ONLY AFTER THIS
                })
                .show();
    }

    private void showPauseDialog(ImageButton pauseBtn) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_game_paused, null);
        Button resumeBtn = dialogView.findViewById(R.id.button_resume);

        pauseDialog = new AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Resume button resumes the game and dismisses the dialog
        resumeBtn.setOnClickListener(v -> {
            gameView.resume();
            pauseBtn.setImageResource(android.R.drawable.ic_media_pause);
            pauseDialog.dismiss();
        });

        if (pauseDialog.getWindow() != null) {
            pauseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        pauseDialog.show();
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