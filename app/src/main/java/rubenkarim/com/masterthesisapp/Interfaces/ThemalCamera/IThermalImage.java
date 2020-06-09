package rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera;

import android.graphics.Bitmap;

import java.io.IOException;

import rubenkarim.com.masterthesisapp.Models.ThermalImgModel;

public interface IThermalImage {

    Bitmap getThermalImage();
    void save(String mThermalImagePath) throws IOException;
    Bitmap getThermalImgWithPalette(ThermalImgModel.Palette palette);
    double getTempertureAtPoint(int x, int y);

}
