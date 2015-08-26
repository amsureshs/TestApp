package audio.lisn.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import audio.lisn.R;
import audio.lisn.app.AppController;
import audio.lisn.webservice.JsonUTF8StringRequest;

public class LoginActivity extends Activity {
	EditText userName, password;
	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		uiHelper = new UiLifecycleHelper(this, statusCallback);
		uiHelper.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		LoginButton authButton = (LoginButton) findViewById(R.id.authButton);


		authButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						if (user != null) {

//							Log.v("You are currently logged in as ",
//									user.getName());
                            addUser(user);
						} else {
							Log.v("You are not logged in.",
									"You are not logged in");
						}

					}

				});

		authButton.setReadPermissions(Arrays.asList("public_profile"));

//		userName = (EditText) findViewById(R.id.userName);
//		password = (EditText) findViewById(R.id.password);
//		Button loginBtn = (Button) findViewById(R.id.btn_login);
//		loginBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				loginToHome();
//
//				// onClickLogin();
//
//			}
//
//		});




	}

	public  void showHashKey(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					"audio.lisn", PackageManager.GET_SIGNATURES); // Your
																		// package
																		// name
						String key="";												// here
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.i("KeyHash:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
                key=Base64.encodeToString(md.digest(), Base64.DEFAULT);
			}
          //  sendMail(key);

        } catch (NameNotFoundException e) {
		} catch (NoSuchAlgorithmException e) {
		}
	}

	private void showAlert(GraphUser user) {
		Log.v("GraphUser", user.getName());
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (session != null) {
			if (!session.isOpened() && !session.isClosed()) {
				session.openForRead(new Session.OpenRequest(this)
						.setPermissions(Arrays.asList("public_profile"))
						.setCallback(statusCallback));
			} else {
				Session.openActiveSession(this, true, statusCallback);
			}
		}
	}
