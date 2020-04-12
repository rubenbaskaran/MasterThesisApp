package rubenkarim.com.masterthesisapp.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class databaseDataExporter {
    private void SaveFileOnPhone() {
        String filepath = "masterthesisapp" + File.separator + "DateTimeNow" + ".csv";
        File file = new File(filepath);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("test");

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append(stringBuilder);
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
