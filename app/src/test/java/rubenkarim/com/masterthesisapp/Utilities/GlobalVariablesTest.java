package rubenkarim.com.masterthesisapp.Utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalVariablesTest {

    @Test
    void getCurrentAlgorithm() {
        GlobalVariables.setCurrentAlgorithm(GlobalVariables.Algorithms.CNN);
        assert GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.CNN;

        GlobalVariables.setCurrentAlgorithm(GlobalVariables.Algorithms.CNNWithTransferLearning);
        assert GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.CNNWithTransferLearning;

        GlobalVariables.setCurrentAlgorithm(GlobalVariables.Algorithms.MinMaxTemplate);
        assert GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MinMaxTemplate;

        GlobalVariables.setCurrentAlgorithm(GlobalVariables.Algorithms.RgbThermalMapping);
        assert GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.CNN;
    }
}