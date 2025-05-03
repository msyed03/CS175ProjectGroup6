package edu.sjsu.android.cs175projectgroup6;

public class Question {
    private final String prompt;
    private final String optionA;
    private final String optionB;
    private final int correctOption; // 0 = A, 1 = B

    public Question(String prompt, String optionA, String optionB, int correctOption) {
        this.prompt = prompt;
        this.optionA = optionA;
        this.optionB = optionB;
        this.correctOption = correctOption;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public int getCorrectOption() {
        return correctOption;
    }
}
