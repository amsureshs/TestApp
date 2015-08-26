package audio.lisn.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.io.IOException;
import java.util.List;

import audio.lisn.R;
import audio.lisn.app.AppController;
import audio.lisn.model.AudioBook;
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

    Intent previewPlaybackServiceIntent;

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
        if(book.isPlayingPreview()){
            holder.previewLayout.setVisibility(View.VISIBLE);
            holder.spinner.setVisibility(View.VISIBLE);
            holder.playButton.setImageResource(R.drawable.btn_play_pause);

        }else{
            holder.previewLayout.setVisibility(View.GONE);
            holder.spinner.setVisibility(View.GONE);
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
                    releaseMediaPlayer();
                    PopupMenu popupMenu = new PopupMenu(listener.getListenerActivity(), finalRow);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            AudioBook audioBook = audioBooks.get(bookIndex);

                            switch (item.getItemId()) {
                                case R.id.action_preview:
                                    playButtonPressed(audioBook,bookIndex);
                                    break;
                                case R.id.action_purchase:
                                    listener.onStoreBookSelect(audioBook, AudioBook.SelectedAction.ACTION_PURCHASE);
                                    break;
                                case R.id.action_detail:
                                    listener.onStoreBookSelect(audioBook, AudioBook.SelectedAction.ACTION_DETAIL);

                                    break;
                                default:
                                    break;

                            }
                            Toast.makeText(
                                    context,
                                    "You Clicked : " + item.getTitle(),
                                    Toast.LENGTH_SHORT
                            ).show();
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
        if(audioBook.getPreview_audio() != null) {
            if(previewPlaybackServiceIntent == null)
                previewPlaybackServiceIntent= AppController.getInstance().getPreviewPlaybackServiceIntent();


           // stopService(playbackServiceIntent);
           // startService(playbackServiceIntent);
            AppController.getInstance().playPreviewFile(audioBook.getPreview_audio());
        }
        }
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

    private void playPreview(AudioBook audioBook ) {
        if (audioBook.getPreview_audio() !=null && (audioBook.getPreview_audio().length()>0)) {
            if (connectionDetector.isConnectingToInternet()) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();

                }else{
                    if(mediaPlayer.isPlaying())
                        mediaPlayer.stop();

                }

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(audioBook.getPreview_audio());
                } catch (IllegalArgumentException e) {
                    Log.v("playPreview","IllegalArgumentException"+e.getMessage());

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SecurityException e) {
                    Log.v("playPreview","SecurityException"+e.getMessage());

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    Log.v("playPreview","IllegalStateException"+e.getMessage());

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.v("playPreview","IOException"+e.getMessage());

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        mediaPlayer.reset();
                        stopPreview();
                        System.out.println("Media Player onError callback!");
                        return true;
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopPreview();
                    }
                });
               // mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync(); // prepare async to not block main
                // thread

                //btnListenPreview.setEnabled(false);

//                mProgressDialog = new ProgressDialog(context);
//                mProgressDialog.setMessage("Please wait!");
//                mProgressDialog.setTitle("Loading...");
//                mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        stopPreview();
//                    }
//                });
            /*
            mProgressDialog = ProgressDialog.show(this, "Please wait!",
                    "Loading...", true);
                    */

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
    }
    private void stopPreview(){
        if (mediaPlayer != null){
            if(mediaPlayer.isPlaying())
            mediaPlayer.stop();
            mediaPlayer.reset();
           // mediaPlayer.release();
           // mediaPlayer=null;

        }
        selectedAudioBook.setPlayingPreview(false);
        notifyDataSetChanged();

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
