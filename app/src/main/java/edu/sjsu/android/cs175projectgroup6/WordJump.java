package edu.sjsu.android.cs175projectgroup6;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

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
        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        gameView = new WordJumpGameView(requireContext());

        return gameView;
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_word_jump, container, false);
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
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.setReorderingAllowed(true);
        ft.replace(R.id.wordJumpFragment, new WordJump());
        ft.commit();
    }
}