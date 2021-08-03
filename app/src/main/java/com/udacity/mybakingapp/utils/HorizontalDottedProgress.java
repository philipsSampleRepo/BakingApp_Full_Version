package com.udacity.mybakingapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * © 2015 .  This code is distributed pursuant to your  Mobile Application Developer License
 * Agreement and may be used solely in accordance with the terms and conditions set forth therein.
 * provides this software on as "as is", "where is" basis, with all faults known and unknown.
 * makes no warranty, express, statutory or implied, and explicitly disclaims the * *
 * warranties or merchantability, fitness for a particular purpose, any warranty of non-infringement
 * of any third party’s intellectual property rights, any warranty that the licensed * works will
 * meet the requirements of licensee or any other user, any warrantee that the software will be
 * error-free or will operate without interruption, and any warranty that the software will
 * interoperate with any licensee or third party hardware, software or systems.  undertakes
 * no obligation whatsoever to support or maintain all or any part of this software.
 * The software is not fault tolerant and is not designed, intended or authorized for use in any
 * medical, lifesaving or life sustaining systems, or any other application in which the failure
 * of the licensed work could create a situation where personal injury or death may occur.
 * <p>
 * All other rights are reserved.
 **/
public class HorizontalDottedProgress extends View {

    //Bounced Dot Radius
    private final int mBounceDotRadius = 8;

    //to get identified in which position dot has to bounce
    private int mDotPosition;

    //specify how many dots you need in a progressbar
    private final int mDotAmount = 10;

    private Paint paint;

    public HorizontalDottedProgress(Context context) {
        super(context);
    }

    public HorizontalDottedProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalDottedProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //Method to draw your customized dot on the canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

         paint = new Paint();

        //set the color for the dot that you want to draw
        paint.setColor(Color.parseColor("#fd583f"));

        //function to create dot
        createDot(canvas, paint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Animation called when attaching to the window, i.e to your screen
        startAnimation();
    }

    private void createDot(Canvas canvas, Paint paint) {

        //here i have set progress bar with 10 dots , so repeat and when i = mDotPosition  then increase the radius of dot i.e mBounceDotRadius
        for (int i = 0; i < mDotAmount; i++) {
            if (i == mDotPosition) {
                canvas.drawCircle(10 + (i * 20), mBounceDotRadius, mBounceDotRadius, paint);
            } else {
                //actual dot radius
                int mDotRadius = 5;
                canvas.drawCircle(10 + (i * 20), mBounceDotRadius, mDotRadius, paint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width;
        int height;

        //calculate the view width
        width = (20 * 9);
        height = (mBounceDotRadius * 2);

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    private void startAnimation() {
        BounceAnimation bounceAnimation = new BounceAnimation();
        bounceAnimation.setDuration(100);
        bounceAnimation.setRepeatCount(Animation.INFINITE);
        bounceAnimation.setInterpolator(new LinearInterpolator());
        bounceAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                mDotPosition++;
                //when mDotPosition == mDotAmount , then start again applying animation from 0th position , i.e  mDotPosition = 0;
                if (mDotPosition == mDotAmount) {
                    mDotPosition = 0;
                }
                Log.d("INFOMETHOD", "----On Animation Repeat----");

            }
        });
        startAnimation(bounceAnimation);
    }


    private class BounceAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            //call invalidate to redraw your view again.
            invalidate();
        }
    }
}