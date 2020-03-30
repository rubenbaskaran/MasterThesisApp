package rubenkarim.com.masterthesisapp.Algorithms;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;

import rubenkarim.com.masterthesisapp.Activities.MarkerActivity;
import rubenkarim.com.masterthesisapp.Models.GradientModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Cnn extends AbstractAlgorithm {

    private final Interpreter mTflite;
    private ThermalImageFile mthermalImageFile;
    private boolean imgShouldBeRect;


    public Cnn(MarkerActivity markerActivity, String cnnModelFilePath, ThermalImageFile mThermalImage, boolean imgShouldBeRect) throws IOException {
        this.imgShouldBeRect = imgShouldBeRect;
        mTflite = new Interpreter((ByteBuffer) loadModelFile(markerActivity, cnnModelFilePath));
        this.mthermalImageFile = mThermalImage;
    }

    @Override
    public GradientModel getGradientAndPositions() {
        //Input
        int[] imgShapeInput = mTflite.getInputTensor(0).shape(); // {1, 240, 320, 1}
        DataType dataTypeInput = mTflite.getInputTensor(0).dataType(); //FLOAT32
        TensorImage inputImageBuffer = new TensorImage(dataTypeInput);
        mthermalImageFile.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);//to get the thermal image only
        //Bitmap grayBitmap = toGrayscale(mImageBitmap);
        Bitmap thermalImage = getBitmap(mthermalImageFile);

        //somePretrained networks only inputs rects.
        if(imgShouldBeRect){
            addBlackBorder(thermalImage, 320);
        }

        inputImageBuffer.load(thermalImage);
        float heightProportion = (float) thermalImage.getHeight() / imgShapeInput[1];
        float widthProportion = (float) thermalImage.getWidth() / imgShapeInput[2];
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imgShapeInput[1], imgShapeInput[2], ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new NormalizeOp(0, 255)).build();
        TensorImage tensorImage = imageProcessor.process(inputImageBuffer);

        //Output
        int[] probabilityShapeOutput = mTflite.getOutputTensor(0).shape();//{1,6}
        DataType probabilityDataTypeOutput = mTflite.getOutputTensor(0).dataType(); //FLOAT32
        TensorBuffer tensorBufferOutput = TensorBuffer.createFixedSize(probabilityShapeOutput, probabilityDataTypeOutput);

        //Get predictions
        mTflite.run(tensorImage.getBuffer(), tensorBufferOutput.getBuffer().rewind());

        float[] scaledResults = tensorBufferOutput.getFloatArray();
        float[] results = new float[6];
        for (int i = 0; i < results.length; i++) {
            if (i % 2 == 0) {
                results[i] = scaledResults[i] * widthProportion;
            } else {
                results[i] = scaledResults[i] * heightProportion;
            }
        }
        return super.calculateGradient(results, mthermalImageFile);
    }

    private Bitmap getBitmap(ThermalImageFile thermalImageFile) {
        JavaImageBuffer javaBuffer = thermalImageFile.getImage();
        return BitmapAndroid.createBitmap(javaBuffer).getBitMap();
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private Bitmap addBlackBorder(Bitmap bmp, int minImgSize) {
        Bitmap bmpWithBorder;
        int bmpAddWidth = minImgSize - bmp.getWidth();
        int bmpAddHeight = minImgSize - bmp.getHeight();
        int newImgWidth;
        int newImgHeight;

        newImgHeight = bmpAddWidth <= 0 ? bmp.getWidth() : minImgSize;

        if (bmpAddWidth <= 0){
            newImgWidth = bmp.getWidth();
        } else {
            newImgWidth = minImgSize;
        }
        if (bmpAddHeight <= 0){
            newImgHeight = bmp.getHeight();
        } else {
            newImgHeight = minImgSize;
        }

        bmpWithBorder = Bitmap.createBitmap(newImgWidth, newImgHeight, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.BLACK);
        float imfHeightOffset = bmpAddHeight >= 0? (float) (bmpAddHeight / 2.0) : 0;
        float imfWidthOffset = bmpAddWidth >= 0? (float) (bmpAddWidth / 2.0) : 0;
        canvas.drawBitmap(bmp, imfWidthOffset, imfHeightOffset, null);
        return bmpWithBorder;
    }

}
