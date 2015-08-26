package audio.lisn.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import audio.lisn.R;
import audio.lisn.app.AppController;
import audio.lisn.model.AudioBook;
import audio.lisn.model.AudioBook.LanguageCode;
import audio.lisn.model.DownloadedAudioBook;
import audio.lisn.util.AppUtils;
import audio.lisn.util.AudioPlayerService;
import audio.lisn.util.ConnectionDetector;
import audio.lisn.util.CustomTypeFace;
import audio.lisn.view.PlayerControllerView;
import audio.lisn.webservice.JsonUTF8StringRequest;

public class AudioBookDetailActivity extends AudioBookBaseActivity implements
        OnClickListener, MediaPlayer.OnPreparedListener {

    ImageButton btnDownload,btnComment, btnListenPreview;
    AudioBook audioBook;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    NetworkImageView bookCoverImage;
    MediaPlayer mediaPlayer = null;
    ProgressDialog mProgressDialog;
    ConnectionDetector connectionDetector;
    TextView title, author,category,narrator,duration,price, description;
    PlayerControllerView audioPlayerLayout;
    int totalAudioFileCount, downloadedFileCount;
    Intent playbackServiceIntent;
    boolean isPlayingPreview;
    Toolbar toolbar;
    List<DownloadTask> downloadingList = new ArrayList<DownloadTask>();
    RatingBar ratingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_audio_book_detail);
        connectionDetector = new ConnectionDetector(getApplicationContext());

        audioBook = (AudioBook) getIntent().getSerializableExtra("audioBook");
        getSupportActionBar().setTitle(audioBook.getEnglish_title());


//        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
//        setSupportActionBar(toolbar);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle(audioBook.getEnglish_title());
//        actionBar.setDisplayUseLogoEnabled(true);


        audioPlayerLayout = (PlayerControllerView) findViewById(R.id.audio_player_layout);
        bookCoverImage = (NetworkImageView) findViewById(R.id.bookCoverImage);
        bookCoverImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                if(!isPlayingPreview){
//                    playPreview();
//                }else{
//                    stopPreview();
//                }

                if( Float.parseFloat(audioBook.getPrice())>0 ){
                    //	onBuyPressed(arg0);
                }else{
                    downloadAudioFile();
                }

            }

        });
        title = (TextView) findViewById(R.id.title);
        description = (TextView) findViewById(R.id.description);
        author = (TextView) findViewById(R.id.author);
        category = (TextView) findViewById(R.id.category);
        narrator = (TextView) findViewById(R.id.narrator);
        duration = (TextView) findViewById(R.id.duration);
        price = (TextView) findViewById(R.id.price);
        ratingBar=(RatingBar)findViewById(R.id.rating_bar);


        btnComment = (ImageButton) findViewById(R.id.btnComment);
        btnComment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showCommentDialog();

            }

        });

        btnDownload = (ImageButton) findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if( Float.parseFloat(audioBook.getPrice())>0 ){
                    //	onBuyPressed(arg0);
                }else{
                    downloadAudioFile();
                }

            }

        });
        btnListenPreview = (ImageButton) findViewById(R.id.btnListenPreview);
        btnListenPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //btnListenPreview.setImageResource(R.drawable.btn_lisn_preview_book);

                if(!isPlayingPreview){
                    playPreview();
                }else{
                    stopPreview();
                }


            }

        });
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(fromUser){
                    uploadUserRate(rating);
                }

            }

        });

        loadData();

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(AudioBookDetailActivity.this);
        mProgressDialog.setMessage("Downloading file..");
        mProgressDialog.setTitle("Download in progress ...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                stopDownload();
            }
        });


        //mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //mProgressDialog.setCancelable(true);
        // initLibrary();
    }


    @Override protected int getLayoutResource() {
        return R.layout.activity_audio_book_detail;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if((AudioPlayerService.mediaPlayer!=null) && AudioPlayerService.hasStartedPlayer){
            audioPlayerLayout.setVisibility(View.VISIBLE);
        }else{
            audioPlayerLayout.setVisibility(View.GONE);

        }
        audioPlayerLayout.updateView();
        registerBroadcastReceiver();
    }
