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

    public double getEyeTemperature() {
        return eyeTemperature;
    }

    public void setEyeTemperature(double eyeTemperature) {
        this.eyeTemperature = eyeTemperature;
    }

    public double getNoseTemperature() {
        return noseTemperature;
    }

    public void setNoseTemperature(double noseTemperature) {
        this.noseTemperature = noseTemperature;
    }

    public boolean isMarkersAdjusted() {
        return markersAdjusted;
    }

    public void setMarkersAdjusted(boolean markersAdjusted) {
        this.markersAdjusted = markersAdjusted;
    }

    private double Gradient;
    private int[] eyePosition;
    private int[] nosePosition;
    private double eyeTemperature;
    private double noseTemperature;
    private boolean markersAdjusted = false;

    public GradientModel(double gradient, int[] eyePosition, int[] nosePosition) {
        Gradient = gradient;
        this.eyePosition = eyePosition;
        this.nosePosition = nosePosition;
    }
}