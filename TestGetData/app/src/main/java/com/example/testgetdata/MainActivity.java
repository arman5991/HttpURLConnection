package com.example.testgetdata;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // Debug log tag.
    private static final String TAG_HTTP_URL_CONNECTION = "HTTP_URL_CONNECTION";
    // Child thread sent message type value to activity main thread Handler.
    private static final int REQUEST_CODE_SHOW_RESPONSE_TEXT = 1;
    // The key of message stored server returned data.
    private static final String KEY_RESPONSE_TEXT = "KEY_RESPONSE_TEXT";
    // Request method GET. The value must be uppercase.
    private static final String REQUEST_METHOD_GET = "GET";
    // Request web page url input text box.
    private EditText requestUrlEditor = null;
    // Send http request button.
    private Button requestUrlButton = null;
    // TextView to display server returned page html text.
    private TextView responseTextView = null;
    // This handler used to listen to child thread show return page html text message and display those text in responseTextView.
    private Handler uiUpdater = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init app ui controls.
        initControls();

        // When click request url button.
        requestUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reqUrl = requestUrlEditor.getText().toString();

                checkURLValid(reqUrl);
            }
        });
    }

    private void checkURLValid(String reqUrl) {
        if (!TextUtils.isEmpty(reqUrl)) {
            if (URLUtil.isHttpUrl(reqUrl) || URLUtil.isHttpsUrl(reqUrl)) {
                startSendHttpRequestThread(reqUrl);
            } else {
                Toast.makeText(getApplicationContext(), "The request url is not a valid http or https url.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "The request url can not be empty.", Toast.LENGTH_LONG).show();
        }
    }


    // Initialize app controls.
    @SuppressLint("HandlerLeak")
    private void initControls() {
        if (requestUrlEditor == null) {
            requestUrlEditor = findViewById(R.id.http_url_editor);
        }

        if (requestUrlButton == null) {
            requestUrlButton = findViewById(R.id.http_url_request_button);
        }

        if (responseTextView == null) {
            responseTextView = findViewById(R.id.http_url_response_text_view);
        }

        // This handler is used to wait for child thread message to update server response text in TextView.
        if (uiUpdater == null) {
            uiUpdater = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == REQUEST_CODE_SHOW_RESPONSE_TEXT) {
                        Bundle bundle = msg.getData();
                        if (bundle != null) {
                            String responseText = bundle.getString(KEY_RESPONSE_TEXT);
                            responseTextView.setText(responseText);
                        }
                    }
                }
            };
        }
    }

    /* Start a thread to send http request to web server use HttpURLConnection object. */
    private void startSendHttpRequestThread(final String reqUrl) {
        Thread sendHttpRequestThread = new Thread() {
            @Override
            public void run() {
                // Http url connection.
                HttpURLConnection httpConn = null;

                // Read text input stream.
                InputStreamReader isReader = null;

                // Read text into buffer.
                BufferedReader bufReader = null;

                // Save server response text.
                StringBuilder readTextBuf = new StringBuilder();

                try {
                    // Create a URL object use page url.
                    URL url = new URL(reqUrl);

                    // Open http connection to web server.
                    httpConn = (HttpURLConnection) url.openConnection();

                    // Set http request method to get.
                    httpConn.setRequestMethod(REQUEST_METHOD_GET);

                    // Set connection timeout and read timeout value.
                    httpConn.setConnectTimeout(10000);
                    httpConn.setReadTimeout(10000);

                    // Get input stream from web url connection.
                    InputStream inputStream = httpConn.getInputStream();

                    // Create input stream reader based on url connection input stream.
                    isReader = new InputStreamReader(inputStream);

                    // Create buffered reader.
                    bufReader = new BufferedReader(isReader);

                    // Read line of text from server response.
                    String line = bufReader.readLine();

                    // Loop while return line is not null.
                    while (line != null) {
                        // Append the text to string buffer.
                        readTextBuf.append(line);

                        // Continue to read text line.
                        line = bufReader.readLine();
                    }

                    // Send message to main thread to update response text in TextView after read all.
                    Message message = new Message();

                    // Set message type.
                    message.what = REQUEST_CODE_SHOW_RESPONSE_TEXT;

                    // Create a bundle object.
                    Bundle bundle = new Bundle();
                    // Put response text in the bundle with the special key.
                    bundle.putString(KEY_RESPONSE_TEXT, readTextBuf.toString());
                    // Set bundle data in message.
                    message.setData(bundle);
                    // Send message to main thread Handler to process.
                    uiUpdater.sendMessage(message);

                } catch (MalformedURLException ex) {

                    Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);

                } catch (IOException ex) {

                    Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);

                } finally {
                    try {
                        if (bufReader != null) {
                            bufReader.close();
                        }

                        if (isReader != null) {
                            isReader.close();
                        }

                        if (httpConn != null) {
                            httpConn.disconnect();
                        }
                    } catch (IOException ex) {
                        Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);
                    }
                }
            }
        };
        // Start the child thread to request web page.
        sendHttpRequestThread.start();
    }
}