private void stopDownload(){
    for (int i = 0; i < downloadingList.size(); i++) {
        DownloadTask downloadTask = downloadingList.get(i);
        downloadTask.cancel(true);
    }
}
    private void starAudioPlayer(){
        if(playbackServiceIntent == null)
            playbackServiceIntent= AppController.getInstance().getPlaybackServiceIntent();

        String[] fileList=new String[audioBook.getDownloadedFileList().size()];
        for (int i=0; i<audioBook.getDownloadedFileList().size();i++){
            fileList[i]=AppUtils.getDataDirectory(getApplicationContext())+audioBook.getBook_id()+"/"+(i+1)+".mp3";

        }
        AppController.getInstance().setFileList(fileList);
        AppController.getInstance().setCurrentAudioBook(audioBook);
        int lastPlayFileIndex=audioBook.getLastPlayFileIndex();
        if(lastPlayFileIndex<1){
            lastPlayFileIndex=0;
        }
        AppController.getInstance().fileIndex=(audioBook.getLastPlayFileIndex()-1);
        stopService(playbackServiceIntent);
        startService(playbackServiceIntent);
        AppController.getInstance().playNextFile();
       // audioPlayerLayout.updateView();

        audioPlayerLayout.setVisibility(View.VISIBLE);
    }
    private void stopPreview(){
        if (mediaPlayer != null){
            mediaPlayer.stop();
            isPlayingPreview=false;
            btnListenPreview.setImageResource(R.drawable.btn_lisn_preview_book);
        }
        //mediaPlayer = null;
    }
    private void playPreview() {
        if (audioBook.getPreview_audio() !=null && (audioBook.getPreview_audio().length()>0)) {
            if (connectionDetector.isConnectingToInternet()) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();

                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(audioBook.getPreview_audio());
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync(); // prepare async to not block main
                // thread

                //btnListenPreview.setEnabled(false);

                mProgressDialog = new ProgressDialog(AudioBookDetailActivity.this);
                mProgressDialog.setMessage("Please wait!");
                mProgressDialog.setTitle("Loading...");
                mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        stopPreview();
                    }
                });
            /*
            mProgressDialog = ProgressDialog.show(this, "Please wait!",
                    "Loading...", true);
                    */

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        this);
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

    private void loadData() {

        bookCoverImage.setImageUrl(audioBook.getCover_image(), imageLoader);
        String narratorText="";
        String durationText="";

        if(audioBook.getLanguageCode()== LanguageCode.LAN_SI){
            Log.v("language_code abc : ", "si");
            description.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            title.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            author.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            category.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            narrator.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            duration.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            narratorText=getString(R.string.narrator_si);
            durationText=getString(R.string.duration_si);
        }else{
            description.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            title.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            author.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            category.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            narrator.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            duration.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));

            narratorText=getString(R.string.narrator_en);
            durationText=getString(R.string.duration_en);
        }
        title.setText(audioBook.getTitle());
        String priceText="Free";
        if( Float.parseFloat(audioBook.getPrice())>0 ){
            priceText="Rs: "+audioBook.getPrice();
        }
        narratorText=narratorText+" - "+audioBook.getNarrator();
        durationText=durationText+" - "+audioBook.getDuration();
        author.setText(" - "+audioBook.getAuthor());
        category.setText(audioBook.getCategory());
        narrator.setText(narratorText);
        duration.setText(durationText);
        price.setText(priceText);
        title.setText(audioBook.getTitle());
        description.setText(audioBook.getDescription());
        if(audioBook.isPurchase()){
            btnDownload.setImageResource(R.drawable.btn_lisn_book_large);
        }else{
            btnDownload.setImageResource(R.drawable.btn_buy_book_large);


        }

    }

    private void downloadAudioFileFromUrl(String url,String fileName){

        if (connectionDetector.isConnectingToInternet()) {
            String dirPath = AppUtils.getDataDirectory(getApplicationContext())
                    + audioBook.getBook_id();
            File file = new File(dirPath + "/" + fileName + ".mp3");

            if (file.exists()) {
                file.delete();
            }
            DownloadTask downloadTask =  new DownloadTask(this);
            downloadTask.execute(dirPath, "" + fileName,
                    url);
            downloadingList.add(downloadTask);

        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    this);
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
    private void downloadAudioFile() {

        if (connectionDetector.isConnectingToInternet()) {
            audioBook.setPurchase(true);
            updateAudioBook(null,null);

            String dirPath = AppUtils.getDataDirectory(getApplicationContext())
                    + audioBook.getBook_id();
            mProgressDialog.show();

            downloadedFileCount=0;
            String [] file_urls=audioBook.getAudio_file_urls();
            //totalAudioFileCount=file_urls.length;
            totalAudioFileCount=0;
            downloadingList.clear();
            HashMap fileList= audioBook.getDownloadedFileList();

            for (int i=0; i<(file_urls.length); i++){
                String file_url=file_urls[i];
                String file_name_with_extention=file_url.substring(file_url.lastIndexOf("/")+1, file_url.length() );
                File file = new File(dirPath + "/" +file_name_with_extention);
               String file_name= file_name_with_extention.substring(0, file_name_with_extention.lastIndexOf('.'));

                if (!file.exists() || (fileList==null) ||(fileList.get(file_name) ==null)) {
                    downloadAudioFileFromUrl(file_url,file_name);
                    totalAudioFileCount++;
                }


            }
            if(downloadedFileCount ==totalAudioFileCount){
                mProgressDialog.dismiss();
                starAudioPlayer();
            }else{
                logUserDownload();
                mProgressDialog.setMessage("Downloading " + (downloadedFileCount + 1) + " of " + totalAudioFileCount);

            }

        } else {

            downloadedFileCount=0;
            String [] file_urls=audioBook.getAudio_file_urls();
            //totalAudioFileCount=file_urls.length;
            totalAudioFileCount=0;

            HashMap fileList= audioBook.getDownloadedFileList();
            String dirPath = AppUtils.getDataDirectory(getApplicationContext())
                    + audioBook.getBook_id();

            for (int i=0; i<(file_urls.length); i++){
                String file_url=file_urls[i];
                String file_name_with_extention=file_url.substring(file_url.lastIndexOf("/")+1, file_url.length() );
                File file = new File(dirPath + "/" +file_name_with_extention);
                String file_name= file_name_with_extention.substring(0, file_name_with_extention.lastIndexOf('.'));

                if (!file.exists() || (fileList==null) ||(fileList.get(file_name) ==null)) {
                    totalAudioFileCount++;
                }


            }
            if(downloadedFileCount ==totalAudioFileCount){
                mProgressDialog.dismiss();
                starAudioPlayer();
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        this);
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
    private void updateAudioBook(String key, String url){
        if(key != null) {
            audioBook.addFileToDownloadedList(key, url);
        }
        DownloadedAudioBook downloadedAudioBook = new DownloadedAudioBook(
                getApplicationContext());
        downloadedAudioBook.readFileFromDisk(getApplicationContext());
        downloadedAudioBook.addBookToList(getApplicationContext(),
                audioBook.getBook_id(), audioBook);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }





    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPlayerUpdateReceiver);
        super.onPause();
    }



    @Override
    public void onDestroy() {
        // Stop service when done
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        mediaPlayer = null;
        isPlayingPreview=false;
        btnListenPreview.setImageResource(R.drawable.btn_lisn_preview_book);
       // btnListenPreview.setEnabled(true);
        super.onStop();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
       // btnListenPreview.setEnabled(true);

        mProgressDialog.dismiss();
        mp.start();
        isPlayingPreview=true;
        btnListenPreview.setImageResource(R.drawable.btn_stop_preview_book);


    }

    private void registerBroadcastReceiver(){
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mPlayerUpdateReceiver,
                new IntentFilter("audio-event"));
    }
    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mPlayerUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            audioPlayerLayout.updateView();
        }
    };

