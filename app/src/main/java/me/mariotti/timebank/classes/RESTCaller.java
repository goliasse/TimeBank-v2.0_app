package me.mariotti.timebank.classes;

import android.app.Activity;
import android.os.AsyncTask;
import me.mariotti.timebank.MainActivity;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class RESTCaller extends AsyncTask<String, Integer, JSONObject> {

    public static final int GET_LISTING_LIST = 1;
    public static final int REQUEST_LISTING = 2;
    public static final int UNREQUEST_LISTING = 3;
    public static final int GET_SINGLE_LISTING = 4;
    public static final int GET_MY_PROFILE = 5;
    public static final int GET_CATEGORIES = 6;
    public static final int CREATE_LISTING = 7;
    public static final int EDIT_LISTING = 8;
    public static final int DELETE_LISTING = 9;

    public static final String mServerUrl = "https://agile-headland-8492.herokuapp.com/";
    private final HashMap<String, Object> outDataMap;
    protected String mResourceUrl;
    final String TAG = "RESTCaller";
    protected ListingAdapter mListingAdapter;
    protected Activity mActivity;
    protected int command;
    protected String[] params;
    protected String HttpMethod = "GET";
    protected boolean doOutput = false;
    protected boolean doInput = true;


    public RESTCaller(Activity mActivity, int command, HashMap<String, Object> outDataMap, String... params) {
        this.mActivity = mActivity;
        this.command = command;
        this.outDataMap=outDataMap;
        this.params = params;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject mJSONObject = new JSONObject();
        HttpURLConnection urlConnection = null;
        int responseCode = 1000;
        String responseMessage = "Unhandled error";
        InputStreamReader in = null;
        OutputStream out = null;
        try {

            //Default behaviour is doInput, dont doOutput and use GET method
            URL url = new URL(mServerUrl + mResourceUrl);
            byte[] postDataBytes=null;
            if (doOutput){
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> param : outDataMap.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                postDataBytes = postData.toString().getBytes("UTF-8");

            }
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(HttpMethod);
            if (doOutput){
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            }
            urlConnection.setDoInput(doInput);
            urlConnection.setDoOutput(doOutput);
            if (User.isLogged) {
                urlConnection.setRequestProperty("Authorization", "Basic " + MainActivity.loggedUser.userCredentials);
            }
            if (doOutput) {
                out = urlConnection.getOutputStream();
                out.write(postDataBytes);
            }
            if (doInput) {
                in = new InputStreamReader(urlConnection.getInputStream(), "UTF-8");
            }

            responseCode = urlConnection.getResponseCode();
            responseMessage = urlConnection.getResponseMessage();
        } catch (Exception e) {
            /*An error 4xx throw an exception. Here catch the error number after the exception is fired. If
            urlConnection is not null but accessing it throws an IOException uses the default error code and get the
             exception toString() as error message. If urlConnection is null uses the default error code and message. */
            try {
                responseCode = urlConnection != null ? urlConnection.getResponseCode() : responseCode;
                responseMessage = urlConnection != null ? urlConnection.getResponseMessage() : responseMessage;
            } catch (IOException e1) {
                responseMessage = e1.toString();
            }
        } finally {
            mJSONObject = JsonUtils.urlResponseToJson(in, responseCode, responseMessage);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return mJSONObject;
    }
}



