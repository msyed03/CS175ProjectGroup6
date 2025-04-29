package edu.sjsu.android.cs175projectgroup6;

import android.graphics.Rect;

public class WordJumpPlatform {
    int x, y, width = 200, height = 30;

    public WordJumpPlatform(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }
}
