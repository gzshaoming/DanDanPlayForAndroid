package com.xyoye.player.commom.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.xyoye.player.R;
import com.xyoye.player.commom.adapter.StreamAdapter;
import com.xyoye.player.commom.bean.TrackInfoBean;
import com.xyoye.player.commom.utils.CommonPlayerUtils;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.exoplayer2.text.CaptionStyleCompat.EDGE_TYPE_NONE;

/**
 * Created by xyoye on 2019/2/22.
 */

public class SettingSubtitleView extends LinearLayout implements View.OnClickListener {
    //网络字幕
    private RelativeLayout networkSubtitleRl;
    //开关
    private Switch subtitleSwitch;
    //加载状态
    private TextView subtitleLoadStatusTv;
    private EditText subExtraTimeEt;

    //内置字幕设置
    private LinearLayout interSizeLL;
    private SeekBar subtitleOtherSB;
    private TextView subtitleOtherSizeTv;
    private TextView bgTB, bgTW, bgBW, bgWB, bgTT;

    //外置字幕设置
    private LinearLayout outerSizeLL, outerTimeLL;
    private SeekBar subtitleTextSizeSB;
    private TextView subtitleTextSizeTv;

    //字幕流设置
    private LinearLayout subtitleRl;
    private RecyclerView subtitleRv;
    private StreamAdapter subtitleStreamAdapter;

    private boolean isExoPlayer = false;
    //时间偏移量
    private float timeOffset;
    //是否已加载字幕
    private boolean isLoadSubtitle = false;
    //是否显示内置字幕控制View
    private boolean isShowInnerCtrl = false;
    //控制回调
    private SettingSubtitleListener settingListener = null;

    //字幕流数据
    private List<TrackInfoBean> subtitleTrackList;

    public SettingSubtitleView(Context context) {
        this(context, null);
    }

