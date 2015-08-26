package audio.lisn.app;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;

import audio.lisn.model.AudioBook;
import audio.lisn.util.AudioPlayerService;
import audio.lisn.util.AudioPlayerService.AudioPlayerServiceBinder;
import audio.lisn.util.LruBitmapCache;
import audio.lisn.util.PreviewAudioPlayerService;
import io.fabric.sdk.android.Fabric;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private String userName,password,userId,gcmRegId;
    Intent playbackServiceIntent;
    private String[] fileList;
    AudioPlayerService mService;
    boolean mBound = false;
    public int fileIndex=-1;
    private JSONArray bookList;
    // private  String playingBookId;
    private AudioBook currentAudioBook;
    int retryCount=0;
    private static AppController mInstance;

    Intent previewPlaybackServiceIntent;
    PreviewAudioPlayerService mPreviewService;
    boolean mPreviewBound = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;

    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public Intent getPlaybackServiceIntent(){
        if (playbackServiceIntent == null) {
            playbackServiceIntent = new Intent(getApplicationContext(), AudioPlayerService.class);
            bindService(playbackServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        }
        return this.playbackServiceIntent;
    }
    public Intent getPreviewPlaybackServiceIntent(){
        if (previewPlaybackServiceIntent == null) {
            previewPlaybackServiceIntent = new Intent(getApplicationContext(), PreviewAudioPlayerService.class);
            bindService(previewPlaybackServiceIntent, mPreviewConnection, Context.BIND_AUTO_CREATE);

        }
        return this.previewPlaybackServiceIntent;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGcmRegId() {
        return gcmRegId;
    }

    public void setGcmRegId(String gcmRegId) {
        this.gcmRegId = gcmRegId;
    }

    public String getAudioFileName() {
        return currentAudioBook.getEnglish_title();
    }
    public String getPlayerControllerTitle() {
        String title="";
        if(currentAudioBook != null) {
            if (fileIndex >= 0 && fileIndex < (fileList.length)) {
                title= currentAudioBook.getEnglish_title() + "[ " + (fileIndex + 1) + " / " + fileList.length + " ]";
            } else {
                title= currentAudioBook.getEnglish_title();
            }
        }
        return title;
    }



    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance

            AudioPlayerServiceBinder binder = (AudioPlayerServiceBinder) service;
            mService = binder.getService();
            mBound = true;
            if(fileIndex ==-1){
                //   playNextFile();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mPreviewConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance

            PreviewAudioPlayerService.PreviewAudioPlayerServiceBinder binder = (PreviewAudioPlayerService.PreviewAudioPlayerServiceBinder) service;
            mPreviewService = binder.getService();
            mPreviewBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPreviewBound = false;
        }
    };
    public void playNextFile(){
        Log.v("playNextFile", "mBound :" + mBound);

        if (mBound && fileList != null) {
            retryCount=0;
            fileIndex++;
            Log.v("playNextFile", "fileIndex :" + fileIndex);

            if(fileIndex>=0 && fileIndex<(fileList.length)) {
                Log.v("playNextFile", "fileIndex :" + fileIndex);

                String fileName=fileList[fileIndex];
                mService.playAudioFile(fileName);
                if(currentAudioBook.getLastPlayFileIndex() ==fileIndex){
                    mService.setSeekPosition(currentAudioBook.getLastSeekPoint());
                }else{
                    mService.setSeekPosition(0);

                }

            }else{
                fileIndex=fileList.length;
            }
            currentAudioBook.setLastSeekPoint(0);
            currentAudioBook.setLastPlayFileIndex(0);
        }
        if(!mBound){


            Log.v("playNextFile", "mBound :" + fileIndex);
            if(++retryCount<5){

                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        Intent playbackServiceIntent1=AppController.getInstance().getPlaybackServiceIntent();
                        stopService(playbackServiceIntent1);
                        startService(playbackServiceIntent1);
                        playNextFile();
                    }
                }, 5);
            }


        }
    }
    public void playPreviousFile(){
        if (mBound && fileList != null) {
            fileIndex--;
            if(fileIndex>=0 && fileIndex<(fileList.length)) {
                Log.v("playPreviousFile", "fileIndex :" + fileIndex);

                String fileName=fileList[fileIndex];
                mService.playAudioFile(fileName);
            }else{
                fileIndex=-1;
            }
        }
    }
    public void stopPlayer(){
        fileIndex=-1;
    }

    public String[] getFileList() {
        return fileList;
    }

    public void setFileList(String[] fileList) {
        this.fileList = fileList;
    }

    public JSONArray getBookList() {
        return bookList;
    }

    public void setBookList(JSONArray bookList) {
        this.bookList = bookList;
    }

    public String getPlayingBookId() {

        return currentAudioBook.getBook_id();
    }



    public AudioBook getCurrentAudioBook() {
        if(fileIndex<1){
            currentAudioBook.setLastPlayFileIndex(0);
        }else{
            currentAudioBook.setLastPlayFileIndex(fileIndex);
        }
        currentAudioBook.setLastSeekPoint(mService.getSeekPosition());
        return currentAudioBook;
    }

    public void setCurrentAudioBook(AudioBook currentAudioBook) {
        this.currentAudioBook = currentAudioBook;
    }


    public void playPreviewFile(final String fileUrl){
        Log.v("playNextFile", "mBound :" + mPreviewBound);



        if (mPreviewBound) {
            stopService(previewPlaybackServiceIntent);
            startService(previewPlaybackServiceIntent);
            mPreviewService.playAudioFile(fileUrl);

        }
        if(!mPreviewBound){

            if(++retryCount<5){

                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        Intent playbackServiceIntent1=AppController.getInstance().getPreviewPlaybackServiceIntent();
                        stopService(playbackServiceIntent1);
                        startService(playbackServiceIntent1);
                        playPreviewFile(fileUrl);
                    }
                }, 5);
            }


        }
    }
}
