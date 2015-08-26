package audio.lisn.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import audio.lisn.R;
import audio.lisn.activity.AudioBookDetailActivity;
import audio.lisn.activity.HomeActivity;
import audio.lisn.adapter.AudioBookListAdapter;
import audio.lisn.app.AppController;
import audio.lisn.model.AudioBook;
import audio.lisn.model.DownloadedAudioBook;
import audio.lisn.util.AppUtils;
import audio.lisn.util.AudioPlayerService;
import audio.lisn.util.ConnectionDetector;
import audio.lisn.view.PlayerControllerView;
import audio.lisn.webservice.JsonUTF8ArrayRequest;

public class AudioBookListFragment extends Fragment implements
		OnRefreshListener, AudioBookListAdapter.ItemSelectListener {

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	// Log tag
	private static final String TAG = HomeActivity.class.getSimpleName();
	// http://rtech.neocities.org
	// private static final String url =
	// "http://rtech.neocities.org/matching.json";
	// matching_si
	private ProgressDialog pDialog;
    private List<AudioBook> bookList = new ArrayList<AudioBook>();
	private ListView listView;
	private AudioBookListAdapter adapter;
	SwipeRefreshLayout swipeLayout;
    PlayerControllerView audioPlayerLayout;
    ConnectionDetector connectionDetector;



    public static AudioBookListFragment newInstance(int sectionNumber) {
		AudioBookListFragment fragment = new AudioBookListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public AudioBookListFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_audio_book_list, container, false);
		swipeLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.swipe_container);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

        audioPlayerLayout = (PlayerControllerView) rootView.findViewById(R.id.audio_player_layout);

        listView = (ListView) rootView.findViewById(R.id.list);

		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	/*
	 * @Override public void onRefresh() { new Handler().postDelayed(new
	 * Runnable() {
	 * 
	 * @Override public void run() { swipeLayout.setRefreshing(false); } },
	 * 5000); }
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        connectionDetector = new ConnectionDetector(activity.getApplicationContext());

        // loadData();
		// ((HomeActivity) activity).onSectionAttached(
		// getArguments().getInt(ARG_SECTION_NUMBER));

	}
    @Override
    public void onResume() {
        super.onResume();
        Log.v("onResume ","AudioBookListFragment");
        if((AudioPlayerService.mediaPlayer!=null) && (AudioPlayerService.hasStartedPlayer)){
            audioPlayerLayout.setVisibility(View.VISIBLE);
        }else{
            audioPlayerLayout.setVisibility(View.GONE);

        }
        loadData();
        audioPlayerLayout.updateView();

        registerBroadcastReceiver();

    }

	private void hidePDialog() {
		if (pDialog != null) {
			pDialog.dismiss();
			pDialog = null;
		}
	}

    private void loadDownloadedData() {
        bookList.clear();
        DownloadedAudioBook downloadedAudioBook=new DownloadedAudioBook(getActivity().getApplicationContext());
        downloadedAudioBook.readFileFromDisk(getActivity().getApplicationContext());
        HashMap< String, AudioBook> hashMap=downloadedAudioBook.getBookList();
        for (AudioBook item : hashMap.values()) {
            bookList.add(item);
        }
        adapter.notifyDataSetChanged();


    }
    private AudioBook getDownloadedBook(String key){
        AudioBook returnBook=null;
        DownloadedAudioBook downloadedAudioBook=new DownloadedAudioBook(getActivity().getApplicationContext());
        downloadedAudioBook.readFileFromDisk(getActivity().getApplicationContext());
        HashMap< String, AudioBook> hashMap=downloadedAudioBook.getBookList();

        returnBook=hashMap.get(key);
        return  returnBook;
    }

	private void downloadData() {
        String url=getString(R.string.book_list_url);

        JsonUTF8ArrayRequest bookListReq = new JsonUTF8ArrayRequest(url, null,

				new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {

                        AppController.getInstance().setBookList(jsonArray);
                        setData(jsonArray);

                    }
                }, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.d(TAG, "Error: " + error.getMessage());
						hidePDialog();

					}
				});
        bookListReq.setShouldCache(true);
		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(bookListReq,"tag_boo_list");
	}

    private void setData(JSONArray jsonArray){
        //JSONArray response = jsonArray;
        hidePDialog();
        swipeLayout.setRefreshing(false);

        bookList.clear();
        // Parsing json
        for (int i = 0; i < jsonArray.length(); i++) {
            try {

                String book_id="";
                JSONObject obj = jsonArray.getJSONObject(i);
                // AudioBook book = new AudioBook();

                try{
                    book_id=obj.getString("book_id");
                } catch (JSONException e) {
                    book_id=obj.getString(""+i);
                    e.printStackTrace();
                }
                AudioBook book=getDownloadedBook(book_id);

                if(book == null){
                    book = new AudioBook();
                }
                book.setBook_id(book_id);
                book.setISBN(book_id);

                book.setAuthor(obj.getString("author"));
                book.setCategory(obj.getString("category"));
                book.setCover_image(obj.getString("cover_image"));
                book.setDescription(obj.getString("description"));
                book.setLanguage(obj.getString("language"));
                book.setPreview_audio(obj.getString("preview_audio"));
                book.setPrice(obj.getString("price"));
                book.setTitle(obj.getString("title"));
                book.setEnglish_title(obj.getString("english_title"));
                book.setRate(obj.getString("rate"));
                book.setDuration(obj.getString("duration"));
                book.setNarrator(obj.getString("narrator"));
                book.setDownloadCount(obj.getInt("downloads"));
                JSONArray arr = obj.getJSONArray("audio_file");
                String[] list = new String[arr.length()];
                for(int index = 0; index< arr.length(); index++) {
                    list[index] = arr.getString(index);
                }

                book.setAudio_file_urls(list);

                // adding profile to profile array
                bookList.add(book);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        // notifying list adapter about data changes
        // so that it renders the list view with updated data
        adapter.notifyDataSetChanged();
    }
    private void loadData() {
		adapter = new AudioBookListAdapter(getActivity()
				.getApplicationContext(), bookList);
		adapter.setListener(this);
		listView.setAdapter(adapter);

        if (connectionDetector.isConnectingToInternet()) {

            pDialog = new ProgressDialog(getActivity());
            // Showing progress dialog before making http request

            pDialog.setMessage(getString(R.string.loading_text));
            pDialog.show();


            // Cache data not exist.
            JSONArray jsonArray = AppController.getInstance().getBookList();
            if (jsonArray != null) {
                setData(jsonArray);
            } else {
                downloadData();
            }

        }else{
            loadDownloadedData();
        }


	}
    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPlayerUpdateReceiver);
        super.onPause();
    }
    @Override
	public void onRefresh() {
		downloadData();
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelect(AudioBook book) {
		Intent intent = new Intent(getActivity().getApplicationContext(),
				AudioBookDetailActivity.class);
		intent.putExtra("audioBook", book);
		startActivity(intent);
		// MatchingProfileDetailActivity matchingProfileDetailActivity= new
		// MatchingProfileDetailActivity();

	}
    @Override
    public void OnLongClickListener(final AudioBook audioBook){

        if(audioBook.isPurchase()) {
            AlertDialog confirmationDialog = new AlertDialog.Builder(getActivity())
                    //set message, title, and icon
                    .setTitle("")
                    .setMessage("Do you want to Delete")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            //your deleting code
                            Log.v("audioBook", "audioBook :" + audioBook.getBook_id());
                            deleteAudioBook(audioBook);
                            dialog.dismiss();
                        }

                    })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    })
                    .create();

            confirmationDialog.show();
        }

    }

    private void stopPlayer(AudioBook audioBook) {
        if (AudioPlayerService.mediaPlayer != null) {
String playingBookId=AppController.getInstance().getPlayingBookId();
            if(playingBookId.equalsIgnoreCase(audioBook.getBook_id()) ){
                AudioPlayerService.mediaPlayer.stop();
                AudioPlayerService.mediaPlayer.release();
                AudioPlayerService.mediaPlayer=null;
                audioPlayerLayout.setVisibility(View.GONE);
            }
        }
    }
private void deleteAudioBook(AudioBook audioBook){
    stopPlayer(audioBook);
    for (String fileName : audioBook.getDownloadedFileList().keySet()){
        String filePath=AppUtils.getDataDirectory(getActivity().getApplicationContext())+audioBook.getBook_id()+"/"+fileName+".mp3";
        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
        }
    }

    audioBook.removeDownloadedFile();
    DownloadedAudioBook downloadedAudioBook = new DownloadedAudioBook(
            getActivity().getApplicationContext());
    downloadedAudioBook.readFileFromDisk(getActivity().getApplicationContext());
    downloadedAudioBook.addBookToList(getActivity().getApplicationContext(),
            audioBook.getBook_id(), audioBook);

}
    private void registerBroadcastReceiver(){
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mPlayerUpdateReceiver,
                new IntentFilter("audio-event"));
    }
    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mPlayerUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioPlayerLayout.updateView();
        }
    };
}
