package rubenkarim.com.masterthesisapp.Utilities;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SharepointUpload {

    public static void UploadToSharepoint() {
        URL url = null;
        try {
            url = new URL("https://catfact.ninja/fact");
        }
        catch (MalformedURLException e) {
            Log.e("URL error", e.toString());
            return;
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        }
        catch (IOException e) {
            Log.e("HttpURLConnection error", e.toString());
            return;
        }

        try {
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            int bufferSize = 1024;
            char[] buffer = new char[bufferSize];
            StringBuilder result = new StringBuilder();
            Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                result.append(buffer, 0, numRead);
            }

            Log.i("Output", result.toString());
        }
        catch (Exception e) {
            Log.e("UploadToSharepoint error", e.toString());
        }
        finally {
            urlConnection.disconnect();
        }
    }
}
