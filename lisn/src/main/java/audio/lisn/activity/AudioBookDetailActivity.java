package audio.lisn.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import audio.lisn.R;
import audio.lisn.model.AudioBook;
import audio.lisn.util.AppUtils;
import audio.lisn.util.ConnectionDetector;
import audio.lisn.util.CustomTypeFace;

public class AudioBookDetailActivity extends  AppCompatActivity implements Runnable{

    private static final String TRANSITION_NAME = "audio.lisn.AudioBookDetailActivity";
    private CollapsingToolbarLayout collapsingToolbarLayout;
    AudioBook audioBook;
    ImageButton previewPlayButton;
    //private boolean isPlayingPreview,isLoadingPreview;
    MediaPlayer mediaPlayer = null;
    ConnectionDetector connectionDetector;

    public RelativeLayout previewLayout;
    public TextView previewLabel,timeLabel;
    public ProgressBar spinner;
    private boolean isPlayingPreview,isLoadingPreview;
    String leftTime;



    public static void navigate(AppCompatActivity activity, View transitionImage, AudioBook audioBook) {
        Intent intent = new Intent(activity, AudioBookDetailActivity.class);
        intent.putExtra("audioBook", audioBook);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionImage, TRANSITION_NAME);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @SuppressWarnings("ConstantConditions")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_audio_book_detail);
        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), TRANSITION_NAME);

        audioBook = (AudioBook) getIntent().getSerializableExtra("audioBook");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        supportStartPostponedEnterTransition();

        String itemTitle = audioBook.getEnglish_title();
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(itemTitle);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        connectionDetector = new ConnectionDetector(getApplicationContext());

        updateData();


    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void updateData() {

        ImageView bookBannerImage = (ImageView) findViewById(R.id.bookBannerImage);
        String bannerImageUrl="http://lorempixel.com/500/500/animals/8/";

        Picasso.with(this).load(bannerImageUrl).into(bookBannerImage);
        Picasso.with(this)
                .load(bannerImageUrl)
                .placeholder(R.drawable.drawer_background)
                .into(bookBannerImage);

        final ImageView bookCoverImage = (ImageView) findViewById(R.id.bookCoverImage);
        Picasso.with(this)
                .load(audioBook.getCover_image())
                .placeholder(R.drawable.audiobook_placeholder)
                .into(bookCoverImage);

        String narratorText="";
        String durationText="";
        //ExpandableTextView descriptionTextView = (ExpandableTextView)findViewById(R.id.description);
        TextView title = (TextView) findViewById(R.id.title);
        TextView description = (TextView) findViewById(R.id.description);
        TextView category = (TextView) findViewById(R.id.category);
        TextView price = (TextView) findViewById(R.id.price);
        TextView author = (TextView) findViewById(R.id.author);
        TextView narrator = (TextView) findViewById(R.id.narrator);
        TextView ratingValue = (TextView) findViewById(R.id.rating_value);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_bar);
         previewPlayButton = (ImageButton) findViewById(R.id.previewPlayButton);
        previewPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviewButtonPressed();

            }
        });

        previewLayout=(RelativeLayout)findViewById(R.id.preview_layout);
        previewLabel=(TextView)findViewById(R.id.preview_label);
        timeLabel=(TextView)findViewById(R.id.time_label);
        spinner = (ProgressBar)findViewById(R.id.progressBar);

        if(audioBook.getLanguageCode()== AudioBook.LanguageCode.LAN_SI){
            description.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            title.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            author.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            category.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            narrator.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
          //  duration.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            narratorText=getString(R.string.narrator_si);
            durationText=getString(R.string.duration_si);
        }else{
            description.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            title.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            author.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            category.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            narrator.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
          //  duration.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));

            narratorText=getString(R.string.narrator_en);
            durationText=getString(R.string.duration_en);
        }
        title.setText(audioBook.getTitle());
        String priceText="Free";
        if( Float.parseFloat(audioBook.getPrice())>0 ){
            priceText="Rs: "+audioBook.getPrice();
        }

        if(Float.parseFloat(audioBook.getRate())>-1){
            ratingBar.setRating(Float.parseFloat(audioBook.getRate()));
            ratingValue.setText(String.format("%.1f", Float.parseFloat(audioBook.getRate())));


        }

        narratorText=narratorText+" - "+audioBook.getNarrator();
        durationText=durationText+" - "+audioBook.getDuration();
        author.setText(audioBook.getAuthor());
        category.setText(audioBook.getCategory());
        narrator.setText(narratorText);
       // duration.setText(durationText);
        price.setText(priceText);
        title.setText(audioBook.getTitle());
        description.setText(audioBook.getDescription());
        if(audioBook.isPurchase()){
           // btnDownload.setImageResource(R.drawable.btn_lisn_book_large);
        }else{
           // btnDownload.setImageResource(R.drawable.btn_buy_book_large);


        }

    }
