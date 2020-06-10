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

public class ThermalImgModel implements IThermalImage {

    private ThermalImage flirThermalImage;


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

    private void setFlirThermalImage(ThermalImage thermalImage){
        this.flirThermalImage = thermalImage;
        flirThermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        flirThermalImage.setPalette(PaletteManager.getDefaultPalettes().get(Palette.WHITEHOT.index)); //default
    }

    public ThermalImgModel(ThermalImageFile thermalImageFile) {
        setFlirThermalImage(thermalImageFile);
    }

    public ThermalImgModel(ThermalImage thermalImage) {
        setFlirThermalImage(thermalImage);
    }

    public Bitmap getThermalImage() {
        JavaImageBuffer javaImageBuffer = flirThermalImage.getImage();
        return BitmapAndroid.createBitmap(javaImageBuffer).getBitMap();
    }

    public void save(String path) throws IOException {
        flirThermalImage.saveAs(path);
    }

    public Bitmap getThermalImgWithPalette(Palette palette) {
        flirThermalImage.setPalette(PaletteManager.getDefaultPalettes().get(palette.index));
        flirThermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        Bitmap paletteThermalImage = getBitmap(flirThermalImage);
        flirThermalImage.setPalette(PaletteManager.getDefaultPalettes().get(Palette.WHITEHOT.index)); //default
        return paletteThermalImage;
    }

    public double getTemperatureAtPoint(int x, int y) {
        flirThermalImage.setTemperatureUnit(TemperatureUnit.CELSIUS);
        return flirThermalImage.getValueAt(new Point(x, y));
    }

    private Bitmap getBitmap(ThermalImage thermalImage) {
        JavaImageBuffer javaBuffer = thermalImage.getImage();
        return BitmapAndroid.createBitmap(javaBuffer).getBitMap();
    }

    public Bitmap getVisualImage() {
        flirThermalImage.getFusion().setFusionMode(FusionMode.VISUAL_ONLY);
        Bitmap visualImage = getBitmap(flirThermalImage);
        flirThermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        return visualImage;
    }

    public int getThermalImgWidth(){
        return flirThermalImage.getWidth();
    }

    public int getThermalImgHeight(){
        return flirThermalImage.getHeight();
    }

}

