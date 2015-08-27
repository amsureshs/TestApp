package audio.lisn.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.io.IOException;
import java.util.List;

import audio.lisn.R;
import audio.lisn.app.AppController;
import audio.lisn.model.AudioBook;
import audio.lisn.util.AppUtils;
import audio.lisn.util.ConnectionDetector;
import audio.lisn.util.CustomTypeFace;

/**
 * Created by Rasika on 8/9/15.
 */
public class StoreBookGridViewAdapter extends ArrayAdapter implements  Runnable,PopupMenu.OnMenuItemClickListener{
    private Context context;
    private int layoutResourceId;
    private List<AudioBook>audioBooks;
    LayoutInflater inflater;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private StoreBookSelectListener listener;
  //  boolean isPlayingPreview;
    MediaPlayer mediaPlayer = null;
    ProgressDialog mProgressDialog;
    ConnectionDetector connectionDetector;
    AudioBook selectedAudioBook;
    private boolean isPlayingPreview,isLoadingPreview;

    Intent previewPlaybackServiceIntent;
    String leftTime;


    // int selectedBookIndex;
    //  private ArrayList data = new ArrayList();

    public StoreBookGridViewAdapter(Context context, int layoutResourceId, List<AudioBook> audioBooks) {
        super(context, layoutResourceId, audioBooks);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.audioBooks = audioBooks;
        connectionDetector = new ConnectionDetector(context);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        final int bookIndex = position;

        if (row == null) {
            if (inflater == null)
                inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();

            holder.thumbNail=(NetworkImageView) row
                    .findViewById(R.id.book_cover_thumbnail);
            holder.title= (TextView) row.findViewById(R.id.book_title);
            holder.author= (TextView) row.findViewById(R.id.book_author);
            holder.price= (TextView) row.findViewById(R.id.book_price);
            holder.ratingBar=(RatingBar)row.findViewById(R.id.rating_bar);
            holder.optionButton=(ImageButton)row.findViewById(R.id.btn_action);
            holder.playButton=(ImageButton)row.findViewById(R.id.playButton);
            holder.previewLayout=(RelativeLayout)row.findViewById(R.id.preview_layout);
            holder.previewLabel=(TextView)row.findViewById(R.id.preview_label);
            holder.timeLabel=(TextView)row.findViewById(R.id.time_label);
            holder.spinner = (ProgressBar)row.findViewById(R.id.progressBar);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

       //final AudioBook book = audioBooks.get(position);
        AudioBook book = audioBooks.get(position);
        if((isLoadingPreview || isPlayingPreview) && selectedAudioBook.getBook_id().equalsIgnoreCase(book.getBook_id()) ){
            holder.previewLayout.setVisibility(View.VISIBLE);
            holder.playButton.setImageResource(R.drawable.btn_play_pause);

            if(isPlayingPreview){
                holder.spinner.setVisibility(View.INVISIBLE);
                holder.previewLabel.setText("Preview");
                holder.timeLabel.setText(leftTime);

            }else{
                holder.spinner.setVisibility(View.VISIBLE);
                holder.previewLabel.setText("Loading...");
                holder.timeLabel.setText("");

            }


        }else{
            holder.previewLayout.setVisibility(View.GONE);
           // holder.spinner.setVisibility(View.GONE);
            holder.playButton.setImageResource(R.drawable.btn_play_start);

        }


        if(book.getLanguageCode()== AudioBook.LanguageCode.LAN_SI){
            holder.title.setTypeface(CustomTypeFace.getSinhalaTypeFace(context));
            holder.author.setTypeface(CustomTypeFace.getSinhalaTypeFace(context));
        }else{
            holder.title.setTypeface(CustomTypeFace.getEnglishTypeFace(context));
            holder.author.setTypeface(CustomTypeFace.getEnglishTypeFace(context));
        }
        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        String priceText="Free";
        if( Float.parseFloat(book.getPrice())>0 ){
            priceText="LKR "+book.getPrice();
        }
        holder.price.setText(priceText);
        if(Float.parseFloat(book.getRate())>-1){
            holder.ratingBar.setRating(Float.parseFloat(book.getRate()));
        }
        holder.ratingBar.setIsIndicator(true);

        // thumbnail image
        holder.thumbNail.setImageUrl(book.getCover_image(), imageLoader);

        final View finalRow = holder.playButton;
        row.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(listener != null){
                    releaseMediaPlayer();
                    AudioBook audioBook = audioBooks.get(bookIndex);

                    listener.onStoreBookSelect(audioBook, AudioBook.SelectedAction.ACTION_DETAIL);
                }

            }
        });
        holder.optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listener != null){

                    PopupMenu popupMenu = new PopupMenu(listener.getListenerActivity(), finalRow);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            AudioBook audioBook = audioBooks.get(bookIndex);

                            switch (item.getItemId()) {
                                case R.id.action_preview:
                                    playButtonPressed(audioBook,bookIndex);
                                    break;
                                case R.id.action_purchase:
                                    releaseMediaPlayer();
                                    listener.onStoreBookSelect(audioBook, AudioBook.SelectedAction.ACTION_PURCHASE);
                                    break;
                                case R.id.action_detail:
                                    releaseMediaPlayer();
                                    listener.onStoreBookSelect(audioBook, AudioBook.SelectedAction.ACTION_DETAIL);

                                    break;
                                default:
                                    break;

                            }
                            
                            return true;
                        }
                    });
                    popupMenu.inflate(R.menu.store_book_menu);
                    popupMenu.show();
                }

            }
        });

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioBook audioBook = audioBooks.get(bookIndex);

                playButtonPressed(audioBook,bookIndex);

            }
        });



       // holder.imageTitle.setText(item.getTitle());
        //holder.image.setImageBitmap(item.getImage());
        return row;
    }
    private void playButtonPressed(AudioBook audioBook,int bookIndex){
        if (audioBook.getPreview_audio() !=null && (audioBook.getPreview_audio().length()>0)) {
            boolean stopPlayer = false;
            if(selectedAudioBook != null){
                if((isLoadingPreview || isPlayingPreview ) && (audioBook.getBook_id().equalsIgnoreCase(selectedAudioBook.getBook_id()))){
                    stopPlayer=true;
                }
            }
            selectedAudioBook=audioBook;
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

           // AppController.getInstance().playPreviewFile(audioBook.getPreview_audio());
        }else{
            if(selectedAudioBook != null && isPlayingPreview){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    new Thread(this).interrupt();
                }

                mediaPlayer.reset();
                isPlayingPreview=false;
                isLoadingPreview=false;

            }
        }
        notifyDataSetChanged();
        }

