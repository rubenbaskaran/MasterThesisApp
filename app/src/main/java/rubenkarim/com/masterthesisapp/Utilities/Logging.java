package rubenkarim.com.masterthesisapp.Utilities;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logging {
    public static void error(String methodName, Exception exception){
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString();
        Log.e("Error - " + methodName, stackTrace);
    }
}