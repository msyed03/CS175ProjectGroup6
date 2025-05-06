package edu.sjsu.android.cs175projectgroup6;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import androidx.appcompat.app.AlertDialog;

public class WordJumpGameView extends View {
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint platformPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final QuestionManager qm;
    private final Random random = new Random();

    // Option texts and ordering
    private String leftOptionText, middleOptionText, rightOptionText;
    private int[] optionOrder;

    // Platform bitmaps and rects
    private Bitmap cloudBitmap;
    private RectF groundRect;
    private RectF basePlatformRect;
    private RectF leftPlatformRect, middlePlatformRect, rightPlatformRect;
    private Bitmap leftPlatformBitmap, middlePlatformBitmap, rightPlatformBitmap, basePlatformBitmap;

    // Duck animation
    private AnimationDrawable animatedJump;
    private float duckX, duckY;
    private float restingDuckX, restingDuckY;
    private int duckWidth, duckHeight;

    // Animators
    private ValueAnimator idleAnimator;
    private ValueAnimator jumpAnimator;
    private ValueAnimator shiftAnimator;
    private boolean isJumping = false;
    private boolean paused = false;

    // Animation timing
    private long lastFrameTime = 0;
    private int currentFrame = 0;
    private long frameDuration = 100;

    // Layout constants
    private static final float GROUND_HEIGHT   = 100f;
    private static final float PLATFORM_HEIGHT = 220f;
    private static final float PLATFORM_GAP    = 300f;
    private static final float ARC_HEIGHT      = 200f;
    private static final float IDLE_AMPLITUDE  = 30f;

    private CountDownTimer countDownTimer;
    private long                timeLeftMillis = 30000;
    private int                 score          = 0;
    private boolean             gameOver       = false;