    public SettingSubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_setting_subtitle, this);

        subtitleSwitch = findViewById(R.id.subtitle_sw);
        subtitleLoadStatusTv = findViewById(R.id.subtitle_load_status_tv);
        subtitleTextSizeTv = findViewById(R.id.subtitle_text_size_tv);
        subtitleTextSizeSB = findViewById(R.id.subtitle_text_size_sb);
        subtitleOtherSizeTv = findViewById(R.id.subtitle_other_size_tv);
        subtitleOtherSB = findViewById(R.id.subtitle_other_size_sb);
        subExtraTimeEt = findViewById(R.id.subtitle_extra_time_et);
        networkSubtitleRl = findViewById(R.id.subtitle_network_rl);

        bgBW = findViewById(R.id.inter_bg_black_white);
        bgWB = findViewById(R.id.inter_bg_white_black);
        bgTB = findViewById(R.id.inter_bg_tran_black);
        bgTW = findViewById(R.id.inter_bg_tran_white);
        bgTT = findViewById(R.id.inter_bg_tran_tran);

        interSizeLL = findViewById(R.id.inter_size_ll);
        outerSizeLL = findViewById(R.id.outer_size_LL);
        outerTimeLL = findViewById(R.id.outer_time_LL);

        subtitleRl = findViewById(R.id.subtitle_track_ll);
        subtitleRv = findViewById(R.id.subtitle_track_rv);

        bgBW.setOnClickListener(this);
        bgWB.setOnClickListener(this);
        bgTB.setOnClickListener(this);
        bgTW.setOnClickListener(this);
        bgTT.setOnClickListener(this);

        findViewById(R.id.subtitle_change_source_tv).setOnClickListener(this);
        findViewById(R.id.subtitle_extra_time_add).setOnClickListener(this);
        findViewById(R.id.subtitle_extra_time_reduce).setOnClickListener(this);
        findViewById(R.id.subtitle_network_tv).setOnClickListener(this);

        subExtraTimeEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        subExtraTimeEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        subExtraTimeEt.setSingleLine(true);

        //默认内置字幕背景色为黑+白
        bgBW.setBackgroundColor(CommonPlayerUtils.getResColor(context, R.color.selected_view_bg));

        subtitleTrackList = new ArrayList<>();
        subtitleStreamAdapter = new StreamAdapter(R.layout.item_video_track, subtitleTrackList);
        subtitleStreamAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (isExoPlayer) {
                for (int i = 0; i < subtitleTrackList.size(); i++) {
                    if (i == position)
                        subtitleTrackList.get(i).setSelect(true);
                    else
                        subtitleTrackList.get(i).setSelect(false);
                }
                settingListener.selectTrack(subtitleTrackList.get(position), false);
                subtitleStreamAdapter.notifyDataSetChanged();
            } else {
                //deselectAll except position
                for (int i = 0; i < subtitleTrackList.size(); i++) {
                    if (i == position) continue;
                    settingListener.deselectTrack(subtitleTrackList.get(i), true);
                    subtitleTrackList.get(i).setSelect(false);
                }
                //select or deselect position
                if (subtitleTrackList.get(position).isSelect()) {
                    settingListener.deselectTrack(subtitleTrackList.get(position), true);
                    subtitleTrackList.get(position).setSelect(false);
                } else {
                    settingListener.selectTrack(subtitleTrackList.get(position), true);
                    subtitleTrackList.get(position).setSelect(true);
                }
                subtitleStreamAdapter.notifyDataSetChanged();
            }
        });

        subtitleRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        subtitleRv.setItemViewCacheSize(10);
        subtitleRv.setAdapter(subtitleStreamAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        //切换字幕显示状态
        subtitleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isLoadSubtitle && isChecked) {
                if (interSizeLL.getVisibility() == View.VISIBLE)
                    interSizeLL.setVisibility(GONE);
                outerTimeLL.setVisibility(VISIBLE);
                outerSizeLL.setVisibility(VISIBLE);
            } else {
                if (isShowInnerCtrl)
                    interSizeLL.setVisibility(VISIBLE);
                outerTimeLL.setVisibility(GONE);
                outerSizeLL.setVisibility(GONE);
            }
            settingListener.setSubtitleSwitch(subtitleSwitch, isChecked);
        });

        //设置偏移时间
        subExtraTimeEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    String offset = subExtraTimeEt.getText().toString().trim();
                    timeOffset = Float.valueOf(offset);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "请输入正确的时间", Toast.LENGTH_LONG).show();
                    return true;
                }
                subExtraTimeEt.clearFocus();
                return false;
            }
            return false;
        });

        //字幕文字大小
        subtitleTextSizeSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) progress = 1;
                subtitleTextSizeTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0) progress = 1;
                settingListener.setSubtitleTextSize(progress);
            }
        });

        //内置字幕文字大小
        subtitleOtherSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) progress = 1;
                subtitleOtherSizeTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0) progress = 1;
                settingListener.setInterSubtitleSize(progress);
            }
        });

        this.setOnTouchListener((v, event) -> true);
    }

    public SettingSubtitleView initListener(SettingSubtitleListener settingListener) {
        this.settingListener = settingListener;
        return this;
    }

    public SettingSubtitleView setExoPlayerType() {
        this.isExoPlayer = true;
        return this;
    }

    /**
     * 初始化中文文字大小
     */
    @SuppressLint("SetTextI18n")
    public SettingSubtitleView initSubtitleTextSize(int progress) {
        subtitleTextSizeSB.setMax(100);
        subtitleTextSizeTv.setText(progress + "%");
        subtitleTextSizeSB.setProgress(progress);
        return this;
    }

    /**
     * 是否显示内置字幕控制View，根据字幕流决定
     */
    @SuppressLint("SetTextI18n")
    public void setInnerSubtitleCtrl(boolean hasInnerSubtitle) {
        isShowInnerCtrl = hasInnerSubtitle;
        interSizeLL.setVisibility(isShowInnerCtrl ? VISIBLE : GONE);
        if (isShowInnerCtrl) {
            subtitleOtherSizeTv.setText("50%");
            subtitleOtherSB.setMax(100);
            subtitleOtherSB.setProgress(50);
            setInterBg(3);
        }
    }

    /**
     * 设置内置字幕背景和字体颜色
     */
    public void setInterBg(int type) {
        CaptionStyleCompat compat;

        bgBW.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        bgWB.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        bgTB.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        bgTW.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        bgTT.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        switch (type) {
            case 0:
                bgBW.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                compat = new CaptionStyleCompat(Color.WHITE, Color.BLACK, Color.TRANSPARENT, EDGE_TYPE_NONE, Color.WHITE, null);
                break;
            case 1:
                bgWB.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                compat = new CaptionStyleCompat(Color.BLACK, Color.WHITE, Color.TRANSPARENT, EDGE_TYPE_NONE, Color.WHITE, null);
                break;
            case 2:
                bgTB.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                compat = new CaptionStyleCompat(Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, EDGE_TYPE_NONE, Color.WHITE, null);
                break;
            case 3:
                bgTW.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                compat = new CaptionStyleCompat(Color.WHITE, Color.TRANSPARENT, Color.TRANSPARENT, EDGE_TYPE_NONE, Color.WHITE, null);
                break;
            case 4:
                bgTT.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                compat = new CaptionStyleCompat(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, EDGE_TYPE_NONE, Color.WHITE, null);
                break;
            default:
                bgBW.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                compat = CaptionStyleCompat.DEFAULT;
        }
        settingListener.setInterBackground(compat);
    }

    /**
     * 设置外置字幕加载状态
     */
    public void setSubtitleLoadStatus(boolean isLoad) {
        if (isLoad) {
            subtitleSwitch.setChecked(true);
            subtitleLoadStatusTv.setText("（已加载）");
            subtitleLoadStatusTv.setTextColor(CommonPlayerUtils.getResColor(getContext(), R.color.theme_color));
        } else {
            subtitleSwitch.setChecked(false);
            subtitleLoadStatusTv.setText("（未加载）");
            subtitleLoadStatusTv.setTextColor(CommonPlayerUtils.getResColor(getContext(), R.color.text_red));
        }
    }

    /**
     * 获取偏移时间
     */
    public float getTimeOffset() {
        return timeOffset;
    }

    public boolean isLoadSubtitle() {
        return isLoadSubtitle;
    }

    public void setLoadSubtitle(boolean loadDanmu) {
        isLoadSubtitle = loadDanmu;
    }

    public void setNetworkSubtitleVisible(boolean isShow) {
        networkSubtitleRl.setVisibility(isShow ? VISIBLE : GONE);
    }

    public void setSubtitleTrackList(List<TrackInfoBean> trackList) {
        this.subtitleTrackList.clear();
        this.subtitleTrackList.addAll(trackList);
        this.subtitleStreamAdapter.setNewData(trackList);
        this.subtitleRl.setVisibility(subtitleTrackList.size() < 1 ? GONE : VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.subtitle_change_source_tv) {
            settingListener.setOpenSubtitleSelector();
        } else if (id == R.id.subtitle_extra_time_reduce) {
            timeOffset -= 0.5f;
            subExtraTimeEt.setText(String.valueOf(timeOffset));
        } else if (id == R.id.subtitle_extra_time_add) {
            timeOffset += 0.5f;
            subExtraTimeEt.setText(String.valueOf(timeOffset));
        } else if (id == R.id.inter_bg_black_white) {
            setInterBg(0);
        } else if (id == R.id.inter_bg_white_black) {
            setInterBg(1);
        } else if (id == R.id.inter_bg_tran_black) {
            setInterBg(2);
        } else if (id == R.id.inter_bg_tran_white) {
            setInterBg(3);
        } else if (id == R.id.inter_bg_tran_tran) {
            setInterBg(4);
        } else if (id == R.id.subtitle_network_tv) {
            settingListener.onShowNetworkSubtitle();
        }
    }

    public interface SettingSubtitleListener {
        void selectTrack(TrackInfoBean trackInfoBean, boolean isAudio);

        void deselectTrack(TrackInfoBean trackInfoBean, boolean isAudio);

        void setSubtitleSwitch(Switch switchView, boolean isChecked);

        void setSubtitleTextSize(int progress);

        void setOpenSubtitleSelector();

        void setInterSubtitleSize(int progress);

        void setInterBackground(CaptionStyleCompat compat);

        void onShowNetworkSubtitle();
    }
}
