package rubenkarim.com.masterthesisapp.Models;

public class RoiModel {
    private int[] upperLeftCornerLocation;
    private int height;
    private int width;

    public RoiModel(int[] upperLeftCornerLocation, int height, int width) {
        this.upperLeftCornerLocation = upperLeftCornerLocation;
        this.height = height;
        this.width = width;
    }

    public int[] getUpperLeftCornerLocation() {
        return upperLeftCornerLocation;
    }

    public void setUpperLeftCornerLocation(int[] upperLeftCornerLocation) {
        this.upperLeftCornerLocation = upperLeftCornerLocation;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
