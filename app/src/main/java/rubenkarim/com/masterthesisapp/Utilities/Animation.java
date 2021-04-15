package rubenkarim.com.masterthesisapp.Utilities;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class Animation {
    public static void showLoadingAnimation(ProgressBar progressBar_loadingAnimation, ImageView faceTemplate) {
        if (progressBar_loadingAnimation != null && progressBar_loadingAnimation.getVisibility() == View.INVISIBLE) {
            progressBar_loadingAnimation.setVisibility(View.VISIBLE);
        }

        if (faceTemplate != null) {
            if (faceTemplate.getVisibility() == View.VISIBLE) {
                faceTemplate.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void hideLoadingAnimation(ProgressBar progressBar_loadingAnimation, ImageView faceTemplate) {
        if (progressBar_loadingAnimation != null && progressBar_loadingAnimation.getVisibility() == View.VISIBLE) {
            progressBar_loadingAnimation.setVisibility(View.INVISIBLE);
        }

        if (faceTemplate != null) {
            if (faceTemplate.getVisibility() == View.INVISIBLE) {
                faceTemplate.setVisibility(View.VISIBLE);
            }
        }
    }
}
