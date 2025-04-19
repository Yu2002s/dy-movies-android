package xyz.jdynb.dymovies.fragment.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import xyz.jdynb.dymovies.R;
import xyz.jdynb.dymovies.activity.SimpleVideoActivity;
import xyz.jdynb.dymovies.activity.VideoPlayActivity;
import xyz.jdynb.dymovies.config.SPConfig;
import xyz.jdynb.dymovies.utils.TimeUtilsKt;
import xyz.jdynb.dymovies.view.player.DongYuPlayer;

/**
 * 播放器设置相关
 */
public class PlayerSettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle bundle, @Nullable String s) {
        setPreferencesFromResource(R.xml.preference_player_setting, s);
    }

    private RecyclerView recyclerView;

    private SeekBarPreference startTime;

    private SeekBarPreference endTime;

    private DongYuPlayer player;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = getPlayer();
    }

    @NonNull
    @Override
    public RecyclerView onCreateRecyclerView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        this.recyclerView = recyclerView;
        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                ViewCompat.setOnApplyWindowInsetsListener(v, null);
                if (recyclerView != null) {
                    int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                    recyclerView.setClipToPadding(false);
                    recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(),
                            recyclerView.getPaddingRight(), bottom);
                }
                return insets;
            }
        });

        Integer colorRes = MaterialColors.getColorOrNull(requireContext(), com.google.android.material.R.attr.colorSurface);

        if (colorRes != null) {
            view.setBackgroundColor(colorRes);
        }

        startTime = findPreference("skip_start_time");
        assert startTime != null;
        endTime = findPreference("skip_end_time");
        assert endTime != null;

        // startTime.setSummary("时间: " + TimeUtilsKt.getTime(startTime.getValue()));
        startTime.setOnPreferenceChangeListener((preference, o) -> {
            long time = (int) o;
            preference.setSummary("时间: " + (TimeUtilsKt.getTime(time)));
            player.setSkipVideoStart((int)o);
            return true;
        });

        // endTime.setSummary("时间: " + TimeUtilsKt.getTime(endTime.getValue()));
        endTime.setOnPreferenceChangeListener((preference, o) -> {
            long time = (int) o;
            preference.setSummary("时间: " + (TimeUtilsKt.getTime(time)));
            player.setSkipVideoEnd((int)time);
            return true;
        });

        SeekBarPreference danmakuLine = findPreference(SPConfig.PLAYER_DANMAKU_LINE);
        assert danmakuLine != null;
        danmakuLine.setOnPreferenceChangeListener((preference, o) -> {
            int line = (int) o;
            if (player != null) {
                player.setDanmakuLine(line);
            }
            return true;
        });

        SeekBarPreference danmakuAlpha = findPreference(SPConfig.PLAYER_DANMAKU_ALPHA);
        assert danmakuAlpha != null;
        danmakuAlpha.setOnPreferenceChangeListener((preference, o) -> {
            int alpha = (int) o;
            if (player != null) {
                player.setDanmakuAlpha(alpha / 10f);
            }
            return true;
        });

        SeekBarPreference danmakuSize = findPreference(SPConfig.PLAYER_DANMAKU_SIZE);
        assert danmakuSize != null;
        danmakuSize.setOnPreferenceChangeListener((preference, o) -> {
            int size = (int) o;
            if (player != null) {
                player.setDanmakuSize(size / 10f);
            }
            return true;
        });

        SeekBarPreference danmakuMargin = findPreference(SPConfig.PLAYER_DANMAKU_MARGIN);
        assert danmakuMargin != null;
        danmakuMargin.setOnPreferenceChangeListener((preference, o) -> {
            int margin = (int) o;
            if (player != null) {
                player.setDanmakuMargin(margin);
            }
            return true;
        });

        SeekBarPreference danmakuSpeed = findPreference(SPConfig.PLAYER_DANMAKU_SPEED);
        assert danmakuSpeed != null;
        danmakuSpeed.setOnPreferenceChangeListener((preference, o) -> {
            int speed = (int) o;
            if (player != null) {
                player.setDanmakuSpeed(speed / 10f);
            }
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player == null) {
            return;
        }
        startTime.setValue(player.getSkipVideoStart());
        endTime.setValue(player.getSkipVideoEnd());
        startTime.setSummary("时间: " + TimeUtilsKt.getTime(startTime.getValue()));
        endTime.setSummary("时间: " + TimeUtilsKt.getTime(endTime.getValue()));
    }

    @Nullable
    private DongYuPlayer getPlayer() {
        FragmentActivity activity = requireActivity();
        if (!(activity instanceof VideoPlayActivity || activity instanceof SimpleVideoActivity)) {
            return null;
        }
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        ViewGroup parentView = (ViewGroup) contentView.getChildAt(0);
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (child instanceof DongYuPlayer) {
                return (DongYuPlayer) child;
            }
        }
        return null;
    }
}
