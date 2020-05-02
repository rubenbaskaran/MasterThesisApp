package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;

import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;

import androidx.annotation.NonNull;


// Sample – face-detection – is the simplest implementation of the face detection functionality on Android.
// It supports 2 modes of execution: available by default Java wrapper for the cascade classifier,
// and manually crafted JNI call to a native class which supports tracking.
// Even Java version is able to show close to the real-time performance on a Google Nexus One device.

// Checkout DetectionBasedTracker.java and FdActivity.java

public class RgbThermalAlgorithmTask extends AbstractAlgorithmTask {

    private ThermalImageFile mThermalImageFile;
    private int deviceScreenWidth;
    private int deviceScreenHeight;


    public RgbThermalAlgorithmTask(ThermalImageFile thermalImage, int deviceScreenWidth, int deviceScreenHeight) {
        mThermalImageFile = thermalImage;
        this.deviceScreenWidth = deviceScreenWidth;
        this.deviceScreenHeight = deviceScreenHeight;
    }

    @Override
    public void getGradientAndPositions(AlgorithmResultListener algorithmResultListener) {
        mThermalImageFile.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        Bitmap thermalImageBitmap = super.getBitmap(mThermalImageFile);
        double thermalImageWidth = thermalImageBitmap.getWidth();
        double thermalImageHeight = thermalImageBitmap.getHeight();
        Bitmap rgbImageBitmap = null;
        double rgbImageWidth = 0;
        double rgbImageHeight = 0;
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

        ThermalImageFile thermalImageFile = mThermalImageFile;
        thermalImageFile.getFusion().setFusionMode(FusionMode.VISUAL_ONLY);
        rgbImageBitmap = super.getBitmap(thermalImageFile);
        rgbImageWidth = rgbImageBitmap.getWidth();
        rgbImageHeight = rgbImageBitmap.getHeight();

        double widthScalingFactor = thermalImageWidth / rgbImageWidth;
        double heightScalingFactor = thermalImageHeight / rgbImageHeight;

        FirebaseVisionImage firebaseVisionImage = convertToFirebaseVisionImage(rgbImageBitmap);

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
                                        rightEyeCoordinates = new int[]{(int)Math.round((rightEye.getPosition().getX() * widthScalingFactor - scaledHorizontalOffset)), (int)Math.round((rightEye.getPosition().getY() * heightScalingFactor - scaledVerticalOffset))};
                                    }
                                    FirebaseVisionFaceLandmark leftEye = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                                    if (leftEye != null) {
                                        leftEyeCoordinates = new int[]{(int)Math.round((leftEye.getPosition().getX() * widthScalingFactor + scaledHorizontalOffset)), (int)Math.round((leftEye.getPosition().getY() * heightScalingFactor - scaledVerticalOffset))};
                                    }
                                    FirebaseVisionFaceLandmark nose = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                    if (nose != null) {
                                        noseCoordinates = new int[]{(int)Math.round((nose.getPosition().getX() * widthScalingFactor)), (int)Math.round((nose.getPosition().getY() * heightScalingFactor - scaledVerticalOffset))};
                                    }

                                    algorithmResultListener.onResult(RgbThermalAlgorithmTask.super.calculateGradient(
                                            rightEyeCoordinates[0],
                                            rightEyeCoordinates[1],
                                            leftEyeCoordinates[0],
                                            leftEyeCoordinates[1],
                                            noseCoordinates[0],
                                            noseCoordinates[1],
                                            finalThermalImageFile
                                    ));
                                } else {
                                    algorithmResultListener.onError("No faces found");
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                algorithmResultListener.onError("Face detection error");
                            }
                        });
    }

    private FirebaseVisionImage convertToFirebaseVisionImage(Bitmap bitmap) {
        return FirebaseVisionImage.fromBitmap(bitmap);
    }
}
