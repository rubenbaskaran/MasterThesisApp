package rubenkarim.com.masterthesisapp.Models;

import java.io.Serializable;

public class GradientModel implements Serializable {
    public double getGradient() {
        return gradient;
    }

    public void setGradient(double gradient) {
        this.gradient = Math.abs(gradient);
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

    public boolean isEyeMarkerAdjusted() {
        return eyeMarkerAdjusted;
    }

    public void setEyeMarkerAdjusted(boolean eyeMarkerAdjusted) {
        this.eyeMarkerAdjusted = eyeMarkerAdjusted;
    }

    public boolean isNoseMarkerAdjusted() {
        return noseMarkerAdjusted;
    public boolean isMarkersAdjusted() {
        return isMarkersAdjusted;
    }

    public void setNoseMarkerAdjusted(boolean noseMarkerAdjusted) {
        this.noseMarkerAdjusted = noseMarkerAdjusted;
    }

    private double Gradient;
    private int[] eyePosition;
    private int[] nosePosition;
    private double eyeTemperature;
    private double noseTemperature;
    private boolean eyeMarkerAdjusted = false;
    private boolean noseMarkerAdjusted = false;

    public GradientModel(double gradient, int[] eyePosition, int[] nosePosition, double eyeTemperature, double noseTemperature) {
        Gradient = gradient;
        this.eyePosition = eyePosition;
        this.nosePosition = nosePosition;
        this.eyeTemperature = eyeTemperature;
        this.noseTemperature = noseTemperature;
    }
}