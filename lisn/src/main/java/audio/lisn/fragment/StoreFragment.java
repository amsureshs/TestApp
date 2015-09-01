package audio.lisn.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import audio.lisn.activity.PlayerControllerActivity;
import audio.lisn.adapter.StoreBookViewAdapter;
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
public class StoreFragment extends Fragment implements  StoreBookViewAdapter.StoreBookSelectListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    private OnStoreBookSelectedListener mListener;
    ConnectionDetector connectionDetector;
    private ProgressDialog pDialog;
    private List<AudioBook> bookList = new ArrayList<AudioBook>();
    private StoreBookViewAdapter storeBookViewAdapter;
    private RecyclerView storeBookView;
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
        storeBookView=(RecyclerView)view.findViewById(R.id.storeBookContainer);
        storeBookView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));

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
            for (int i = 0; (i < jsonArray.length()); i++) {
                try {

                    JSONObject obj = jsonArray.getJSONObject(i);
                    // AudioBook book = new AudioBook();
                    String book_id = "";
                    try {
                        book_id = obj.getString("book_id");
                    } catch (JSONException e) {
                        book_id = obj.getString("" + i);
                        e.printStackTrace();
                    }
                    AudioBook book = new AudioBook(obj, i);


                    bookList.add(book);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


        }

        // notifying list adapter about data changes
        // so that it renders the list view with updated data
        storeBookViewAdapter.notifyDataSetChanged();
    }
    private void loadData() {
        storeBookViewAdapter = new StoreBookViewAdapter(getActivity().getApplicationContext(),bookList);
        storeBookViewAdapter.setStoreBookSelectListener(this);
        storeBookView.setAdapter(storeBookViewAdapter);


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
    public void onStoreBookSelect(View view, AudioBook audioBook, AudioBook.SelectedAction btnIndex) {
        switch (btnIndex){
            case ACTION_PURCHASE:
                AudioBookDetailActivity.navigate((android.support.v7.app.AppCompatActivity) getActivity(), view.findViewById(R.id.book_cover_thumbnail), audioBook);
                break;
            case ACTION_DETAIL:
                PlayerControllerActivity.navigate((android.support.v7.app.AppCompatActivity) getActivity(), view.findViewById(R.id.book_cover_thumbnail), audioBook);
            case ACTION_PLAY:
                PlayerControllerActivity.navigate((android.support.v7.app.AppCompatActivity) getActivity(), view.findViewById(R.id.book_cover_thumbnail), audioBook);

            default:
                break;

        }

    }

    public interface OnStoreBookSelectedListener {
        public void onStoreBookSelected(int position);

    }


}