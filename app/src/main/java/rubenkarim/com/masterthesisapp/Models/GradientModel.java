package rubenkarim.com.masterthesisapp.Models;

import java.io.Serializable;

public class GradientModel implements Serializable {
    public double getGradient() {
        return Gradient;
    }

    public void setGradient(double gradient) {
        Gradient = gradient;
    }

    public int[] getEyePosition() {
        return eyePosition;
    }

    public void setEyePosition(int[] eyePosition) {
        this.eyePosition = eyePosition;
    }

    public int[] getNosePosition() {
        return nosePosition;
    }

    public void setNosePosition(int[] nosePosition) {
        this.nosePosition = nosePosition;
    }

    private double Gradient;
    private int[] eyePosition;
    private int[] nosePosition;

    public GradientModel(double gradient, int[] eyePosition, int[] nosePosition) {
        Gradient = gradient;
        this.eyePosition = eyePosition;
        this.nosePosition = nosePosition;
    }
}