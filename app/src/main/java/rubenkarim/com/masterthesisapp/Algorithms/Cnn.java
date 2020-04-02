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

    private class BitmapWithBordersInfo {
        private Bitmap thermalImg;
        private float offsetX;
        private float offsetY;

        public BitmapWithBordersInfo(Bitmap thermalImg, float offsetX, float offsetY) {
            this.thermalImg = thermalImg;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public Bitmap getThermalImg() {
            return thermalImg;
        }

        public float getOffsetWidth() {
            return offsetX;
        }

        public float getOffsetHeight() {
            return offsetY;
        }
    }

    @Override
    public GradientModel getGradientAndPositions() {
        //Input
        int[] imgShapeInput = mTflite.getInputTensor(0).shape(); // cnn: {1, width: 240, Height: 320, 1} cnnTransferlearning: {1, 320, 320, 1}
        mthermalImageFile.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);//to get the thermal image only
        //Bitmap grayBitmap = toGrayscale(mImageBitmap);
        Bitmap thermalImage = getBitmap(mthermalImageFile);

        float heightProportion = (float) thermalImage.getHeight() / imgShapeInput[1];
        float widthProportion = (float) thermalImage.getWidth() / imgShapeInput[2];

        BitmapWithBordersInfo bitmapWithBordersInfo = null;
        TensorImage tensorImage;
        int cnnImgInputSize = 640;
        if(imgShouldBeRect){ //somePretrained networks only inputs rects.

            bitmapWithBordersInfo = addBlackBorder(thermalImage, cnnImgInputSize);
            tensorImage = getTensorImage(imgShapeInput, bitmapWithBordersInfo.getThermalImg());

        } else {
            tensorImage = getTensorImage(imgShapeInput, thermalImage);
        }

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

        if(imgShouldBeRect){
            for (int i = 0; i < results.length; i++) {
                if (i % 2 == 0) {
                    results[i] = results[i]; //- bitmapWithBordersInfo.getOffsetWidth();
                } else {
                    results[i] = results[i] +80;
                }
            }
        }

        return super.calculateGradient(results, mthermalImageFile);
    }

    private TensorImage getTensorImage(int[] imgShapeInput, Bitmap thermalImage) {
        DataType dataTypeInput = mTflite.getInputTensor(0).dataType(); //FLOAT32
        TensorImage inputImageBuffer = new TensorImage(dataTypeInput);
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imgShapeInput[1], imgShapeInput[2], ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0, 255)).build();
        inputImageBuffer.load(thermalImage);
        return imageProcessor.process(inputImageBuffer);
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

    private BitmapWithBordersInfo addBlackBorder(Bitmap bmp, int minImgSize) {
        Bitmap bmpWithBorder;
        int bmpAddWidth = minImgSize - bmp.getWidth();
        int bmpAddHeight = minImgSize - bmp.getHeight();

        int newImgWidth = bmpAddWidth <= 0 ? bmp.getWidth() : minImgSize;
        int newImgHeight = bmpAddHeight <= 0 ? bmp.getHeight(): minImgSize;

        bmpWithBorder = Bitmap.createBitmap(newImgWidth, newImgHeight, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.BLACK);
        float imgHeightOffset = bmpAddHeight >= 0? (float) (bmpAddHeight / 2.0) : 0;
        float imgWidthOffset = bmpAddWidth >= 0? (float) (bmpAddWidth / 2.0) : 0;
        canvas.drawBitmap(bmp, imgWidthOffset, imgHeightOffset, null);
        return new BitmapWithBordersInfo(bmpWithBorder, imgWidthOffset, imgHeightOffset);
    }

}
