package rubenkarim.com.masterthesisapp.Algorithms;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

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
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;

// Sample – face-detection – is the simplest implementation of the face detection functionality on Android.
// It supports 2 modes of execution: available by default Java wrapper for the cascade classifier,
// and manually crafted JNI call to a native class which supports tracking.
// Even Java version is able to show close to the real-time performance on a Google Nexus One device.

// Checkout DetectionBasedTracker.java and FdActivity.java

public class RgbThermalAlgorithm extends AbstractAlgorithm {

    private Context markerActivityReference;

    public RgbThermalAlgorithm(Context markerActivityReference) {

        this.markerActivityReference = markerActivityReference;
    }

    @Override
    public GradientModel getGradientAndPositions() {
        return null;
    }

    public void getGradientAndPositionsAsync(String thermalImagePath) {
        GradientModel gradientAndPositions = new GradientModel(100, null, null);
        Bitmap thermalImageBitmap = ImageProcessing.convertToBitmap(thermalImagePath);
        double thermalImageWidth = thermalImageBitmap.getWidth();
        double thermalImageHeight = thermalImageBitmap.getHeight();
        Bitmap rgbImageBitmap = null;
        double rgbImageWidth = 0;
        double rgbImageHeight = 0;
        int verticalOffset = 15;
        int horizontalOffset = 10;

        try {
            ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(thermalImagePath);
            thermalImageFile.getFusion().setFusionMode(FusionMode.VISUAL_ONLY);
            JavaImageBuffer javaImageBuffer = thermalImageFile.getImage();
            rgbImageBitmap = BitmapAndroid.createBitmap(javaImageBuffer).getBitMap();
            rgbImageWidth = rgbImageBitmap.getWidth();
            rgbImageHeight = rgbImageBitmap.getHeight();
        }
        catch (IOException e) {
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

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(firebaseVisionImage)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        // [START_EXCLUDE]
                                        // [START get_face_info]
                                        if (!faces.isEmpty()) {
                                            Rect bounds = faces.get(0).getBoundingBox();
                                            float rotY = faces.get(0).getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = faces.get(0).getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                            FirebaseVisionFaceLandmark leftEye = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                                            if (leftEye != null) {
                                                gradientAndPositions.setEyePosition(new int[]{(int) (leftEye.getPosition().getX() * widthScalingFactor + horizontalOffset), (int) (leftEye.getPosition().getY() * heightScalingFactor + verticalOffset)});
                                                // TODO: Get eye temperature
                                            }
                                            FirebaseVisionFaceLandmark nose = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                            if (nose != null) {
                                                gradientAndPositions.setNosePosition(new int[]{(int) (nose.getPosition().getX() * widthScalingFactor), (int) (nose.getPosition().getY() * heightScalingFactor + verticalOffset)});
                                                // TODO: Get nose temperature
                                            }

                                            // TODO: eye_temp-nose_temp = gradient
                                            ((MarkerActivity) markerActivityReference).setPicture(gradientAndPositions);
                                        }
                                        else {
                                            Snackbar.make(((Activity) markerActivityReference).findViewById(R.id.linearLayout_MarkerActivity), "No faces found", Snackbar.LENGTH_SHORT).show();
                                            ((MarkerActivity) markerActivityReference).setPicture(null);
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(((Activity) markerActivityReference).findViewById(R.id.linearLayout_MarkerActivity), "Face detection error", Snackbar.LENGTH_SHORT).show();
                                        ((MarkerActivity) markerActivityReference).setPicture(null);
                                    }
                                });
    }
}
