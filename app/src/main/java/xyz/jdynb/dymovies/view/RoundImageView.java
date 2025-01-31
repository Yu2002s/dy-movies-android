package xyz.jdynb.dymovies.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import xyz.jdynb.dymovies.R;

public class RoundImageView extends AppCompatImageView {

    private final Context context;
    private final Path path = new Path();
    private int radius = 0;

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(@NonNull Context context, @Nullable AttributeSet attrs, int styles) {
        super(context, attrs, styles);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        setRadius(typedArray.getInt(R.styleable.RoundImageView_radius, 0));
        typedArray.recycle();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    private int dp2px(int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        if (radius > 0) {
            path.reset();
            path.addRoundRect(0, 0, w, h, radius, radius, Path.Direction.CW);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (radius > 0) {
            canvas.clipPath(path);
        }
        super.onDraw(canvas);
    }
}