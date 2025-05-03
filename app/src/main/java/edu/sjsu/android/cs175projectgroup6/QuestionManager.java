package edu.sjsu.android.cs175projectgroup6;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


public class QuestionManager {
    private final List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;

    public QuestionManager(Context ctx) {
        loadFromAssets(ctx);
        Collections.shuffle(questions);    // randomize question order
    }

    private void loadFromAssets(Context ctx) {
        try (InputStream is = ctx.getAssets().open("questions.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String prompt = parts[0];
                    String optA   = parts[1];
                    String optB   = parts[2];
                    int correct   = Integer.parseInt(parts[3]);
                    questions.add(new Question(prompt, optA, optB, correct));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Returns the current question, or null if none loaded */
    public Question getCurrent() {
        if (questions.isEmpty()) return null;
        return questions.get(currentIndex);
    }

    /**
     * Submit an answer (0 or 1).
     * @return true if correct (advances to next), false if wrong (no advance)
     */
    public boolean answer(int selectedOption) {
        Question q = getCurrent();
        if (q == null) return false;
        boolean correct = (selectedOption == q.getCorrectOption());
        if (correct) advance();
        return correct;
    }

    private void advance() {
        currentIndex++;
        if (currentIndex >= questions.size()) {
            currentIndex = 0;
        }
    }
}