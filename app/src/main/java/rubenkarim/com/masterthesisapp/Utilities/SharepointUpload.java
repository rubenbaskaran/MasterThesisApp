package rubenkarim.com.masterthesisapp.Utilities;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class SharepointUpload {

    public static void UploadToSharepoint(Context context) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://catfact.ninja/fact";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.e("http test", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("http test", error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

//        URL url = null;
//        try {
//            url = new URL("https://catfact.ninja/fact");
//        }
//        catch (MalformedURLException e) {
//            Log.e("URL error", e.toString());
//            return;
//        }
//
//        HttpURLConnection urlConnection = null;
//        try {
//            urlConnection = (HttpURLConnection) url.openConnection();
//        }
//        catch (IOException e) {
//            Log.e("HttpURLConnection error", e.toString());
//            return;
//        }
//
//        try {
//            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
//
//            int bufferSize = 1024;
//            char[] buffer = new char[bufferSize];
//            StringBuilder result = new StringBuilder();
//            Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
//                result.append(buffer, 0, numRead);
//            }
//
//            Log.i("Output", result.toString());
//        }
//        catch (Exception e) {
//            Log.e("UploadToSharepoint error", e.toString());
//        }
//        finally {
//            urlConnection.disconnect();
//        }
    }
}
