package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.nio.ByteBuffer;

public class Cnn extends AbstractAlgorithm {

    private final Interpreter mTflite;
    private ThermalImageFile mthermalImageFile;

    public Cnn(File modelFile, ThermalImageFile mthermalImageFile) {
        mTflite = new Interpreter(modelFile);
        this.mthermalImageFile = mthermalImageFile;
    }

    public Cnn(ByteBuffer modelFile, ThermalImageFile mthermalImageFile) {
        mTflite = new Interpreter(modelFile);
        this.mthermalImageFile = mthermalImageFile;
    }

    @Override
    public GradientModel getGradientAndPositions() {
        //Input
        int[] imgShapeInput = mTflite.getInputTensor(0).shape(); // {1, 240, 320, 1}
        DataType dataTypeInput = mTflite.getInputTensor(0).dataType(); //FLOAT32
        TensorImage inputImageBuffer = new TensorImage(dataTypeInput);
        mthermalImageFile.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        //Bitmap grayBitmap = toGrayscale(mImageBitmap);
        Bitmap thermalImage = getBitmap(mthermalImageFile);
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

        //Get predictions 9109909
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

}
