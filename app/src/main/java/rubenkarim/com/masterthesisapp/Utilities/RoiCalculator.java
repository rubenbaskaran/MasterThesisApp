package rubenkarim.com.masterthesisapp.Utilities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Dictionary;
import java.util.List;

import rubenkarim.com.masterthesisapp.R;

public class RoiCalculator {
    public static List<Dictionary> getListOfRoiPixels(ImageView imageView) {
        int width = imageView.getWidth();
        int height = imageView.getHeight();
        int radius = imageView.getWidth() / 2;
        int[] leftUpperCornerLocation = new int[2];
        imageView.getLocationOnScreen(leftUpperCornerLocation);
        int[] center = {leftUpperCornerLocation[0] + radius, leftUpperCornerLocation[1] + radius};
        Bitmap imageViewBitmap = ImageProcessing.loadBitmapFromView((View)imageView);
        int counter = 0;

        for(int x = leftUpperCornerLocation[0]; x <= leftUpperCornerLocation[0] + width; x++){
            for(int y = leftUpperCornerLocation[1]; y <= leftUpperCornerLocation[1] + height; y++){

                // TODO: If pixel is within radius then get pixel value
                Log.e("ROI Pixels", "x: " + x + ", y: " + y);
                //imageViewBitmap.getPixel(x,y);
                //Log.e("Pixel color", Color.red(targetPixel) + "," + Color.green(targetPixel) + "," + Color.blue(targetPixel));
            }
        }

        return null;
    }
}
