package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.flir.thermalsdk.image.palettes.Palette;
import com.flir.thermalsdk.image.palettes.PaletteManager;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public class CnnAlgorithmTask extends AbstractAlgorithmTask {

    private final Interpreter mTflite;
    private ThermalImageFile mThermalImage;
    private boolean needsOffset;

    public CnnAlgorithmTask(MappedByteBuffer cnnModel, ThermalImageFile thermalImage, boolean needsOffset) {
        mTflite = new Interpreter((ByteBuffer) cnnModel);
        this.mThermalImage = thermalImage;
        this.needsOffset = needsOffset;
    }

    @Override
    public void getGradientAndPositions(AlgorithmResultListener algorithmResultListener) {
        int[] imgShapeInput = mTflite.getInputTensor(0).shape(); // cnn: {1, width: 240, Height: 320, 3} cnnTransferlearning: {1, 320, 320, 3}
        mThermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);//to get the thermal image only
        mThermalImage.setPalette(PaletteManager.getDefaultPalettes().get(0));
        //Bitmap grayBitmap = toGrayscale(mImageBitmap);
        Bitmap thermalImage = super.getBitmap(mThermalImage);

        //Change Img
        int cnnImgInputSize = 640; //image has not been downsized yet
        BitmapWithBordersInfo bitmapWithBordersInfo = addBlackBorder(thermalImage, cnnImgInputSize);
        TensorImage tensorImage = getTensorImage(imgShapeInput, bitmapWithBordersInfo.getThermalImg());

        //Output
        int[] probabilityShapeOutput = mTflite.getOutputTensor(0).shape();//{1,6}
        DataType probabilityDataTypeOutput = mTflite.getOutputTensor(0).dataType(); //FLOAT32
        TensorBuffer tensorBufferOutput = TensorBuffer.createFixedSize(probabilityShapeOutput, probabilityDataTypeOutput);

        //Get predictions
        mTflite.run(tensorImage.getBuffer(), tensorBufferOutput.getBuffer().rewind());


        float heightProportion = (float) thermalImage.getHeight() / imgShapeInput[2];
        float widthProportion = (float) thermalImage.getWidth() / imgShapeInput[1];

        float[] results = tensorBufferOutput.getFloatArray();
        float[] scaledResults = new float[6];
        for (int i = 0; i < scaledResults.length; i++) {
            if (i % 2 == 0) {
                scaledResults[i] = results[i] * widthProportion;
            } else {
                scaledResults[i] = results[i] * heightProportion;
                if(needsOffset){
                    scaledResults[i] -=80.0f;
                }
            }
        }
        mThermalImage.setPalette(PaletteManager.getDefaultPalettes().get(12));
        algorithmResultListener.onResult(super.calculateGradient(scaledResults, mThermalImage));
    }

    private TensorImage getTensorImage(int[] imgShapeInput, Bitmap thermalImage) {
        DataType dataTypeInput = mTflite.getInputTensor(0).dataType(); //FLOAT32
        TensorImage inputImageBuffer = new TensorImage(dataTypeInput);
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imgShapeInput[2], imgShapeInput[1], ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0, 255)).build();
        inputImageBuffer.load(thermalImage);
        return imageProcessor.process(inputImageBuffer);
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
}
