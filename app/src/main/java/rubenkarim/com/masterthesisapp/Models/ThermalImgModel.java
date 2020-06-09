package rubenkarim.com.masterthesisapp.Models;

import android.graphics.Bitmap;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.Point;
import com.flir.thermalsdk.image.TemperatureUnit;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.flir.thermalsdk.image.palettes.PaletteManager;

import java.io.IOException;

import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.IThermalImage;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;

public class ThermalImgModel implements IThermalImage {

    private ThermalImage thermalImage;


    public enum Palette {
        IRON(0),
        ARCTIC(1),
        BLACKHOT(2),
        BW(3),
        COLDEST(4),
        COLORWHEEL_REDHOT(5),
        COLORWHEEL6(6),
        COLORWHEEL12(7),
        DOUBLERAINBOW2(8),
        LAVA(9),
        RAINBOW(10),
        RAINHC(11),
        WHITEHOT(12),
        HOTTEST(13);

        private final int index;

        Palette(int index) {
            this.index = index;
        }

    }

    public ThermalImgModel(ThermalImageFile thermalImageFile) {
        this.thermalImage = (ThermalImage) thermalImageFile;
    }

    public ThermalImgModel(ThermalImage thermalImage) {
        this.thermalImage = thermalImage;
    }

    public Bitmap getThermalImage() {
        JavaImageBuffer javaImageBuffer = thermalImage.getImage();
        thermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        return BitmapAndroid.createBitmap(javaImageBuffer).getBitMap();
    }

    public void save(String path) throws IOException {
        thermalImage.saveAs(path);
    }

    public Bitmap getThermalImgWithPalette(Palette palette) {
        thermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        thermalImage.setPalette(PaletteManager.getDefaultPalettes().get(palette.index));
        Bitmap paletteThermalImage = getBitmap(thermalImage);
        thermalImage.setPalette(PaletteManager.getDefaultPalettes().get(Palette.IRON.index)); //default
        return paletteThermalImage;
    }

    public double getTemperatureAtPoint(int x, int y) {
        thermalImage.setTemperatureUnit(TemperatureUnit.CELSIUS);
        return thermalImage.getValueAt(new Point(x, y));
    }

    private Bitmap getBitmap(ThermalImage thermalImage) {
        JavaImageBuffer javaBuffer = thermalImage.getImage();
        return BitmapAndroid.createBitmap(javaBuffer).getBitMap();
    }

    public Bitmap getVisualImage() {
        thermalImage.getFusion().setFusionMode(FusionMode.VISUAL_ONLY);
        return getBitmap(thermalImage);
    }

    public int getThermalImgWidth(){
        return thermalImage.getWidth();
    }

    public int getThermalImgHeight(){
        return thermalImage.getHeight();
    }

}

