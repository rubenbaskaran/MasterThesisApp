package rubenkarim.com.masterthesisapp.Utilities;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Logging {
    public static void error(Context context, String methodName, Exception exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString();
        Log.e("Error - " + methodName, stackTrace);
        writeToLog(context, methodName, stackTrace);
    }
    public static void info(Context context, String tag, String message) {
        Log.i(tag, message);
        writeToLog(context,tag, message);
    }

    private static void writeToLog(Context context, String methodName, String stackTrace) {
        File logDirectory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "/Masterthesislogs/");
        boolean isDirectoryCreated = logDirectory.exists();
        if (!isDirectoryCreated){
            isDirectoryCreated = logDirectory.mkdirs();
        }

        if (isDirectoryCreated) {
            String filepath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/Masterthesislogs/log.txt";
            File file = new File(filepath);
            StringBuilder stringBuilder = new StringBuilder();
            String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Timestamp(System.currentTimeMillis()));
            stringBuilder.append(dateTime).append(" - ").append(methodName).append(" - ").append(stackTrace).append("\n");

            try {
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.append(stringBuilder);
                fileWriter.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}