private  void userAddedSuccess(boolean status){
    if(status) {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }else{
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setMessage("The server is unavailable. Please try again later.").setPositiveButton(
                "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
    private void addUser(final GraphUser user){
        String url=getString(R.string.add_user_url);

        String username="NULL";
        String fbname="NULL";
        String loc="NULL";
        String bday="NULL";
        String email="NULL";
        String mobile="NULL";
        String age="NULL";
        String pref="NULL";
        String fbid="NULL";
        String fname="NULL";
        String mname="NULL";
        String lname="NULL";
        String fburl="NULL";

        if(user.getName() !=null){
            username=user.getName();
        }
        if(user.getUsername() !=null){
            fbname=user.getUsername();
        }
        if(user.getBirthday() !=null){
            bday=user.getBirthday();
        }
        if(user.getLocation() !=null){
            GraphPlace place=user.getLocation();
            loc=place.getName();
        }
        if(user.getId() !=null){
            fbid=user.getId();
        }
        if(user.getFirstName() !=null){
            fname=user.getFirstName();
        }
        if(user.getMiddleName() !=null){
            mname=user.getMiddleName();
        }
        if(user.getLastName() !=null){
            lname=user.getLastName();
        }
        if(user.getLink() !=null){
            fburl=user.getLink();
        }
        Map<String, String> postParam = new HashMap<String, String>();

        try {
            postParam.put("username",username);
            postParam.put("fbname",fbname);
            postParam.put("location", loc);
            postParam.put("birthDay",bday);
            postParam.put("email",email);
            postParam.put("mobile",mobile);
            postParam.put("age",age);
            postParam.put("pref",pref);
            postParam.put("fbid",fbid);
            postParam.put("fname",fname);
            postParam.put("mname",mname);
            postParam.put("lname",lname);
            postParam.put("fburl", fburl);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       // Map<String,String> postParam = new HashMap<String, String>();

        JsonUTF8StringRequest bookListReq = new JsonUTF8StringRequest(Request.Method.POST,url, postParam,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("response", "response :" + response);

                        //SUCCESS: UID=5
                        Log.v("response", "respondString :" + response);

                        String[] separated = response.split(":");
                        if((separated[0].trim().equalsIgnoreCase("SUCCESS")) ||(separated[0].trim().equalsIgnoreCase("EXIST")) ){

                            if(separated[1] !=null) {
                                String uid="0";
                                String[] separated2 = separated[1].split("=");
                                if(separated2[1] !=null) {
                                    uid = separated2[1].trim();
                                }
                                loginSuccess(uid);
                                Log.v("response", "uid :" + uid);
                            }

                            userAddedSuccess(true);
                        }else{
                            userAddedSuccess(false);
                        }
                        Log.v("response","response :"+response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                 Log.v("response","error :"+error.getMessage());
                NetworkResponse response = error.networkResponse;
                if(response !=null) {
                    Log.v("response", response.statusCode + " data: " + response.data.toString());
                }

                // sendMail("Error Message: statusCode: "+response.statusCode+" data: "+ response.data.toString());

                userAddedSuccess(false);
            }
        });
        bookListReq.setShouldCache(false);
        AppController.getInstance().addToRequestQueue(bookListReq, "tag_search_book");


    }
    /*
    private void addUser(final GraphUser user){
        String url=getString(R.string.add_user_url);

        StringRequest userAddReq = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //SUCCESS: UID=5
                String[] separated = response.split(":");
                if((separated[0].trim().equalsIgnoreCase("SUCCESS")) ||(separated[0].trim().equalsIgnoreCase("EXIST")) ){

                    if(separated[1] !=null) {
                        String uid="0";
                        String[] separated2 = separated[1].split("=");
                        if(separated2[1] !=null) {
                            uid = separated2[1].trim();

                        }
                        loginSuccess(uid);
                        Log.v("response", "uid :" + uid);

                    }

                    userAddedSuccess(true);
                }else{
                    sendMail("Respond : "+ response);

                    userAddedSuccess(false);
                }
                Log.v("response","response :"+response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Log.v("response","error :"+error.);
                NetworkResponse response = error.networkResponse;

                sendMail("Error Message: statusCode: "+response.statusCode+" data: "+ response.data.toString());

                userAddedSuccess(false);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                String username="NULL";
                String fbname="NULL";
                String loc="NULL";
                String bday="NULL";
                String email="NULL";
                String mobile="NULL";
                String age="NULL";
                String pref="NULL";
                String fbid="NULL";
                String fname="NULL";
                String mname="NULL";
                String lname="NULL";
                String fburl="NULL";

                if(user.getName() !=null){
                    username=user.getName();
                }
                if(user.getUsername() !=null){
                    fbname=user.getUsername();
                }
                if(user.getBirthday() !=null){
                    bday=user.getBirthday();
                }
                if(user.getLocation() !=null){
                    GraphPlace place=user.getLocation();
                    loc=place.getName();
                }
                if(user.getId() !=null){
                    fbid=user.getId();
                }
                if(user.getFirstName() !=null){
                    fname=user.getFirstName();
                }
                if(user.getMiddleName() !=null){
                    mname=user.getMiddleName();
                }
                if(user.getLastName() !=null){
                    lname=user.getLastName();
                }
                if(user.getLink() !=null){
                    fburl=user.getLink();
                }
                Map<String,String> params = new HashMap<String, String>();
                params.put("username",username);
                params.put("fbname",fbname);
                params.put("location", loc);
                params.put("birthDay",bday);
                params.put("email",email);
                params.put("mobile",mobile);
                params.put("age",age);
                params.put("pref",pref);
                params.put("fbid",fbid);
                params.put("fname",fname);
                params.put("mname",mname);
                params.put("lname",lname);
                params.put("fburl",fburl);
                Log.v("params",params.toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/json; charset=utf-8");
                return params;
            }
        };
        sendMail("userAddReq:  "+userAddReq.toString());

        AppController.getInstance().addToRequestQueue(userAddReq,"tag_add_user");

    }
    */
    private  void sendMail(String message) {

		/* Create the Intent */
        final Intent emailIntent = new Intent(
                android.content.Intent.ACTION_SEND);
        String messageBody = "<b>Message:</b> " + message;


		/* Fill it with Data */
        emailIntent.setType("text/html");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[] { "" });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "DEBUG");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                Html.fromHtml(messageBody));

		/* Send it off to the Activity-Chooser */
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));

    }

    private void loginSuccess(String user_id) {
        SharedPreferences sharedPref =getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.user_login_id),user_id);
        editor.putBoolean(getString(R.string.user_login_status),true);
        editor.commit();
        AppController.getInstance().setUserId(user_id);

	}


	/*
	 * private Session.StatusCallback statusCallback = new
	 * Session.StatusCallback() {
	 * 
	 * @Override public void call(Session session, SessionState state, Exception
	 * exception) { if (state.isOpened()) { Log.d("MainActivity",
	 * "Facebook session opened."); } else if (state.isClosed()) {
	 * Log.d("MainActivity", "Facebook session closed.");
	 * 
	 * } } };
	 */

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			// Respond to session state changes, ex: updating the view
			if (state.isOpened()) {
				Log.d("MainActivity", "Facebook session opened.");
			} else if (state.isClosed()) {
				Log.d("MainActivity", "Facebook session closed.");

			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		uiHelper.onSaveInstanceState(savedState);
	}

}
