package com.github.jitnegii.swc.utils;

import android.content.Context;
import android.view.animation.Animation;

import com.github.jitnegii.swc.R;

public class AnimationUtils {

    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;

    private Context context;

    public AnimationUtils(Context context) {
        this.context = context;
    }

    public Animation getRotateOpen() {

        if (rotateOpen == null)
            rotateOpen = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim);
        return rotateOpen;
    }

    public Animation getRotateClose() {
        if (rotateClose == null)
            rotateClose = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim);
        return rotateClose;
    }

    public Animation getFromBottom() {
        if (fromBottom == null)
            fromBottom = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim);
        return fromBottom;
    }

    public Animation getToBottom() {
        if (toBottom == null)
            toBottom = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim);
        return toBottom;
    }
}
