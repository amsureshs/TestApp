package audio.lisn.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import audio.lisn.R;
import audio.lisn.activity.AudioBookDetailActivity;
import audio.lisn.adapter.StoreBookGridViewAdapter;
import audio.lisn.app.AppController;
import audio.lisn.model.AudioBook;
import audio.lisn.util.ConnectionDetector;
import audio.lisn.webservice.JsonUTF8ArrayRequest;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StoreFragment.OnStoreBookSelectedListener} interface
 * to handle interaction events.
 * Use the {@link StoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StoreFragment extends Fragment implements StoreBookGridViewAdapter.StoreBookSelectListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    private OnStoreBookSelectedListener mListener;
    GridView gridViewStoreBook;
    ConnectionDetector connectionDetector;
    private ProgressDialog pDialog;
    private List<AudioBook> bookList = new ArrayList<AudioBook>();
    private StoreBookGridViewAdapter storeBookGridViewAdapter;
    private static final String TAG = StoreFragment.class.getSimpleName();
    private AudioBook selectedBook;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StoreFragment.
     */
    public static StoreFragment newInstance() {
        StoreFragment fragment = new StoreFragment();
        return fragment;
    }

    public StoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(
                R.layout.fragment_store, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        gridViewStoreBook=(GridView)view.findViewById(R.id.gridViewStoreBook);


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnStoreBookSelectedListener) activity;
            connectionDetector = new ConnectionDetector(activity.getApplicationContext());

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        loadData();

    }
    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
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
        //   showOption();
    }

    private void setData(JSONArray jsonArray){
        //JSONArray response = jsonArray;
        hidePDialog();

        bookList.clear();
        // Parsing json
        for (int i = 0; (i < jsonArray.length()) ; i++) {
            try {

                JSONObject obj = jsonArray.getJSONObject(i);
                // AudioBook book = new AudioBook();
                String book_id="";
                try{
                    book_id=obj.getString("book_id");
                } catch (JSONException e) {
                    book_id=obj.getString(""+i);
                    e.printStackTrace();
                }
                AudioBook book=new AudioBook(obj,i);


                bookList.add(book);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        // notifying list adapter about data changes
        // so that it renders the list view with updated data
        storeBookGridViewAdapter.notifyDataSetChanged();
    }
    private void loadData() {
        storeBookGridViewAdapter = new StoreBookGridViewAdapter(getActivity()
                .getApplicationContext(),R.layout.store_book_view, bookList);
        storeBookGridViewAdapter.setListener(this);
        gridViewStoreBook.setAdapter(storeBookGridViewAdapter);

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

        }


    }

    @Override
    public void onStoreBookSelect(AudioBook audioBook, AudioBook.SelectedAction btnIndex) {
        selectedBook=audioBook;

        switch (btnIndex){
            case ACTION_PREVIEW:
                break;
            case ACTION_DETAIL:
                showDetailView();
                break;
            case ACTION_PURCHASE:
            default:
                break;

        }
    }

    @Override
    public Activity getListenerActivity() {
        return getActivity();
    }
    private void showDetailView(){
        Intent intent = new Intent(getActivity().getApplicationContext(),
                AudioBookDetailActivity.class);
        intent.putExtra("audioBook", selectedBook);
        startActivity(intent);
    }

    public interface OnStoreBookSelectedListener {
        public void onStoreBookSelected(int position);

    }


}