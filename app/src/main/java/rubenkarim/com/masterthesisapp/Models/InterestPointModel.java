package rubenkarim.com.masterthesisapp.Models;

public class InterestPointModel {
    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }

    public int[] getPosition() {
        return Position;
    }

    public void setPosition(int[] position) {
        Position = position;
    }

    private double Value;
    private int[] Position;

    public InterestPointModel(double value, int[] position) {
        Value = value;
        Position = position;
    }
}