    private void initGame() {
        score          = 0;
        timeLeftMillis = 30000;
        gameOver       = false;

        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                invalidate();            // redraw to update timer
            }
            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                gameOver       = true;
                paused         = true;
                showGameOverDialog();
            }
        }.start();
    }

    /** Callback interface to request a restart */
    public interface GameEventListener {
        void onRequestRestart();
    }
    private GameEventListener listener;

    public void setGameEventListener(GameEventListener listener) {
        this.listener = listener;
    }

    public WordJumpGameView(Context ctx) {
        super(ctx);
        qm = new QuestionManager(ctx);
        platformPaint.setColor(0xff64C864);
        textPaint.setColor(0xff000000);
        textPaint.setTextSize(48);
        textPaint.setTextAlign(Paint.Align.CENTER);

        initGame();
    }

    public boolean isPaused() {
        return paused;
    }
    public void pause() {
        paused = true;
        if (idleAnimator != null)  idleAnimator.cancel();
        if (shiftAnimator != null) shiftAnimator.cancel();
        if (jumpAnimator != null)  jumpAnimator.cancel();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    public void resume() {
        paused = false;
        startIdleBounce();
        // restart timer from where we left off
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            public void onTick(long m) { timeLeftMillis = m; invalidate(); }
            public void onFinish() {
                timeLeftMillis = 0; gameOver = true; paused = true;
                showGameOverDialog();
            }
        }.start();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);

        // Cloud bitmap
        Bitmap originalCloud = BitmapFactory.decodeResource(getResources(), R.drawable.cloud);
        int targetWidth = getWidth() - 1;
        float aspectRatio = (float) originalCloud.getHeight() / originalCloud.getWidth();
        int targetHeight = (int) (targetWidth * aspectRatio);
        cloudBitmap = Bitmap.createScaledBitmap(originalCloud, targetWidth, targetHeight, true);

        // Ground & base platform
        groundRect = new RectF(0, h - GROUND_HEIGHT, w, h);
        basePlatformRect = new RectF(groundRect);

        // Duck animation setup
        View dummy = new View(getContext());
        dummy.setBackgroundResource(R.drawable.duck_right_animation);
        animatedJump = (AnimationDrawable) dummy.getBackground();
        animatedJump.setBounds(0,0,animatedJump.getIntrinsicWidth(),animatedJump.getIntrinsicHeight());
        animatedJump.start();
        float desiredH = GROUND_HEIGHT * 3.0f;
        float scale = desiredH / animatedJump.getIntrinsicHeight();
        duckWidth = (int)(animatedJump.getIntrinsicWidth() * scale);
        duckHeight = (int)(animatedJump.getIntrinsicHeight() * scale);

        // Compute option platform positions & texts
        computeAnswerPlatforms(w);
        randomizeOptions();

        // Initial duck position
        restingDuckX = basePlatformRect.centerX() - duckWidth/2f;
        restingDuckY = basePlatformRect.top - duckHeight;
        duckX = restingDuckX;
        duckY = restingDuckY;
        startIdleBounce();

        // Load platform images
        Bitmap rawPlatform = BitmapFactory.decodeResource(getResources(), R.drawable.platform_base);
        float baseW = basePlatformRect.width();
        float baseH = PLATFORM_HEIGHT;
        basePlatformBitmap = Bitmap.createScaledBitmap(rawPlatform, (int)baseW, (int)baseH, true);

        leftPlatformBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tree_branch_left);
        middlePlatformBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tree_branch_middle);
        rightPlatformBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tree_branch_right);

        // SCALE TO MATCH PLATFORM RECTS
        leftPlatformBitmap = Bitmap.createScaledBitmap(
                leftPlatformBitmap,
                (int) leftPlatformRect.width(),
                (int) leftPlatformRect.height(),
                true
        );
        middlePlatformBitmap = Bitmap.createScaledBitmap(
                middlePlatformBitmap,
                (int) middlePlatformRect.width(),
                (int) middlePlatformRect.height(),
                true
        );
        rightPlatformBitmap = Bitmap.createScaledBitmap(
                rightPlatformBitmap,
                (int) rightPlatformRect.width(),
                (int) rightPlatformRect.height(),
                true
        );
    }

    private void computeAnswerPlatforms(int viewWidth) {
        float seg = viewWidth / 3f;
        float topY = basePlatformRect.top - PLATFORM_GAP - PLATFORM_HEIGHT;
        leftPlatformRect   = new RectF(0,       topY, seg,        topY + PLATFORM_HEIGHT);
        middlePlatformRect = new RectF(seg,     topY, seg * 2,    topY + PLATFORM_HEIGHT);
        rightPlatformRect  = new RectF(seg * 2, topY, viewWidth,   topY + PLATFORM_HEIGHT);
    }

    private void randomizeOptions() {
        Question q = qm.getCurrent();
        if (q == null) return;
        // shuffle mapping of 0=A,1=B,2=C to slots
        List<Integer> order = Arrays.asList(0,1,2);
        Collections.shuffle(order);
        optionOrder = new int[]{order.get(0), order.get(1), order.get(2)};
        leftOptionText   = getTextFor(q, optionOrder[0]);
        middleOptionText = getTextFor(q, optionOrder[1]);
        rightOptionText  = getTextFor(q, optionOrder[2]);
    }

    private String getTextFor(Question q, int idx) {
        switch (idx) {
            case 0: return q.getOptionA();
            case 1: return q.getOptionB();
            case 2: return q.getOptionC();
            default: return "";
        }
    }

    private void startIdleBounce() {
        if (idleAnimator != null) idleAnimator.cancel();
        idleAnimator = ValueAnimator.ofFloat(0f,1f);
        idleAnimator.setDuration(800);
        idleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        idleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        idleAnimator.addUpdateListener(anim -> {
            if (!isJumping) {
                float f = (float) anim.getAnimatedValue();
                duckY = restingDuckY - (IDLE_AMPLITUDE * f);
                invalidate();
            }
        });
        idleAnimator.start();
    }

    @Override
    protected void onDraw(Canvas c) {
        if (paused) return;
        super.onDraw(c);
        // draw current score (left) and time remaining (right)
        float yPos = getHeight() * 0.12f;
        c.drawText("Score: " + score,
                getWidth() * 0.2f, yPos, textPaint);
        c.drawText("Time: " + (timeLeftMillis / 1000),
                getWidth() * 0.8f, yPos, textPaint);

        if (cloudBitmap != null) c.drawBitmap(cloudBitmap, 0, 120, null);

        Question q = qm.getCurrent();
        if (q != null) {
            float promptY = getHeight()*0.3f;
            c.drawText(q.getPrompt(), getWidth()/2f, promptY, textPaint);
        }

        // Draw base and answer platforms
        if (basePlatformBitmap != null) c.drawBitmap(basePlatformBitmap,
                basePlatformRect.left, basePlatformRect.top, null);
        if (q == null) return;
        c.drawBitmap(leftPlatformBitmap,   leftPlatformRect.left,   leftPlatformRect.top,   null);
        c.drawBitmap(middlePlatformBitmap, middlePlatformRect.left, middlePlatformRect.top, null);
        c.drawBitmap(rightPlatformBitmap,  rightPlatformRect.left,  rightPlatformRect.top,  null);
        float textY = leftPlatformRect.centerY() -
                (textPaint.descent() + textPaint.ascent())/2f;
        c.drawText(leftOptionText,   leftPlatformRect.centerX(),   textY, textPaint);
        c.drawText(middleOptionText, middlePlatformRect.centerX(), textY, textPaint);
        c.drawText(rightOptionText,  rightPlatformRect.centerX(),  textY, textPaint);

        // Draw duck frame
        c.save();
        c.translate(duckX, duckY);
        c.scale((float)duckWidth/animatedJump.getIntrinsicWidth(),
                (float)duckHeight/animatedJump.getIntrinsicHeight());
        animatedJump.draw(c);
        long now = System.currentTimeMillis();
        if (now - lastFrameTime >= frameDuration) {
            animatedJump.selectDrawable(currentFrame);
            currentFrame = (currentFrame+1) % animatedJump.getNumberOfFrames();
            lastFrameTime = now;
        }
        c.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (paused || isJumping || e.getAction() != MotionEvent.ACTION_DOWN) return true;

        float x = e.getX(), y = e.getY();
        boolean hitL = leftPlatformRect.contains(x,y);
        boolean hitM = middlePlatformRect.contains(x,y);
        boolean hitR = rightPlatformRect.contains(x,y);
        if (!hitL && !hitM && !hitR) return true;

        idleAnimator.cancel();
        isJumping = true;

        float startX = duckX, startY = duckY;
        RectF target;
        int displayIndex;
        if (hitL)      { target = leftPlatformRect;   displayIndex = 0; }
        else if (hitM) { target = middlePlatformRect; displayIndex = 1; }
        else           { target = rightPlatformRect;  displayIndex = 2; }
        float endX = target.centerX() - duckWidth/2f;
        float endY = target.top       - duckHeight;
        int selectedIndex = optionOrder[displayIndex];

        jumpAnimator = ValueAnimator.ofFloat(0f,1f);
        jumpAnimator.setDuration(600);
        jumpAnimator.addUpdateListener(anim -> {
            float f = (float) anim.getAnimatedValue();
            duckX = startX + f*(endX - startX);
            float linY = startY + f*(endY - startY);
            float arc  = 4 * ARC_HEIGHT * f * (1-f);
            duckY = linY - arc;
            invalidate();
        });
        jumpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                boolean correct = qm.answer(selectedIndex);
                if (!correct) {
                    paused = true;
                    showGameOverDialog();
                } else {
                    // correct answer flow
                    score += 50;
                    randomizeOptions();
                    switch(displayIndex) {
                        case 0: basePlatformBitmap = leftPlatformBitmap;   break;
                        case 1: basePlatformBitmap = middlePlatformBitmap; break;
                        default: basePlatformBitmap = rightPlatformBitmap; break;
                    }
                    // start platform shift animation
                    float initT = target.top, initB = target.bottom;
                    float finalT = groundRect.top, finalB = groundRect.bottom;
                    float L = target.left, R = target.right;
                    int vw = getWidth();
                    shiftAnimator = ValueAnimator.ofFloat(0f,1f);
                    shiftAnimator.setDuration(500);
                    shiftAnimator.addUpdateListener(sa -> {
                        float f2 = (float) sa.getAnimatedValue();
                        basePlatformRect = new RectF(L,
                                initT + f2*(finalT - initT),
                                R,
                                initB + f2*(finalB - initB));
                        computeAnswerPlatforms(vw);
                        restingDuckX = basePlatformRect.centerX() - duckWidth/2f;
                        restingDuckY = basePlatformRect.top - duckHeight;
                        duckX = restingDuckX;
                        duckY = restingDuckY;
                        invalidate();
                    });
                    shiftAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator sa) {
                            isJumping = false;
                            startIdleBounce();
                        }
                    });
                    shiftAnimator.start();
                }
            }
        });
        jumpAnimator.start();
        return true;
    }

    private void showGameOverDialog() {
        if (countDownTimer != null) countDownTimer.cancel();

        // persist high‐score
        SharedPreferences prefs = getContext()
                .getSharedPreferences("WordJumpPrefs", Context.MODE_PRIVATE);
        int prevHigh = prefs.getInt("highscore", 0);
        boolean isNew    = score > prevHigh;
        if (isNew) {
            prefs.edit().putInt("highscore", score).apply();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // title
        TextView title = new TextView(getContext());
        title.setText("Game Over");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);
        int pad = (int)(20 * getResources().getDisplayMetrics().density);
        title.setPadding(pad,pad,pad,pad);
        builder.setCustomTitle(title);

        // message
        String msg = (gameOver ? "Time's up!\n" : "Wrong answer!\n")
                + "Your score: " + score
                + (isNew   ? "\n🎉 New High Score!" : "");
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Try Again", (d,w) -> {
                    if (listener!=null) listener.onRequestRestart();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        // center text & button
        TextView tv = dialog.findViewById(android.R.id.message);
        if (tv!=null) tv.setGravity(Gravity.CENTER);
        Button btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (btn!=null) btn.setGravity(Gravity.CENTER);
    }

    private void requestRestart() {
        if (listener != null) listener.onRequestRestart();
    }

    private void handleGameOver() {
        requestRestart();
    }
}