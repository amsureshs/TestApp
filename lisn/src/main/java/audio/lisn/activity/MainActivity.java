package audio.lisn.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import audio.lisn.R;
import audio.lisn.app.AppController;
import audio.lisn.util.ConnectionDetector;
import audio.lisn.webservice.JsonUTF8ArrayRequest;

public class MainActivity extends Activity {

    ConnectionDetector connectionDetector;
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    ProgressBar progressBar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        progressBar=(ProgressBar)findViewById(R.id.lodingProgressBar);
        connectionDetector = new ConnectionDetector(getApplicationContext());


            // String userName =
		// AppDataManager.getData(getApplicationContext(),Constants.USER_NAME);
		if (!isUserLogin()) {
			Intent intent = new Intent(getApplicationContext(),
					LoginActivity.class);
            startActivityForResult(intent, 1);

        } else {
            if (connectionDetector.isConnectingToInternet()) {
                downloadData();
            }else{

                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        loadHome();
                    }
                }, SPLASH_DISPLAY_LENGTH);
            }

		}

	}

    private void loadHome(){
        progressBar.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(getApplicationContext(),
                HomeActivity.class);
        startActivity(intent);
        finish();
    }
    private void downloadData() {
        progressBar.setVisibility(View.VISIBLE);

        String url=getString(R.string.book_list_url);

        // Creating volley request obj
        JsonUTF8ArrayRequest bookListReq = new JsonUTF8ArrayRequest(url, null,

                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {

                        AppController.getInstance().setBookList(jsonArray);
                        loadHome();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadHome();

            }
        });
        bookListReq.setShouldCache(true);
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(bookListReq,"tag_boo_list");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                downloadData();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult
    private boolean isUserLogin(){
        SharedPreferences sharedPref =getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean loginStatus = sharedPref.getBoolean(getString(R.string.user_login_status), false);
        String loginId = sharedPref.getString(getString(R.string.user_login_id), "5");
        AppController.getInstance().setUserId(loginId);

        return loginStatus;
    }

}