/* Download AsyncTask*/
    public class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private String file_name,file_url;

        public DownloadTask(Context context, Dialog dialog,
                            ProgressBar progressBar, TextView progressTextView,
                            String destinationPath, String fileName, JSONObject jObject) {
            this.context = context;
        }

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            String directory = sUrl[0];
            String fileName = sUrl[1];
            file_name=fileName;
            file_url=sUrl[2];

            // prevent CPU from going off if the user presses the power button
            // during download
            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wl.acquire();

            // download
            try {
                File rootPath = new File(directory);
                if (!rootPath.exists()) {
                    rootPath.mkdirs();
                }
                // new File(directory).mkdirs();
                InputStream input = null;
                OutputStream output = null;
                CipherOutputStream cos = null;
                HttpURLConnection connection = null;
                try {
                    // connect to url
                    URL url = new URL(sUrl[2]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // check for http_ok (200)
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                        return "Server returned HTTP "
                                + connection.getResponseCode() + " "
                                + connection.getResponseMessage();

                    int fileLength = connection.getContentLength();
                    // download the file
                    input = connection.getInputStream();
                    output = new FileOutputStream(directory + "/" + fileName
                            + ".mp3");// change extension

                    // copying
                    byte data[] = new byte[4096];
                    int count;
                    long total = 0;
                    SecretKeySpec sks = new SecretKeySpec(
                            "Mary has one cat".getBytes(), "AES");
                    // Create cipher
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, sks);
                    // Wrap the output stream
                    cos = new CipherOutputStream(output, cipher);

                    while ((count = input.read(data)) != -1) {

                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        //if (fileLength > 0) // only if total length is known
                        //	publishProgress((int) (total * 100 / fileLength));
                        cos.write(data, 0, count);
                    }

                } catch (Exception e) {
                    return e.toString();
                } finally // closing streams and connection
                {
                    try {
                        if (cos != null) {
                            cos.flush();
                            cos.close();
                        }

                        if (input != null)
                            input.close();
                    } catch (IOException ignored) {
                    }

                    if (connection != null)
                        connection.disconnect();
                }
                if(isCancelled() && (connection != null)){
                    Log.v("isCancelled","disconnect");
                    connection.disconnect();
                }
            } finally {
                wl.release(); // release the lock screen
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            // PowerManager pm = (PowerManager)
            // context.getSystemService(Context.POWER_SERVICE);
            // mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
            // getClass().getName());
            // mWakeLock.acquire();

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            //mProgressDialog.setIndeterminate(false);
            //mProgressDialog.setMax(100);
            //mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.setMessage("Downloading " + (downloadedFileCount + 1) + " of " + totalAudioFileCount);

            downloadedFileCount++;
            if (result == null) {
                audioBook.addFileToDownloadedList(file_name, file_url);
                updateAudioBook(null,null);
                if (totalAudioFileCount == downloadedFileCount) {
                    mProgressDialog.dismiss();
                    starAudioPlayer();
                }
            }
            // mWakeLock.release();
            //mProgressDialog.dismiss();
            // if (result != null)
            // Toast.makeText(context,"Download error: "+result,
            // Toast.LENGTH_LONG).show();
            // else
            // Toast.makeText(context,"File downloaded",
            // Toast.LENGTH_SHORT).show();
        }

    }
    private void saveCoverImage(String url){
        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                // iv.setImageBitmap(response);
            }
        }, 0, 0, null, null);

    }
