import java.io.*;

public class Heatmap {

    public final static int HEATMAP_ROW = 60;

    public final static int HEATMAP_COL = 181;

    private double heatmap[][];

    public Heatmap() {
        heatmap = new double[HEATMAP_ROW][HEATMAP_COL];
    }

    public double[][] getHeatmap() {
        return heatmap;
    }
}
