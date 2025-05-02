package edu.sjsu.android.cs175projectgroup6;

import android.graphics.Rect;

public class WordJumpPlayer {
    int x, y, width = 100, height = 100;
    int velocityY = 0;
    int gravity = 1;
    int jumpStrength = -30;
    int moveSpeed = 15;
    int screenX;

    public WordJumpPlayer(int screenX, int screenY) {
        this.screenX = screenX;
        x = screenX / 2 - width / 2;
        y = screenY - 300;
    }

    public void update() {
        velocityY += gravity;
        y += velocityY;
    }

    public void jump() {
        velocityY = jumpStrength;
    }

    public void moveLeft() {
        x -= moveSpeed;
        if (x + width < 0) {
            x = screenX;
        }
    }

    public void moveRight() {
        x += moveSpeed;
        if (x > screenX) {
            x = -width;
        }
    }

    //TODO: Change to drawable and animated bird
    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }
}
