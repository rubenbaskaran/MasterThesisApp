package rubenkarim.com.masterthesisapp.Utilities;

import android.widget.ImageView;

import java.io.Serializable;

public class MinMaxDTO implements Serializable {
    //region Getters and setters
    public int[] getLeftEyeLocation() {
        return leftEyeLocation;
    }

    public void setLeftEyeLocation(int[] leftEyeLocation) {
        this.leftEyeLocation = leftEyeLocation;
    }

    public int[] getRightEyeLocation() {
        return rightEyeLocation;
    }

    public void setRightEyeLocation(int[] rightEyeLocation) {
        this.rightEyeLocation = rightEyeLocation;
    }

    public int[] getNoseLocation() {
        return noseLocation;
    }

    public void setNoseLocation(int[] noseLocation) {
        this.noseLocation = noseLocation;
    }

    public int[] getCameraPreviewContainerLocation() {
        return cameraPreviewContainerLocation;
    }

    public void setCameraPreviewContainerLocation(int[] cameraPreviewContainerLocation) {
        this.cameraPreviewContainerLocation = cameraPreviewContainerLocation;
    }

    public int getLeftEyeWidth() {
        return leftEyeWidth;
    }

    public void setLeftEyeWidth(int leftEyeWidth) {
        this.leftEyeWidth = leftEyeWidth;
    }

    public int getLeftEyeHeight() {
        return leftEyeHeight;
    }

    public void setLeftEyeHeight(int leftEyeHeight) {
        this.leftEyeHeight = leftEyeHeight;
    }

    public int getRightEyeWidth() {
        return rightEyeWidth;
    }

    public void setRightEyeWidth(int rightEyeWidth) {
        this.rightEyeWidth = rightEyeWidth;
    }

    public int getRightEyeHeight() {
        return rightEyeHeight;
    }

    public void setRightEyeHeight(int rightEyeHeight) {
        this.rightEyeHeight = rightEyeHeight;
    }

    public int getNoseWidth() {
        return noseWidth;
    }

    public void setNoseWidth(int noseWidth) {
        this.noseWidth = noseWidth;
    }

    public int getNoseHeight() {
        return noseHeight;
    }

    public void setNoseHeight(int noseHeight) {
        this.noseHeight = noseHeight;
    }

    public int getCameraPreviewContainerWidth() {
        return cameraPreviewContainerWidth;
    }

    public void setCameraPreviewContainerWidth(int cameraPreviewContainerWidth) {
        this.cameraPreviewContainerWidth = cameraPreviewContainerWidth;
    }

    public int getCameraPreviewContainerHeight() {
        return cameraPreviewContainerHeight;
    }

    public void setCameraPreviewContainerHeight(int cameraPreviewContainerHeight) {
        this.cameraPreviewContainerHeight = cameraPreviewContainerHeight;
    }
    //endregion

    //region Properties
    private int[] leftEyeLocation = new int[2];
    private int[] rightEyeLocation = new int[2];
    private int[] noseLocation = new int[2];
    private int[] cameraPreviewContainerLocation = new int[2];
    private int leftEyeWidth;
    private int leftEyeHeight;
    private int rightEyeWidth;
    private int rightEyeHeight;
    private int noseWidth;
    private int noseHeight;
    private int cameraPreviewContainerWidth;
    private int cameraPreviewContainerHeight;
    //endregion

    public MinMaxDTO(ImageView leftEye, ImageView rightEye, ImageView nose, ImageView cameraPreviewContainer) {
        leftEye.getLocationOnScreen(leftEyeLocation);
        rightEye.getLocationOnScreen(rightEyeLocation);
        nose.getLocationOnScreen(noseLocation);
        cameraPreviewContainer.getLocationOnScreen(cameraPreviewContainerLocation);

        leftEyeWidth = leftEye.getWidth();
        rightEyeWidth = rightEye.getWidth();
        noseWidth = nose.getWidth();
        cameraPreviewContainerWidth = cameraPreviewContainer.getWidth();

        leftEyeHeight = leftEye.getHeight();
        rightEyeHeight = rightEye.getHeight();
        noseHeight = nose.getHeight();
        cameraPreviewContainerHeight = cameraPreviewContainer.getHeight();
    }
}
