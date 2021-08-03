package com.udacity.mybakingapp.ui.frgments;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media.session.MediaButtonReceiver;

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
import com.udacity.mybakingapp.model.Ingredient;
import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybakingapp.model.Step;
import com.udacity.mybakingapp.ui.activities.DetailsActivity;
import com.udacity.mybakingapp.ui.activities.RecipeListActivity;
import com.udacity.mybakingapp.utils.SharedPrefManager;
import com.udacity.mybankingapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.udacity.mybakingapp.ui.activities.DetailsActivity.TRACK_NO;
import static com.udacity.mybakingapp.ui.activities.DetailsActivity.TRACK_POSITION;

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
public class RecipeDetailsFragment extends Fragment implements Player.EventListener {

    private static final String CHANNEL_ID = "media_playback_channel";
    private static final String TAG = DetailsActivity.class.getSimpleName();

    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;
    private static MediaSessionCompat mMediaSession;
    private SimpleExoPlayer mExoPlayer;
    private int position = 0;
    private long seekBarPosition = 0;

    @BindView(R.id.recipe_poster_img)
    ImageView mRecipeImage;

    @BindView(R.id.ingredients_container)
    LinearLayout mRecipeIngredientsContainer;

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

    @BindView(R.id.playerView)
    PlayerView mPlayerView;

    @BindView(R.id.guide_layout)
    ConstraintLayout guide_layout;

    private String title;
    private RecipeModel model;
    private String shortDescription = "";
    @SuppressLint("StaticFieldLeak")
    public static RecipeDetailsFragment recipeListFragment = null;

    public RecipeDetailsFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
    }

    public static RecipeDetailsFragment getInstance() {
        if (recipeListFragment == null) {
            recipeListFragment = new RecipeDetailsFragment();
        }
        return recipeListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recipe_details, container,
                false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null && savedInstanceState.containsKey(RecipeListActivity.RECIPE)) {
            model = savedInstanceState.getParcelable(RecipeListActivity.RECIPE);
        }

        onNewIntent(getArguments());
        bindData(getArguments());
        setRecipeProperties(model);

        setDescriptions(0);
        setupView();
        handleGuideView();
        showNotification(mStateBuilder.build());
        return rootView;
    }

    private void handleGuideView() {
        if (getActivity() != null && !SharedPrefManager.getViewGuideStatus(getActivity())) {
            setGuideListener();
        } else {
            guide_layout.setVisibility(View.GONE);
        }
    }

    private void setGuideListener() {
        guide_layout.setOnClickListener(view -> {
            Log.d(TAG, "onClick: guide_layout");
            guide_layout.setVisibility(View.GONE);
            if (getActivity() != null) {
                SharedPrefManager.setViewGuideStatus(getActivity(), true);
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
            Toast.makeText(getActivity(), "No steps to be shown...",
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
        if (getActivity() == null) {
            return;
        }
        mPlayerView.setDefaultArtwork(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.recipe_icon));

        mMediaSession = new MediaSessionCompat(getActivity(), this.getClass().getSimpleName());
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

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

        if (mExoPlayer == null && getActivity() != null) {
            mExoPlayer = new SimpleExoPlayer.Builder(getActivity()).build();
            mPlayerView.setPlayer(mExoPlayer);
            mPlayerView.setDefaultArtwork(ContextCompat
                    .getDrawable(getActivity().getApplicationContext(), R.drawable.recipe_icon));

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
            mExoPlayer.setPlayWhenReady(true);
            mPlayerView.setControlDispatcher(new MyDefaultControlDispatcher());
            mExoPlayer.addListener(this);
        }
    }

    @Override
    public void onDestroy() {
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
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
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

        // You only need to create the channel on API 26+ devices
        if (getActivity() == null) {
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID);

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
                MediaButtonReceiver.buildMediaButtonPendingIntent(getActivity(),
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action restartAction = new NotificationCompat.Action(
                R.drawable.exo_controls_previous, getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent(getActivity(),
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        NotificationCompat.Action next = new NotificationCompat.Action(
                R.drawable.exo_controls_next, getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent(getActivity(),
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        Intent intent = new Intent(getActivity(), RecipeListActivity.class);
        //intent.putExtra(RecipeListActivity.RECIPE, model);
        PendingIntent contentPendingIntent = PendingIntent.getActivity

                (getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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

        mNotificationManager = (NotificationManager) (getActivity()).getSystemService(NOTIFICATION_SERVICE);
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


    protected void onNewIntent(Bundle dataIntent) {
        if (dataIntent == null) {
            return;
        }
        model = dataIntent.getParcelable(RecipeListActivity.RECIPE);
        if (model != null) {
            title = model.getName();
            mRecipeTitle.setText(title);
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
    public void onActivityCreated(@NotNull Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && model != null) {
            model = savedInstanceState.getParcelable(RecipeListActivity.RECIPE);
            position = savedInstanceState.getInt(TRACK_NO);
            seekBarPosition = savedInstanceState.getLong(TRACK_POSITION);
            if (mExoPlayer != null) {
                mExoPlayer.seekTo(position, seekBarPosition);
            }
        }
    }

    private void bindData(Bundle bundle) {

        if (bundle == null || !bundle.containsKey(RecipeListActivity.RECIPE)) {
            return;
        }

        model = bundle.getParcelable(RecipeListActivity.RECIPE);
        if (model == null) {
            return;
        }
        title = model.getName();
        mRecipeTitle.setText(title);
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

            setIngredients(recipe);
        }
    }

    private void setIngredients(RecipeModel recipe) {
        mRecipeIngredientsContainer.removeAllViews();
        String suffix;
        StringBuilder builder = new StringBuilder();
        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                TextView textView = new TextView(getActivity());

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

                int orientation = getResources().getConfiguration().orientation;
                if (Configuration.ORIENTATION_LANDSCAPE == orientation) {
                    textView.setTextSize(30);
                } else {
                    textView.setTextSize(20);
                }
                textView.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                mRecipeIngredientsContainer.addView(textView);
            }
        } else {
            TextView textView = new TextView(getActivity());
            textView.setText("Error retrieving ingredients.\nCheck network connection.");
            textView.setTextSize(15);
            textView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            mRecipeIngredientsContainer.addView(textView);
        }
    }
}
