package rubenkarim.com.masterthesisapp.Algorithms;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Bitmap;

import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.ThermalValue;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.nio.ByteBuffer;

public class Cnn extends AbstractAlgorithm {

    private final Interpreter mTflite;
    private Bitmap mImageBitmap;
    private ThermalImageFile thermalImageFile;

    public Cnn(File modelFile, Bitmap imagefile, ThermalImageFile thermalImageFile){
        mTflite = new Interpreter(modelFile);
        this.mImageBitmap = imagefile;
        this.thermalImageFile = thermalImageFile;
    }
    public Cnn(ByteBuffer modelFile, Bitmap imageFile, ThermalImageFile thermalImageFile){
        mTflite = new Interpreter(modelFile);
        this.mImageBitmap = imageFile;
        this.thermalImageFile = thermalImageFile;
    }

    @Override
    public GradientModel getGradientAndPositions() {
        //TODO: investigate the output of the model

        //Input
        int[] imgShapeInput = mTflite.getInputTensor(0).shape(); // {1, 240, 320, 1}
        DataType dataTypeInput = mTflite.getInputTensor(0).dataType(); //FLOAT32
        TensorImage  inputImageBuffer = new TensorImage(dataTypeInput);
        Bitmap grayBitmap = toGrayscale(mImageBitmap);
        inputImageBuffer.load(grayBitmap);
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imgShapeInput[1], imgShapeInput[2], ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new NormalizeOp(0,255)).build();
        TensorImage tensorImage = imageProcessor.process(inputImageBuffer);

        //Output
        int[] probabilityShapeOutput = mTflite.getOutputTensor(0).shape();//{1,6}
        DataType probabilityDataTypeOutput = mTflite.getOutputTensor(0).dataType(); //FLOAT32
        TensorBuffer tensorBufferOutput = TensorBuffer.createFixedSize(probabilityShapeOutput, probabilityDataTypeOutput);

        //Get predictions 9109909
        mTflite.run(tensorImage.getBuffer(), tensorBufferOutput.getBuffer().rewind());

        float[] results = tensorBufferOutput.getFloatArray();

        return super.calculateGradient(results, thermalImageFile);
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
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
