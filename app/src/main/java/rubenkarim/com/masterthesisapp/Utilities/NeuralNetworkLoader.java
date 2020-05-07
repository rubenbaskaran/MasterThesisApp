package rubenkarim.com.masterthesisapp.Utilities;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class NeuralNetworkLoader {

    public static MappedByteBuffer loadCnnTransferLearning(Context context) throws IOException {
        String cnnTransferLearningModelFile = "RGB_InceptionV3_Huber_loss.tflite";
        return loadNNModelFile(context, cnnTransferLearningModelFile);
    }

    public static MappedByteBuffer loadCnn(Context context) throws IOException {
        String cnnModelFile = "SmallEncoderDecoder.tflite";
        return loadNNModelFile(context, cnnModelFile);
    }

    public static MappedByteBuffer loadNNModelFile(Context context, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
