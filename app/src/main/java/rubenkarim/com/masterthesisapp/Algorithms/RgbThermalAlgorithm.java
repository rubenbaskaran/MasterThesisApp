package rubenkarim.com.masterthesisapp.Algorithms;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import rubenkarim.com.masterthesisapp.Activities.MarkerActivity;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;


// Sample – face-detection – is the simplest implementation of the face detection functionality on Android.
// It supports 2 modes of execution: available by default Java wrapper for the cascade classifier,
// and manually crafted JNI call to a native class which supports tracking.
// Even Java version is able to show close to the real-time performance on a Google Nexus One device.

// Checkout DetectionBasedTracker.java and FdActivity.java

public class RgbThermalAlgorithm extends AbstractAlgorithm {

    private static final String TAG = RgbThermalAlgorithm.class.getSimpleName();
    private Context markerActivityReference;

    public RgbThermalAlgorithm(Context markerActivityReference) {

        this.markerActivityReference = markerActivityReference;
    }


    public void getGradientAndPositions(AlgorithmResult algorithmResult, String thermalImagePath, int deviceScreenWidth, int deviceScreenHeight) {
        Bitmap thermalImageBitmap = ImageProcessing.convertToBitmap(thermalImagePath);
        double thermalImageWidth = thermalImageBitmap.getWidth();
        double thermalImageHeight = thermalImageBitmap.getHeight();
        Bitmap rgbImageBitmap = null;
        double rgbImageWidth = 0;
        double rgbImageHeight = 0;
        ThermalImageFile thermalImageFile = null;
        int defaultVerticalOffset = 25;
        int defaultHorizontalOffset = 10;
        int defaultScreenHeight = 1848;
        int defaultScreenWidth = 1080;

        int scaledVerticalOffset = deviceScreenHeight > defaultScreenHeight ?
                (defaultVerticalOffset * (deviceScreenHeight / defaultScreenHeight))
                : (defaultVerticalOffset / (defaultScreenHeight / deviceScreenHeight));

        int scaledHorizontalOffset = deviceScreenWidth > defaultScreenWidth ?
                (defaultHorizontalOffset * (deviceScreenWidth / defaultScreenWidth))
                : (defaultHorizontalOffset / (defaultScreenWidth / deviceScreenWidth));

        try {
            thermalImageFile = (ThermalImageFile) ImageFactory.createImage(thermalImagePath);
            thermalImageFile.getFusion().setFusionMode(FusionMode.VISUAL_ONLY);
            JavaImageBuffer javaImageBuffer = thermalImageFile.getImage();
            rgbImageBitmap = BitmapAndroid.createBitmap(javaImageBuffer).getBitMap();
            rgbImageWidth = rgbImageBitmap.getWidth();
            rgbImageHeight = rgbImageBitmap.getHeight();
        } catch (IOException e) {
            //FIXME: Handle exception or pass it up
            Logging.error("getGradientAndPositions", e);
        }

        double widthScalingFactor = thermalImageWidth / rgbImageWidth;
        double heightScalingFactor = thermalImageHeight / rgbImageHeight;

        FirebaseVisionImage firebaseVisionImage = ImageProcessing.convertToFirebaseVisionImage(rgbImageBitmap);

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .build();

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        ThermalImageFile finalThermalImageFile = thermalImageFile;


        detector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                // Task completed successfully
                                // [START_EXCLUDE]
                                // [START get_face_info]
                                if (!faces.isEmpty()) {
                                    int[] rightEyeCoordinates = null;
                                    int[] leftEyeCoordinates = null;
                                    int[] noseCoordinates = null;

                                    FirebaseVisionFaceLandmark rightEye = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                    if (rightEye != null) {
                                        rightEyeCoordinates = new int[]{(int) (rightEye.getPosition().getX() * widthScalingFactor - scaledHorizontalOffset), (int) (rightEye.getPosition().getY() * heightScalingFactor - scaledVerticalOffset)};
                                    }
                                    FirebaseVisionFaceLandmark leftEye = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                                    if (leftEye != null) {
                                        leftEyeCoordinates = new int[]{(int) (leftEye.getPosition().getX() * widthScalingFactor + scaledHorizontalOffset), (int) (leftEye.getPosition().getY() * heightScalingFactor - scaledVerticalOffset)};
                                    }
                                    FirebaseVisionFaceLandmark nose = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                    if (nose != null) {
                                        noseCoordinates = new int[]{(int) (nose.getPosition().getX() * widthScalingFactor), (int) (nose.getPosition().getY() * heightScalingFactor - scaledVerticalOffset)};
                                    }

                                    algorithmResult.onResult(RgbThermalAlgorithm.super.calculateGradient(
                                            rightEyeCoordinates[0],
                                            rightEyeCoordinates[1],
                                            leftEyeCoordinates[0],
                                            leftEyeCoordinates[1],
                                            noseCoordinates[0],
                                            noseCoordinates[1],
                                            finalThermalImageFile
                                    ));
                                } else {
                                    algorithmResult.onError("No faces found");
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                algorithmResult.onError("Face detection error");
                            }
                        });
    }

    @Override
    public void getGradientAndPositions(AlgorithmResult algorithmResult) {
        Log.e(TAG, "getGradientAndPositions: You are calling the wrong method");
    }
}
