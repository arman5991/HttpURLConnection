package json.com.json;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Debug log tag.
    private static final String TAG_HTTP_URL_CONNECTION = "HTTP_URL_CONNECTION";

    // Child thread sent message type value to activity main thread Handler.
    private static final int REQUEST_CODE_SHOW_RESPONSE_JSON = 1;

    // The key of message stored server returned data.
    private static final String KEY_RESPONSE_JSON = "KEY_RESPONSE_JSON";

    // Connection and reader time out millisecond
    private static final int TIME_OUT = 10000;

    // Request method GET. The value must be uppercase.
    private static final String REQUEST_METHOD_GET = "GET";

    // BASE_URL
    private static final String BASE_URL = "https://api.learn2crack.com";

    // Endpoint
    private static final String URL_END_POINT = "/android/jsonandroid/";

    // Full URL
    private static final String FULL_URL = BASE_URL + URL_END_POINT;

    // RecyclerView to show from server returned android data.
    private RecyclerView recyclerView;

    // Android list for recyclerView adapter
    private List<Android> modelList = new ArrayList<>();

    // This handler used to listen to child thread show return json
    private Handler uiUpdater = null;

    // Data
    private String json;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRecyclerView();

        if (uiUpdater == null) {
            uiUpdater = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == REQUEST_CODE_SHOW_RESPONSE_JSON) {
                        Bundle bundle = msg.getData();
                        if (bundle != null) {

                            json = bundle.getString(KEY_RESPONSE_JSON);

                            jsonParsAsMapData();
//                            jsonParsAsGsonData();

                            recyclerView.setAdapter(new RecyclerViewAdapter(modelList));
                        }
                    }
                }
            };
        }

        getJSON(FULL_URL, TIME_OUT);
    }

    private void jsonParsAsGsonData() {
        AndroidVersions data = new Gson().fromJson(json, AndroidVersions.class);
        modelList = data.getAndroid();
    }

    private void jsonParsAsMapData() {
        try {
            JSONObject jsonObjects = new JSONObject(json);
            JSONArray jsonArray = jsonObjects.getJSONArray("android");

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.has("ver") && jsonObject.has("name") && jsonObject.has("api")) {

                    String ver = jsonObject.getString("ver");
                    String name = jsonObject.getString("name");
                    String api = jsonObject.getString("api");

                    Android model = new Android();
                    model.setVer(ver);
                    model.setName(name);
                    model.setApi(api);

                    modelList.add(model);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    public void getJSON(final String reqUrl, final int timeout) {
        Thread sendHttpRequestThread = new Thread() {
            @Override
            public void run() {

                // Http url connection.
                HttpURLConnection connection = null;

                // Read text input stream.
                InputStreamReader isReader = null;

                // Read text into buffer.
                BufferedReader bufReader = null;

                // Save server response text.
                StringBuilder stringBuilder = new StringBuilder();

                try {

                    // Create a URL object use page url.
                    URL url = new URL(reqUrl);

                    // Open http connection to web server.
                    connection = (HttpURLConnection) url.openConnection();

                    // Set http request method to get.
                    connection.setRequestMethod(REQUEST_METHOD_GET);

                    // Set connection timeout and read timeout value.
                    connection.setConnectTimeout(timeout);
                    connection.setReadTimeout(timeout);

                    // Connect to server
                    connection.connect();

                    // Response status code
                    int status = connection.getResponseCode();

                    if (status == 200) {

                        // Get input stream from web url connection.
                        InputStream inputStream = connection.getInputStream();

                        // Create input stream reader based on url connection input stream.
                        isReader = new InputStreamReader(inputStream);

                        // Create buffered reader.
                        bufReader = new BufferedReader(isReader);

                        // Read line of text from server response.
                        String line = bufReader.readLine();

                        while (line != null) {
                            // Append the text to string buffer.
                            stringBuilder.append(line).append("\n");

                            // Continue to read text line.
                            line = bufReader.readLine();
                        }

                        bufReader.close();

                        // Init send message
                        Message message = getMessage(stringBuilder);

                        // Send message to main thread Handler to process.
                        updateUI(message);

                    } else {
                        Log.e(TAG_HTTP_URL_CONNECTION, "status = " + status);
                    }

                } catch (MalformedURLException ex) {

                    Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);

                } catch (IOException ex) {

                    Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);

                } finally {

                    if (connection != null) {
                        try {
                            connection.disconnect();

                        } catch (Exception ex) {

                            Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);
                        }
                    }
                }
            }
        };

        // Start the child thread to request web page.
        sendHttpRequestThread.start();
    }

    private void updateUI(Message message) {
        uiUpdater.sendMessage(message);
    }

    @NonNull
    private Message getMessage(StringBuilder stringBuilder) {
        // Send message to main thread.
        Message message = new Message();

        // Set message type.
        message.what = REQUEST_CODE_SHOW_RESPONSE_JSON;

        // Create a bundle object.
        Bundle bundle = new Bundle();

        // Put response text in the bundle with the special key.
        bundle.putString(KEY_RESPONSE_JSON, stringBuilder.toString());

        // Set bundle data in message.
        message.setData(bundle);
        return message;
    }
}
