package edu.sjsu.android.cs175projectgroup6;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;

public class WordJumpGameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private int screenX, screenY;
    private Paint paint;
    private WordJumpPlayer player;
    private ArrayList<WordJumpPlatform> platforms;
    private SurfaceHolder holder;

    public WordJumpGameView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;
        holder = getHolder();
        paint = new Paint();
        player = new WordJumpPlayer(screenX, screenY);
        platforms = new ArrayList<>();
        generatePlatforms();
    }

    private void generatePlatforms() {
        int y = screenY;
        for (int i = 0; i < 10; i++) {
            platforms.add(new WordJumpPlatform(new Random().nextInt(screenX - 100), y));
            y -= 200;
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    private void update() {
        player.update();

        for (WordJumpPlatform platform : platforms) {
            if (player.velocityY > 0 && player.getRect().intersect(platform.getRect())) {
                player.jump();
            }
        }

        if (player.y < screenY / 2) {
            int dy = (screenY / 2) - player.y;
            player.y = screenY / 2;

            for (WordJumpPlatform platform : platforms) {
                platform.y += dy;
                if (platform.y > screenY) {
                    platform.y = 0;
                    platform.x = new Random().nextInt(screenX - 100);
                }
            }
        }
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.WHITE);

            paint.setColor(Color.BLUE);
            canvas.drawRect(player.getRect(), paint);

            paint.setColor(Color.GREEN);
            for (WordJumpPlatform platform : platforms) {
                canvas.drawRect(platform.getRect(), paint);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(17); // 60 fps
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getX() < screenX / 2) {
            player.moveLeft();
        } else {
            player.moveRight();
        }
        return true;
    }
}
