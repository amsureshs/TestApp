package audio.lisn.webservice;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by Rasika on 7/4/15.
 */
public class JsonUTF8StringRequest extends Request<String> {
    private Response.Listener<String> listener;
    private Map<String, String> params;

    public JsonUTF8StringRequest(int method, String url, Response.ErrorListener listener) {
        super(method, url, listener);
    }
    public JsonUTF8StringRequest(String url, Map<String, String> params,
                                 Response.Listener<String> reponseListener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }

    public JsonUTF8StringRequest(int method, String url, Map<String, String> params,
                                 Response.Listener<String> reponseListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }

    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        Log.v("params", "params:" + params);
        return params;
    };

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, "UTF-8");
            return Response.success(new String(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(String jsonObject) {
        listener.onResponse(jsonObject);

    }
}
