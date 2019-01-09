package com.example.macintosh.simpleexoplayerexampleapilevel;

import android.app.Dialog;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    Dialog mFullScreenDialog;
    boolean mExoPlayerFullscreen = false;
    ImageView mFullScreenIcon;
    private FrameLayout mFullScreenButton;

    SimpleExoPlayer player;
    PlayerView mPlayerView;

    int currentWindow;
    long playBackPosition;
    private boolean autoPlay = false;

    // constant fields for saving and restoring bundle
    public static final String AUTOPLAY = "autoplay";
    public static final String CURRENT_WINDOW_INDEX = "current_window_index";
    public static final String PLAYBACK_POSITION = "playback_position";

    private final String PATH = "file:///android_asset/videoplayback.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayerView = findViewById(R.id.playerview);
        initFullscreenDialog();
        if(savedInstanceState!= null){
            Log.e("ON_CREATE","saveinstance");
            currentWindow = savedInstanceState.getInt(CURRENT_WINDOW_INDEX);
            playBackPosition = savedInstanceState.getLong(PLAYBACK_POSITION);
            autoPlay = savedInstanceState.getBoolean(AUTOPLAY);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mExoPlayerFullscreen = savedInstanceState.getBoolean("fullscreen");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mExoPlayerFullscreen){
            initFullscreenDialog();
        }
        initFullscreenButton();
        initialisePlayer();
        Log.e("ON_RESUME","On resume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        autoPlay = false;
        mExoPlayerFullscreen = getIntent().getBooleanExtra("intent",mExoPlayerFullscreen);
    }

    private void initialisePlayer(){
        Uri uri = Uri.parse(PATH);

        //create exoplayer object
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this,trackSelector);

        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource);
        //attach the player to the view
        mPlayerView.setPlayer(player);
        player.seekTo(currentWindow,playBackPosition);
        player.setPlayWhenReady(autoPlay);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            openFullscreenDialog();
        }

    }

    private MediaSource buildMediaSource(Uri uri) {
        DefaultExtractorsFactory extractorSourceFactory = new DefaultExtractorsFactory();
//        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("ua");

        String userAgent = Util.getUserAgent(this, getApplicationContext().getApplicationInfo().packageName);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent);

//        ExtractorMediaSource audioSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorSourceFactory, null, null);
        // this return a single mediaSource object. i.e. no next, previous buttons to play next/prev media file
        return new ExtractorMediaSource(uri, dataSourceFactory, extractorSourceFactory, null, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("ON_SAVE","before");
        if(player==null){
            Log.e("ON_SAVE","ONSAVE");
            outState.putInt(CURRENT_WINDOW_INDEX,currentWindow);
            outState.putLong(PLAYBACK_POSITION,playBackPosition);
            outState.putBoolean(AUTOPLAY,autoPlay);
            getIntent().putExtra("intent",mExoPlayerFullscreen);
        }
    }

    private void releasePlayer(){
        currentWindow = player.getCurrentWindowIndex();
        playBackPosition = player.getCurrentPosition();
        autoPlay = player.getPlayWhenReady();
        player.release();
        player = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("ON_PAUSE","ONPAUSE");
        releasePlayer();
    }

    private void initFullscreenDialog(){

            mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                public void onBackPressed() {
                    if (mExoPlayerFullscreen)
                        closeFullscreenDialog();
                    super.onBackPressed();
                }
            };


    }


    private void closeFullscreenDialog() {
        ((ViewGroup) mPlayerView.getParent()).removeView(mPlayerView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(mPlayerView);
        mFullScreenDialog.dismiss();
        mExoPlayerFullscreen = false;
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_expand));
    }

    private void openFullscreenDialog() {

        ((ViewGroup) mPlayerView.getParent()).removeView(mPlayerView);
        mFullScreenDialog.addContentView(mPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }

    private void initFullscreenButton() {

        PlaybackControlView controlView = mPlayerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                else
                    openFullscreenDialog();
            }
        });
    }
}