private void updatePreviewLayout(){
    if(isLoadingPreview || isPlayingPreview){
        previewLayout.setVisibility(View.VISIBLE);
        previewPlayButton.setImageResource(R.drawable.btn_play_pause);

        if(isPlayingPreview){
           spinner.setVisibility(View.INVISIBLE);
            previewLabel.setText("Preview");
            timeLabel.setText(leftTime);

        }else{
            spinner.setVisibility(View.VISIBLE);
            previewLabel.setText("Loading...");
            timeLabel.setText("");
        }
    }else{
        previewLayout.setVisibility(View.GONE);
        previewPlayButton.setImageResource(R.drawable.btn_play_start);
    }
}

    private void playPreviewButtonPressed(){
        if (audioBook.getPreview_audio() !=null && (audioBook.getPreview_audio().length()>0)) {
            boolean stopPlayer = false;
                if(isLoadingPreview || isPlayingPreview ){
                    stopPlayer=true;
                }

            if(stopPlayer){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    new Thread(this).interrupt();
                }

                mediaPlayer.reset();
                isPlayingPreview=false;
                isLoadingPreview=false;

            }else{
                playPreview();
            }

        }
        updatePreviewLayout();
    }
    private void playPreview( ) {
        isLoadingPreview=true;
        isPlayingPreview=false;
                if (connectionDetector.isConnectingToInternet()) {
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    }
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        new Thread(this).interrupt();
                    }

                    mediaPlayer.reset();

                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(audioBook.getPreview_audio());
                    } catch (IOException e) {
                        Log.v("playPreview", "IOException" + e.getMessage());

                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        public void onPrepared(MediaPlayer mp) {
                            isPlayingPreview=true;
                            isLoadingPreview=false;
                            startTimer();
                            mp.start();
                            updatePreviewLayout();
                        }
                    });
                    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        public boolean onError(MediaPlayer mp, int what, int extra) {

                            return false;
                        }
                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            isPlayingPreview=false;
                            isLoadingPreview=false;
                            stopTimer();
                            updatePreviewLayout();

                        }
                    });
                    mediaPlayer.prepareAsync(); // prepare async to not block main


                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("No Internet Connection").setPositiveButton(
                            "OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // FIRE ZE MISSILES!
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

    }

    private void startTimer(){
        new Thread(this).start();
    }
    private void stopTimer(){
        new Thread(this).interrupt();
    }

    private void releaseMediaPlayer(){
        if (mediaPlayer != null){
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;

        }
        stopTimer();
    }


    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int currentPosition = 0;//
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < mediaPlayer.getDuration()) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           updateTimer();
        }
    }
    private void updateTimer() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int totalDuration =mediaPlayer.getDuration();
        leftTime= AppUtils.milliSecondsToTimer(totalDuration - currentPosition);
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(this.getMainLooper());

        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                updatePreviewLayout();

            } // This is your code
        };
        mainHandler.post(timerRunnable);
    }
}