/* private method */
    private void showCommentDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_comment);
        dialog.setTitle("Post Comment");

        // set the custom dialog components - text, image and button
        final EditText comment = (EditText) dialog.findViewById(R.id.comment);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                logUserComment(comment.getText().toString());
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void uploadUserRate(final float rateValue){
        Map<String, String> params = new HashMap<String, String>();
        params.put("userid", AppController.getInstance().getUserId());
        params.put("bookid", audioBook.getBook_id());
        params.put("actid", ""+6);
        params.put("content", ""+rateValue);

        String url = getResources().getString(R.string.user_action_url);
Log.v("url:","url:"+url);
// Request a string response from the provided URL.
        JsonUTF8StringRequest stringRequest = new JsonUTF8StringRequest(Request.Method.POST, url,params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("response","response:"+response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {

        };
// Add the request to the RequestQueue.
        AppController.getInstance().addToRequestQueue(stringRequest,"tag_rate_book");

    }
    private void logUserDownload(){

        Map<String, String> params = new HashMap<String, String>();
        params.put("userid", AppController.getInstance().getUserId());
        params.put("bookid", audioBook.getBook_id());
        params.put("actid", ""+1);
        params.put("content", ""+1);

        String url = getResources().getString(R.string.user_action_url);

// Request a string response from the provided URL.
        JsonUTF8StringRequest stringRequest = new JsonUTF8StringRequest(Request.Method.POST, url,params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("response","response:"+response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {



        };
// Add the request to the RequestQueue.
        AppController.getInstance().addToRequestQueue(stringRequest,"tag_download_book");

    }

    private void logUserComment( String comment){
       // comment="comment ";
        Map<String, String> params = new HashMap<String, String>();
        params.put("userid", AppController.getInstance().getUserId());
        params.put("bookid", audioBook.getBook_id());
        params.put("actid", ""+4);
        params.put("content", comment);

        String url = getResources().getString(R.string.user_action_url);
Log.v("url","url:"+url);
// Request a string response from the provided URL.
        JsonUTF8StringRequest stringRequest = new JsonUTF8StringRequest(Request.Method.POST, url,params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("response","response:"+response);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {



        };
// Add the request to the RequestQueue.
        AppController.getInstance().addToRequestQueue(stringRequest,"tag_comment_book");

    }

}
