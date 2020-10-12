package dk.stacktrace.messagingforwarder;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class HttpPostThread implements Runnable {
    private static final String TAG = HttpPostThread.class.getName();

    public HttpPostThread(URL url, String message, String slot, String timestamp, String secret) {
        this.url = url;
        this.message = message;
        this.slot = slot;
        this.timestamp = timestamp;
        this.secret = secret;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)this.url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String reqBody = "secret="+ secret + "&message="+message+"&slot="+slot+"&timestamp="+timestamp;






            byte[] bytes = reqBody.getBytes(StandardCharsets.UTF_8);
            OutputStream out = connection.getOutputStream();
            out.write(bytes);
            out.flush();
            int status = connection.getResponseCode();
            Log.i(TAG, "Server replied with HTTP status: " + status);
            out.close();
        }
        catch (IOException e) {
            Log.w(TAG, "Error communicating with HTTP server", e);
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    private final URL url;
    private final String message;
    private final String slot;
    private final String timestamp;
    private final String secret;
}
