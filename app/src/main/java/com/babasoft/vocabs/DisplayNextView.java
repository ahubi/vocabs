package com.babasoft.vocabs;

import android.view.animation.Animation;
import android.widget.Button;

public final class DisplayNextView implements Animation.AnimationListener {
    private boolean mCurrentView;
    Button mV;
    
    public DisplayNextView(boolean currentView, Button v) {
        mCurrentView = currentView;
        this.mV = v;
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        mV.post(new SwapViews(mCurrentView,mV));
    }

    public void onAnimationRepeat(Animation animation) {
    }
}