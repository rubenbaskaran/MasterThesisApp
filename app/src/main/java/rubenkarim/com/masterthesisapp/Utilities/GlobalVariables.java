package rubenkarim.com.masterthesisapp.Utilities;

public class GlobalVariables
{
    public enum Algorithms
    {
        CNN,
        CNNWithTransferLearning,
        RgbThermalMapping,
        MaxMinTemplate
    }

    public static Algorithms getCurrentAlgorithm()
    {
        return CurrentAlgorithm;
    }

    public static void setCurrentAlgorithm(Algorithms currentAlgorithm)
    {
        CurrentAlgorithm = currentAlgorithm;
    }

    private static Algorithms CurrentAlgorithm;

}
