package xyz.jdynb.dymovies.view.pager;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class ScaleTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.70f;
    private static final float MIN_ALPHA = 0.5f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position < -1 || position > 1) {
            page.setAlpha(MIN_ALPHA);
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
        } else if (position <= 1) { // [-1,1]
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float scaleX;
            if (position < 0) {
                scaleX = 1 + 0.3f * position;
            } else {
                scaleX = 1 - 0.3f * position;
            }
            page.setScaleX(scaleX);
            page.setScaleY(scaleX);
            page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        }
    }
}
