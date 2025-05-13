package edu.sjsu.android.cs175projectgroup6;

public class Question {
    private final String prompt;
    private final String optionA;
    private final String optionB;
    private final String optionC;
    private final int correctOption; // 0 = A, 1 = B, 2 = C

    public Question(String prompt,
                    String optionA,
                    String optionB,
                    String optionC,
                    int correctOption) {
        this.prompt       = prompt;
        this.optionA      = optionA;
        this.optionB      = optionB;
        this.optionC      = optionC;
        this.correctOption= correctOption;
    }

    public String getPrompt()   { return prompt; }
    public String getOptionA()  { return optionA; }
    public String getOptionB()  { return optionB; }
    public String getOptionC()  { return optionC; }
    public int    getCorrectOption() { return correctOption; }
}
