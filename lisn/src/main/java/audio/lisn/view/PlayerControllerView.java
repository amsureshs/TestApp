    package audio.lisn.view;

    import android.content.Context;
    import android.content.Intent;
    import android.support.v4.content.LocalBroadcastManager;
    import android.util.AttributeSet;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.ImageButton;
    import android.widget.LinearLayout;
    import android.widget.SeekBar;
    import android.widget.TextView;

    import audio.lisn.R;

    import audio.lisn.app.AppController;
    import audio.lisn.model.AudioBook;
    import audio.lisn.model.DownloadedAudioBook;
    import audio.lisn.util.AudioPlayerService;
    import audio.lisn.util.Constants;

    /**
     * Created by Rasika on 4/12/15.
     */
    public class PlayerControllerView extends LinearLayout{

        public  ImageButton previousItemPlayButton,playPauseButton,
                nextItemPlayButton,playStopButton,closePlayButton,bookmarkButton;
        public  TextView audioTitle,musicCurrentLoc,musicDuration;
        public  SeekBar musicSeekBar;
        private Context context;

        public PlayerControllerView(Context context) {
            super(context);
           // LayoutInflater.from(context).inflate(R.layout.view_player_controller, this);
            initViews(context, null);
        }

        public PlayerControllerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initViews(context, attrs);
        }

        public PlayerControllerView(Context context, AttributeSet attrs, int defStyle) {
            this(context, attrs);
            initViews(context, attrs);
        }

        void initViews(Context context,AttributeSet attrs){
            this.context=context;

            LayoutInflater.from(context).inflate(R.layout.view_player_controller, this, true);
            previousItemPlayButton=(ImageButton)this.findViewById(R.id.previousItemPlayButton);
            playPauseButton=(ImageButton)this.findViewById(R.id.playPauseButton);
            nextItemPlayButton=(ImageButton)this.findViewById(R.id.nextItemPlayButton);
            playStopButton=(ImageButton)this.findViewById(R.id.playstopButton);
            closePlayButton=(ImageButton)this.findViewById(R.id.closePlayButton);
            bookmarkButton=(ImageButton)this.findViewById(R.id.playBookmarkButton);
            audioTitle=(TextView)this.findViewById(R.id.audioTitle);
            musicCurrentLoc=(TextView)this.findViewById(R.id.musicCurrentLoc);
            musicDuration=(TextView)this.findViewById(R.id.musicDuration);
            musicSeekBar=(SeekBar)this.findViewById(R.id.musicSeekBar);

            previousItemPlayButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    AppController.getInstance().playPreviousFile();

                }

            });

            playPauseButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    playPauseAudio();

                }

            });
            nextItemPlayButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    AppController.getInstance().playNextFile();


                }

            });
            musicSeekBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());


            playStopButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    stopAudioPlayer();

                }

            });
            bookmarkButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    bookmarkAudioPlayer();

                }

            });
            closePlayButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    closeAudioPlayer();

                }

            });
            updateView();
        }
        private void playPauseAudio(){
            if((AudioPlayerService.mediaPlayer!=null) && AudioPlayerService.mediaPlayer.isPlaying()){
              //  AudioPlayerService.mediaPlayer.pause(); // ?
                playPauseButton.setImageResource(R.drawable.btn_play_start);
                sendStateChange("pause");
            }else if(AudioPlayerService.mediaPlayer!=null){
                playPauseButton.setImageResource(R.drawable.btn_play_pause);
                AudioPlayerService.mediaPlayer.start(); // ?
                sendStateChange("start");

            }

        }
        private void stopAudioPlayer(){

            if(AudioPlayerService.mediaPlayer!=null){
                playPauseButton.setImageResource(R.drawable.btn_play_start);
                sendStateChange("stop");
                AppController.getInstance().stopPlayer();

            }


        }
        private void bookmarkAudioPlayer(){
            bookmarkAudioBook();
        }
        private void closeAudioPlayer(){
            bookmarkAudioBook();
            stopAudioPlayer();

            this.setVisibility(GONE);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
        }

        public void updateView(){
            audioTitle.setText(AppController.getInstance().getPlayerControllerTitle());

            if(AudioPlayerService.mediaPlayer!=null){

                musicSeekBar.setMax(AudioPlayerService.audioDuration);
                musicSeekBar.setProgress(AudioPlayerService.mediaPlayer.getCurrentPosition());
                musicCurrentLoc.setText(milliSecondsToTimer(AudioPlayerService.mediaPlayer.getCurrentPosition()));
                musicDuration.setText(milliSecondsToTimer(AudioPlayerService.audioDuration));

                if(AudioPlayerService.mediaPlayer.isPlaying()){
                playPauseButton.setImageResource(R.drawable.btn_play_pause);

            }else {
                playPauseButton.setImageResource(R.drawable.btn_play_start);


            }
            }

            audioTitle.setText(AppController.getInstance().getPlayerControllerTitle());
        }

