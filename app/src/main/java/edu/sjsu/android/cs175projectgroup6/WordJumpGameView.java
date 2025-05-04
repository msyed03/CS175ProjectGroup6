package edu.sjsu.android.cs175projectgroup6;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;

public class WordJumpGameView extends View {
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint platformPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final QuestionManager qm;
    private final Random random = new Random();
    private boolean swapOptions;
    private String leftOptionText, rightOptionText;
    private Bitmap cloudBitmap;
    private RectF groundRect;            // fixed reference “ground”
    private RectF basePlatformRect;      // current platform the ball bounces on
    private RectF leftPlatformRect, rightPlatformRect;
    private Bitmap leftPlatformBitmap, rightPlatformBitmap, basePlatformBitmap;
    private float duckX, duckY;
    private float restingDuckX, restingDuckY;
    private AnimationDrawable animatedJump;
    private int duckWidth, duckHeight;
    private ValueAnimator idleAnimator, jumpAnimator, shiftAnimator;
    private boolean isJumping = false;

    private long lastFrameTime = 0;
    private int currentFrame = 0;
    private long frameDuration = 100;

    private static final float GROUND_HEIGHT   = 80f;
    private static final float PLATFORM_HEIGHT = 500f;
    private static final float PLATFORM_GAP    = 300f;
    private static final float ARC_HEIGHT      = 200f;
    private static final float IDLE_AMPLITUDE  = 30f;


    public WordJumpGameView(Context ctx) {
        super(ctx);
        qm = new QuestionManager(ctx);

        // Set background drawable
//        setBackgroundResource(R.drawable.cloud);

        platformPaint.setColor(0xff64C864); // rgb(100,200,100)
        textPaint.setColor(0xff000000);
        textPaint.setTextSize(48);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);

        // CLOUD
        Bitmap originalCloud = BitmapFactory.decodeResource(getResources(), R.drawable.cloud);
        int targetWidth = getWidth() - 1;
        float aspectRatio = (float) originalCloud.getHeight() / originalCloud.getWidth();
        int targetHeight = (int) (targetWidth * aspectRatio);
        cloudBitmap = Bitmap.createScaledBitmap(originalCloud, targetWidth, targetHeight, true);

        // GROUND
        groundRect = new RectF(0, h - GROUND_HEIGHT, w, h);
        basePlatformRect = new RectF(groundRect);

        // DUCK
        ImageView dummyImageView = new ImageView(getContext());
        dummyImageView.setBackgroundResource(R.drawable.duck_right_animation);
        animatedJump = (AnimationDrawable) dummyImageView.getBackground();
        animatedJump.setBounds(0, 0, animatedJump.getIntrinsicWidth(), animatedJump.getIntrinsicHeight());
        animatedJump.start();

        float desiredH = GROUND_HEIGHT * 3.0f;
        float scale = desiredH / animatedJump.getIntrinsicHeight();
        duckWidth = (int)(animatedJump.getIntrinsicWidth() * scale);
        duckHeight = (int)(animatedJump.getIntrinsicHeight() * scale);

        animatedJump.setBounds(0, 0, animatedJump.getIntrinsicWidth(), animatedJump.getIntrinsicHeight());
        animatedJump.start();

        computeAnswerPlatforms(w);
        randomizeOptions();  // set initial option placement

        restingDuckX = basePlatformRect.centerX() - duckWidth / 2f;
        restingDuckY = basePlatformRect.top - duckHeight;
        duckX = restingDuckX;
        duckY = restingDuckY;

        startIdleBounce();

        // Load platform images
        Bitmap rawPlatform = BitmapFactory.decodeResource(getResources(), R.drawable.platform_base);
        float width = basePlatformRect.width();    // or fixed width
        float height = PLATFORM_HEIGHT;
        basePlatformBitmap = Bitmap.createScaledBitmap(rawPlatform, (int)width, (int)height, true);

        leftPlatformBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tree_branch_left);
        rightPlatformBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tree_branch_right);

        // Scale images to match the platform rects
        // however we have to make sure they're already initialized

        leftPlatformBitmap = Bitmap.createScaledBitmap(
                leftPlatformBitmap,
                (int) leftPlatformRect.width(),
                (int) leftPlatformRect.height(),
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
        float m = viewWidth * 0.10f;
        float pw= viewWidth * 0.35f;
        float topY = basePlatformRect.top - PLATFORM_GAP - PLATFORM_HEIGHT;

        leftPlatformRect  = new RectF(0, topY, m+pw,        topY+PLATFORM_HEIGHT);
        rightPlatformRect = new RectF(viewWidth-m-pw, topY,
                viewWidth,    topY+PLATFORM_HEIGHT);
    }

    private void randomizeOptions() {
        Question q = qm.getCurrent();
        if (q == null) return;
        swapOptions = random.nextBoolean();
        if (!swapOptions) {
            leftOptionText  = q.getOptionA();
            rightOptionText = q.getOptionB();
        } else {
            leftOptionText  = q.getOptionB();
            rightOptionText = q.getOptionA();
        }
    }

    private void startIdleBounce() {
        if (idleAnimator != null) idleAnimator.cancel();
        idleAnimator = ValueAnimator.ofFloat(0f,1f);
        idleAnimator.setDuration(800);
        idleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        idleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        idleAnimator.addUpdateListener(a -> {
            if (!isJumping) {
                float f = (float)a.getAnimatedValue();
                duckY = restingDuckY - (IDLE_AMPLITUDE * f);
                invalidate();
            }
        });
        idleAnimator.start();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        if (cloudBitmap != null) {
            c.drawBitmap(cloudBitmap, 0, 120, null);
        }

        Question q = qm.getCurrent();
        if (q != null) {
            float promptY = getHeight() * 0.3f;
            c.drawText(q.getPrompt(), getWidth()/2f, promptY, textPaint);
        }

        // draw base platform
//        c.drawRect(basePlatformRect, platformPaint);
//        c.drawBitmap(basePlatformBitmap, basePlatformBitmap.left, basePlatformBitmap.top, null);
        if (q == null) return;

        // Draw left and right platforms
//        c.drawRect(leftPlatformRect,  platformPaint);
//        c.drawRect(rightPlatformRect, platformPaint);
        c.drawBitmap(leftPlatformBitmap, leftPlatformRect.left, leftPlatformRect.top, null);
        c.drawBitmap(rightPlatformBitmap, rightPlatformRect.left, rightPlatformRect.top, null);

        float textY = leftPlatformRect.centerY()
                - (textPaint.descent() + textPaint.ascent())/2f;
        // Draw randomized option texts
        c.drawText(leftOptionText,  leftPlatformRect.centerX(),  textY, textPaint);
        c.drawText(rightOptionText, rightPlatformRect.centerX(), textY, textPaint);

        c.save();
        c.translate(duckX, duckY);
        c.scale((float)duckWidth / animatedJump.getIntrinsicWidth(),
                (float)duckHeight / animatedJump.getIntrinsicHeight());
        animatedJump.draw(c);

        // Frames for the animation
        long now = System.currentTimeMillis();

        if (now - lastFrameTime >= frameDuration) {
            animatedJump.selectDrawable(currentFrame);
            currentFrame = (currentFrame + 1) % animatedJump.getNumberOfFrames();
            lastFrameTime = now;
        }
        c.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (isJumping || e.getAction() != MotionEvent.ACTION_DOWN) return true;

        float x = e.getX(), y = e.getY();
        boolean hitL = leftPlatformRect.contains(x,y);
        boolean hitR = rightPlatformRect.contains(x,y);
        if (!hitL && !hitR) return true;

        idleAnimator.cancel();
        isJumping = true;

        float startX = duckX, startY = duckY;
        RectF target = hitL ? leftPlatformRect : rightPlatformRect;
        float endX = target.centerX() - duckWidth/2f;
        float endY = target.top       - duckHeight;

        // Map tap to the correct question index based on swap
        int selectedIndex = hitL
                ? (swapOptions ? 1 : 0)
                : (swapOptions ? 0 : 1);

        jumpAnimator = ValueAnimator.ofFloat(0f,1f);
        jumpAnimator.setDuration(600);
        jumpAnimator.addUpdateListener(a -> {
            float f = (float)a.getAnimatedValue();
            duckX = startX + f*(endX - startX);
            float linY = startY + f*(endY - startY);
            float arc  = 4 * ARC_HEIGHT * f * (1 - f);
            duckY = linY - arc;
            invalidate();
        });
        jumpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                boolean correct = qm.answer(selectedIndex);
                if (!correct) {
                    isJumping = false;
                    duckX = restingDuckX;
                    duckY = restingDuckY;
                    startIdleBounce();
                    return;
                }

                // Prepare next question options
                randomizeOptions();

                float initT = target.top, initB = target.bottom;
                float finalT= groundRect.top, finalB = groundRect.bottom;
                float L = target.left, R = target.right;
                int vw = getWidth();

                shiftAnimator = ValueAnimator.ofFloat(0f,1f);
                shiftAnimator.setDuration(500);
                shiftAnimator.addUpdateListener(sa -> {
                    float f2 = (float)sa.getAnimatedValue();
                    float newT = initT + f2*(finalT - initT);
                    float newB = initB + f2*(finalB - initB);
                    basePlatformRect = new RectF(L, newT, R, newB);

                    computeAnswerPlatforms(vw);

                    restingDuckX = basePlatformRect.centerX() - duckWidth/2f;
                    restingDuckY = basePlatformRect.top       - duckHeight;
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
        });
        jumpAnimator.start();
        return true;
    }
}