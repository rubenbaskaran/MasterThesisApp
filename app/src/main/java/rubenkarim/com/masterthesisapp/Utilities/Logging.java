package rubenkarim.com.masterthesisapp.Utilities;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Logging {
    public static void error(String methodName, Exception exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString();
        Log.e("Error - " + methodName, stackTrace);
        writeToLog(methodName, stackTrace);
    }
    public static void info(String tag, String message) {
        Log.i(tag, message);
        writeToLog(tag, message);
    }

    private static void writeToLog(String methodName, String stackTrace) {
        File logDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/Masterthesislogs/");
        boolean isDirectoryCreated = logDirectory.exists() || logDirectory.mkdirs();

        if (isDirectoryCreated) {
            String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/Masterthesislogs/log.txt";
            File file = new File(filepath);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(methodName).append(" - ").append(stackTrace).append("\n");

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