//        public void updateSeekBarProgress(){
//
//            if((AudioPlayerService.mediaPlayer!=null) && AudioPlayerService.mediaPlayer.isPlaying()){
//                Log.v("", "updateView isPlaying"+AudioPlayerService.mediaPlayer.getCurrentPosition());
//                musicSeekBar.setMax(AudioPlayerService.mediaPlayer.getDuration());
//                musicDuration.setText(String.format("%.2f", AudioPlayerService.mediaPlayer.getDuration()/60000.0));
//                musicSeekBar.setProgress(AudioPlayerService.mediaPlayer.getCurrentPosition());
//                int currentPosition=AudioPlayerService.mediaPlayer.getCurrentPosition();
//                musicCurrentLoc.setText(String.format("%.2f", currentPosition/60000.0));
//            }else if(AudioPlayerService.mediaPlayer!=null){
//                Log.v("", "updateView notPlaying");
//                playPauseButton.setImageResource(R.drawable.ic_action_play);
//            }
//
//        }
    //    public static void updateSeekBarPosition(final int currentPosition){
    //        Handler handler = new Handler(Looper.getMainLooper());
    //        handler.post(new Runnable() {
    //            public void run() {
    //
    //                musicCurrentLoc.setText(String.format("%.2f", currentPosition/60000.0));
    //                musicSeekBar.setProgress(currentPosition);
    //            }
    //        });
    //
    //
    //    }

        /*  ?Service*/
        class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                /**/
                if (fromUser) {
                    if(AudioPlayerService.mediaPlayer!=null) {
                        AudioPlayerService.mediaPlayer.seekTo(progress);// ?
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(AudioPlayerService.mediaPlayer!=null) {
                    playPauseButton.setImageResource(R.drawable.btn_play_start);
                    AudioPlayerService.mediaPlayer.pause(); // ?
                   // mState = State.Playing;

                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(AudioPlayerService.mediaPlayer!=null) {
                    playPauseButton.setImageResource(R.drawable.btn_play_pause);
                    AudioPlayerService.mediaPlayer.start(); // ?
                }
            }
        }
        public String milliSecondsToTimer(long milliseconds){
            String finalTimerString = "";
            String secondsString = "";

            // Convert total duration into time
            int hours = (int)( milliseconds / (1000*60*60));
            int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
            int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
            // Add hours if there
            if(hours > 0){
                finalTimerString = hours + ":";
            }

            // Prepending 0 to seconds if it is one digit
            if(seconds < 10){
                secondsString = "0" + seconds;
            }else{
                secondsString = "" + seconds;}

            finalTimerString = finalTimerString + minutes + ":" + secondsString;

            // return timer string
            return finalTimerString;
        }
        // Send an Intent with an action named "my-event".
        private void sendStateChange(String state) {
            Intent intent = new Intent(Constants.PLAYER_STATE_CHANGE);
            // add data
            intent.putExtra("state", state);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        private void bookmarkAudioBook(){
            AudioBook audioBook=AppController.getInstance().getCurrentAudioBook();
            Log.v("bookmarkAudioBook","LastPlayFileIndex:"+audioBook.getLastPlayFileIndex());
            Log.v("bookmarkAudioBook","LastSeekPoint:"+audioBook.getLastSeekPoint());

                DownloadedAudioBook downloadedAudioBook = new DownloadedAudioBook(
                        context);
                downloadedAudioBook.addBookToList(context,
                        audioBook.getBook_id(), audioBook);

        }

    }
