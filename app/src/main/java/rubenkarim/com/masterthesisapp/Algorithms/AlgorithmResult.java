package rubenkarim.com.masterthesisapp.Algorithms;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

public interface AlgorithmResult {
    void onResult(GradientModel gradientModel);
    void onError(String errorMessage);
}
