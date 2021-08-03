package com.udacity.mybakingapp.ui.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultControlDispatcher;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;

import com.google.android.exoplayer2.source.TrackGroupArray;

import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;

import com.google.android.exoplayer2.util.Util;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.udacity.mybakingapp.model.Ingredient;
import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybakingapp.model.Step;
import com.udacity.mybakingapp.utils.SharedPrefManager;
import com.udacity.mybankingapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements Player.EventListener {

    private static final String CHANNEL_ID = "media_playback_channel";
    public static final String TRACK_NO = "TRACK_NO";
    public static final String TRACK_POSITION = "TRACK_POSITION";
    private static final String TAG = DetailsActivity.class.getSimpleName();

    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;
    private static MediaSessionCompat mMediaSession;
    private PlayerView mPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private int position = 0;
    private long seekBarPosition = 0;

    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;

    @BindView(R.id.recipe_poster_img)
    ImageView mRecipeImage;

    @BindView(R.id.image_recipe_backdrop)
    ImageView mRecipeBackDropImage;

    @BindView(R.id.ingredients_container)
    LinearLayout mRecipeIngredientsContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recipe_title)
    TextView mRecipeTitle;

    @BindView(R.id.txt_short_description_val)
    TextView mShortDescription;

    @BindView(R.id.txt_full_description_val)
    TextView mFullDescription;

    @BindView(R.id.txt_full_description)
    TextView mFullDescriptionLbl;

    @BindView(R.id.txt_short_description)
    TextView mShortDescriptionLbl;

    @BindView(R.id.guide_layout)
    ConstraintLayout guide_layout;

    private String title;
    private RecipeModel model;
    private String shortDescription = "";
    private Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        Log.d("TAG", "onCreate: ");

        if (savedInstanceState != null && savedInstanceState.containsKey(RecipeListActivity.RECIPE)) {
            model = savedInstanceState.getParcelable(RecipeListActivity.RECIPE);
        }

        context = this;
        onNewIntent(getIntent());
        bindData(getIntent());

        setSupportActionBar(toolbar);
        handleActionBarTitle();
        init();

        setRecipeProperties(model);
        setDescriptions(0);

        setupView();
        handleGuideView();
    }

    private void handleGuideView() {
        if (!SharedPrefManager.getViewGuideStatus(context)) {
            setGuideListener();
        } else {
            guide_layout.setVisibility(View.GONE);
        }
    }

    private void setGuideListener() {
        guide_layout.setOnClickListener(view -> {
            Log.d(TAG, "onClick: guide_layout");
            if (!SharedPrefManager.getViewGuideStatus(context)) {
                guide_layout.setVisibility(View.GONE);
                SharedPrefManager.setViewGuideStatus(context, true);
            }
        });
    }

    private void setDescriptions(int position) {
        validateDataModel();

        List<Step> list = model.getSteps();
        if (position < 0 || position > list.size()) {
            Log.d(TAG, "setDescriptions: size and position " + list.size() + " " + position);
            return;
        }
        Step step = list.get(position);
        shortDescription = step.getShortDescription();
        handleDescriptionValidation(step.getShortDescription(), step.getDescription());
    }

    private void validateDataModel() {
        if (model == null || model.getSteps() == null || model.getSteps().size() == 0) {
            Toast.makeText(this, "No steps to be shown...",
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void handleDescriptionValidation(String shortDescription, String longDescription) {
        if (TextUtils.isEmpty(shortDescription)) {
            mShortDescriptionLbl.setVisibility(View.GONE);
        } else {
            mShortDescription.setText(shortDescription);
        }
        if (TextUtils.isEmpty(shortDescription)) {
            mFullDescriptionLbl.setVisibility(View.GONE);
        } else {
            mFullDescription.setText(longDescription);
        }
    }

    private void handleExoPlayerURIValidation(String fullURL, String thumbnailURL) {
        if (TextUtils.isEmpty(fullURL)) {
            if (TextUtils.isEmpty(thumbnailURL)) {
                mExoPlayer.addMediaItem(MediaItem.fromUri(Uri.parse("asset:///dummy_track.mp3")));
            } else {
                mExoPlayer.addMediaItem(MediaItem.fromUri(thumbnailURL));
            }
        } else {
            mExoPlayer.addMediaItem(MediaItem.fromUri(fullURL));
        }
    }

    private void setupView() {
        mPlayerView = findViewById(R.id.playerView);
        mPlayerView.setDefaultArtwork(ContextCompat.getDrawable(getApplicationContext(), R.drawable.recipe_icon));

        mMediaSession = new MediaSessionCompat(this, this.getClass().getSimpleName());
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        mStateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new MySessionCallback());
        mMediaSession.setActive(true);

        initializePlayer();
    }

    private void initializePlayer() {

        if (mExoPlayer == null) {
            mExoPlayer = new SimpleExoPlayer.Builder(this).build();
            mPlayerView.setPlayer(mExoPlayer);
            mPlayerView.setDefaultArtwork(ContextCompat
                    .getDrawable(getApplicationContext(), R.drawable.recipe_icon));

            validateDataModel();

            String fullURL;
            String thumbnailURL;

            for (Step steps : model.getSteps()) {
                if (steps != null) {
                    fullURL = steps.getVideoURL();
                    thumbnailURL = steps.getThumbnailURL();

                    Log.d(TAG, "fullURL: " + fullURL);
                    Log.d(TAG, "thumbnailURL: " + thumbnailURL);
                    Log.d(TAG, "--------------------------------");

                    handleExoPlayerURIValidation(fullURL, thumbnailURL);
                } else {
                    Log.e(TAG, "step is empty...");
                }
            }
            mExoPlayer.prepare();
            mPlayerView.setControlDispatcher(new MyDefaultControlDispatcher());
            mExoPlayer.setPlayWhenReady(true);
            mExoPlayer.addListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        mMediaSession.setActive(false);
    }

    private void releasePlayer() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        seekBarPosition = mExoPlayer.getCurrentPosition();
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        position = mExoPlayer.getCurrentWindowIndex();

        Log.d(TAG, "onTracksChanged: " + position);
        setDescriptions(position);
        showNotification(mStateBuilder.build());
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if ((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
            Log.d("onPlayerStateChanged:", "PLAYING");
        } else if ((playbackState == ExoPlayer.STATE_READY)) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
            Log.d("onPlayerStateChanged:", "PAUSED");
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
        showNotification(mStateBuilder.build());
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    /**
     * Media Session Callbacks, where all external clients control the player.
     */

    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            if (mExoPlayer != null)
                mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            super.onPause();
            if (Util.SDK_INT <= 23) {
                mExoPlayer.setPlayWhenReady(false);
                releasePlayer();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (Util.SDK_INT > 23) {
                mExoPlayer.setPlayWhenReady(false);
                releasePlayer();
            }
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            if (mExoPlayer.hasNext()) {
                Log.i("TAG", "ExoPLayer: Skip to Previous");
                ++position;
                setDescriptions(position);
                mExoPlayer.next();
            }
        }

        @Override
        public void onSkipToPrevious() {
            // mExoPlayer.seekTo(0);
            super.onSkipToPrevious();
            if (mExoPlayer.hasPrevious()) {
                Log.i("TAG", "ExoPLayer: Skip to Previous");
                --position;
                setDescriptions(position);
                mExoPlayer.previous();
            }
        }
    }

    private void showNotification(PlaybackStateCompat state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        int icon;
        String play_pause;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.exo_controls_pause;
            play_pause = getString(R.string.pause);
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = getString(R.string.play);
        }

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action restartAction = new NotificationCompat.Action(
                R.drawable.exo_controls_previous, getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        NotificationCompat.Action next = new NotificationCompat.Action(
                R.drawable.exo_controls_next, getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        Intent intent = new Intent(this, DetailsActivity.class);

        intent.putExtra(RecipeListActivity.RECIPE, model);
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle(title)
                .setContentText(shortDescription)
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.recipe_icon)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .addAction(next)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1));

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients
     */

    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }

    private class MyDefaultControlDispatcher extends DefaultControlDispatcher {
        @Override
        public boolean dispatchPrevious(Player player) {
            Log.d(TAG, "dispatchPrevious: ");
            Timeline timeline = player.getCurrentTimeline();

            if (timeline.isEmpty() || player.isPlayingAd()) {
                return true;
            }
            Timeline.Window window = new Timeline.Window();
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
            int previousWindowIndex = player.getPreviousWindowIndex();
            if (previousWindowIndex != C.INDEX_UNSET
                    && (player.getCurrentPosition() <= 3000
                    || (window.isDynamic && !window.isSeekable))) {
                player.seekTo(previousWindowIndex, C.TIME_UNSET);
                --position;
                setDescriptions(position);
            } else {
                //player.seekTo(windowIndex, /* positionMs= */ 0);
                if (mExoPlayer.hasPrevious()) {
                    player.seekTo(previousWindowIndex, C.TIME_UNSET);
                    if (position > 0) {
                        --position;
                        setDescriptions(position);
                    }
                }
            }
            return true;
        }

        @Override
        public boolean dispatchNext(Player player) {
            Log.d("TAG", "dispatchNext: ");
            ++position;
            setDescriptions(position);
            return super.dispatchNext(player);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        CharSequence name = "Media playback";
        String description = "Media playback controls";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    @Override
    protected void onNewIntent(Intent dataIntent) {
        super.onNewIntent(dataIntent);

        if (dataIntent == null) {
            return;
        }

        Bundle bundle = dataIntent.getExtras();
        if (bundle != null && bundle.containsKey(RecipeListActivity.RECIPE)) {
            model = bundle.getParcelable(RecipeListActivity.RECIPE);

            if (model != null) {
                title = model.getName();
                mRecipeTitle.setText(title);
            }
        } else {
            return;
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(RecipeListActivity.RECIPE, model);
        savedInstanceState.putInt(TRACK_NO, position);
        savedInstanceState.putLong(TRACK_POSITION, mExoPlayer.getContentPosition());
    }

    @Override
    public void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && model != null) {
            model = savedInstanceState.getParcelable(RecipeListActivity.RECIPE);
            position = savedInstanceState.getInt(TRACK_NO);
            seekBarPosition = savedInstanceState.getLong(TRACK_POSITION);
            if (mExoPlayer != null) {
                mExoPlayer.seekTo(position, seekBarPosition);
            }
        }
    }

    private void bindData(Intent intent) {
        if (intent == null) {
            return;
        }
        Bundle bundle = intent.getExtras();

        if (bundle != null && bundle.containsKey(RecipeListActivity.RECIPE)) {
            model = bundle.getParcelable(RecipeListActivity.RECIPE);
            if (model != null) {
                title = model.getName();
                mRecipeTitle.setText(title);
            }
        } else {
            return;
        }
    }

    private void init() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @SuppressLint("PrivateResource")
            Drawable upArrow = ContextCompat.getDrawable(this,
                    R.drawable.abc_ic_ab_back_material);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Objects.requireNonNull(upArrow).setColorFilter(new BlendModeColorFilter
                        (ContextCompat.getColor(this,
                                R.color.secondary), BlendMode.SRC_ATOP));
            } else {
                Objects.requireNonNull(upArrow).setColorFilter(ContextCompat.getColor(this,
                        R.color.secondary), PorterDuff.Mode.SRC_ATOP);
            }
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }
    }

    private void setRecipeProperties(RecipeModel recipe) {
        if (recipe != null) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.recipe_icon)
                    .error(R.drawable.recipe_icon);

            Glide.with(this)
                    .setDefaultRequestOptions(options)
                    .load(recipe.getImage())
                    .into(mRecipeImage);

            Glide.with(this)
                    .setDefaultRequestOptions(options)
                    .load(recipe.getImage())
                    .into(mRecipeBackDropImage);
            setIngredients(recipe);
        }
    }

    private void setIngredients(RecipeModel recipe) {
        mRecipeIngredientsContainer.removeAllViews();
        String suffix;
        StringBuilder builder = new StringBuilder();
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                TextView textView = new TextView(this);

                if (ingredient.getQuantity() > 1) {
                    suffix = "s";
                } else {
                    suffix = "";
                }

                builder.append(ingredient.getQuantity());
                builder.append(" ");
                builder.append(ingredient.getMeasure());
                builder.append(suffix);
                builder.append(" ");
                builder.append(getString(R.string.of));
                builder.append(" ");
                builder.append(ingredient.getIngredient());

                textView.setText(builder.toString());
                builder.setLength(0);
                textView.setTextSize(15);
                textView.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                mRecipeIngredientsContainer.addView(textView);
            }
        } else {
            TextView textView = new TextView(this);
            textView.setText("Error retrieving ingredients.\nCheck network connection.");
            textView.setTextSize(15);
            textView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            mRecipeIngredientsContainer.addView(textView);
        }
    }

    private void handleActionBarTitle() {
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(title);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent recipeList = new Intent(this, RecipeListActivity.class);
        startActivity(recipeList);
    }
}