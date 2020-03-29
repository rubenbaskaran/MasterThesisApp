package rubenkarim.com.masterthesisapp.Utilities;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class Animation {
    public static void showLoadingAnimation(ProgressBar progressBar_loadingAnimation, ImageView faceTemplate, RelativeLayout eyeNoseTemplate) {
        if (progressBar_loadingAnimation.getVisibility() == View.INVISIBLE) {
            progressBar_loadingAnimation.setVisibility(View.VISIBLE);
        }

        if (faceTemplate != null && GlobalVariables.getCurrentAlgorithm() != GlobalVariables.Algorithms.MaxMinTemplate) {
            if (faceTemplate.getVisibility() == View.VISIBLE) {
                faceTemplate.setVisibility(View.INVISIBLE);
            }
        }

        if (eyeNoseTemplate != null && GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MaxMinTemplate) {
            if (eyeNoseTemplate.getVisibility() == View.VISIBLE) {
                eyeNoseTemplate.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void hideLoadingAnimation(ProgressBar progressBar_loadingAnimation, ImageView faceTemplate, RelativeLayout eyeNoseTemplate) {
        if (progressBar_loadingAnimation.getVisibility() == View.VISIBLE) {
            progressBar_loadingAnimation.setVisibility(View.INVISIBLE);
        }

        if (faceTemplate != null && GlobalVariables.getCurrentAlgorithm() != GlobalVariables.Algorithms.MaxMinTemplate) {
            if (faceTemplate.getVisibility() == View.INVISIBLE) {
                faceTemplate.setVisibility(View.VISIBLE);
            }
        }

        if (eyeNoseTemplate != null && GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MaxMinTemplate) {
            if (eyeNoseTemplate.getVisibility() == View.INVISIBLE) {
                eyeNoseTemplate.setVisibility(View.VISIBLE);
            }
        }
    }
}