//    private void playButtonPressed(AudioBook audioBook,int bookIndex){
//        if(audioBook.getPreview_audio() != null) {
//            if(previewPlaybackServiceIntent == null)
//                previewPlaybackServiceIntent= AppController.getInstance().getPreviewPlaybackServiceIntent();
//            AppController.getInstance().playPreviewFile(audioBook.getPreview_audio());
//        }
//    }
    /*
    private void playButtonPressed(AudioBook audioBook,int bookIndex){
        if(audioBook.getPreview_audio() != null) {

            if(selectedAudioBook !=null) {
                stopPreview();
            }
//            if(audioBook.isPlayingPreview()){
//                audioBook.setPlayingPreview(false);
//            }else{
//                audioBook.setPlayingPreview(true);
//
//            }
            audioBook.setPlayingPreview(!audioBook.isPlayingPreview());
          //  audioBooks.set(bookIndex,audioBook);


//            if(selectedAudioBook !=null){
//                stopPreview();
////                if (selectedAudioBook.isPlayingPreview()) {
////                    stopPreview();
////                }
//              //  if(selectedAudioBook !=null && selectedAudioBook.getBook_id() != audioBook.getBook_id()){
//               // selectedAudioBook.setPlayingPreview(!selectedAudioBook.isPlayingPreview());
//
//            }
            selectedAudioBook = audioBook;

//            if (mediaPlayer != null) {
//                mediaPlayer.reset();
//            }

            if (selectedAudioBook.isPlayingPreview()) {
                Log.v("isPlayingPreview"," true"+audioBook.isPlayingPreview());
                playPreview(audioBook);

            }else{
                Log.v("isPlayingPreview"," false"+audioBook.isPlayingPreview());

            }
            notifyDataSetChanged();

        }
    }

*/
    public void setListener(StoreBookSelectListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
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
        leftTime= AppUtils.milliSecondsToTimer(totalDuration-currentPosition);
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(context.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();

            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        Log.v("onPrepared","onPrepared");
//        mp.start();
//
//    }

    static class ViewHolder {
        NetworkImageView thumbNail;
        TextView title, author,price;
        RatingBar ratingBar;
        ImageButton optionButton,playButton;
        RelativeLayout previewLayout;
        TextView previewLabel,timeLabel;
        private ProgressBar spinner;
    }

    public interface StoreBookSelectListener
    {
        public void onStoreBookSelect(AudioBook audioBook,AudioBook.SelectedAction btnIndex);
        public Activity getListenerActivity();
    }

    private void playPreview( ) {
        isLoadingPreview=true;
        isPlayingPreview=false;

        if (connectionDetector.isConnectingToInternet()) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                new Thread(this).interrupt();
            }

                mediaPlayer.reset();

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(selectedAudioBook.getPreview_audio());
                }catch (IOException e) {
                    Log.v("playPreview","IOException"+e.getMessage());

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        isPlayingPreview=true;
                        isLoadingPreview=false;
                        startTimer();
                        mp.start();
                        notifyDataSetChanged();
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
                        notifyDataSetChanged();
                    }
                });
                mediaPlayer.prepareAsync(); // prepare async to not block main


            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
//    private void stopPreview(){
//        if (mediaPlayer != null){
//            if(mediaPlayer.isPlaying())
//            mediaPlayer.stop();
//            mediaPlayer.release();
//            mediaPlayer=null;
//
//        }
//
//
//    }
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
    }
}
