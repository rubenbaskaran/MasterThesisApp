package rubenkarim.com.masterthesisapp.Utilities;

public class GlobalVariables
{
    private static Algorithms CurrentAlgorithm = null;

    public enum Algorithms
    {
        RgbThermalMapping,
    }

    public static Algorithms getCurrentAlgorithm()
    {
        return CurrentAlgorithm;
    }

    public static void setCurrentAlgorithm(Algorithms currentAlgorithm)
    {
        CurrentAlgorithm = currentAlgorithm;
    }



}
