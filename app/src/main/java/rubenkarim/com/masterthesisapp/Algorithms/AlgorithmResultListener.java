package rubenkarim.com.masterthesisapp.Algorithms;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

public interface AlgorithmResultListener {
    void onResult(GradientModel gradientModel);
    void onError(String errorMessage);
}
