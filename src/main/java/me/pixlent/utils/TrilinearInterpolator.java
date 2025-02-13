package me.pixlent.utils;

public class TrilinearInterpolator {
    public double interpolate(double c000, double c001,
                              double c010, double c011,
                              double c100, double c101,
                              double c110, double c111,
                             double x, double y, double z) {
        double c00 = c000 * (1 - x) + c100 * x;
        double c01 = c001 * (1 - x) + c101 * x;
        double c10 = c010 * (1 - x) + c110 * x;
        double c11 = c011 * (1 - x) + c111 * x;

        double c0 = c00 * (1 - y) + c10 * y;
        double c1 = c01 * (1 - y) + c11 * y;

        return c0 * (1 - z) + c1 * z;
    }

    public float interpolate(float c000, float c001,
                             float c010, float c011,
                             float c100, float c101,
                             float c110, float c111,
                             float x, float y, float z) {
        float c00 = c000 * (1 - x) + c100 * x;
        float c01 = c001 * (1 - x) + c101 * x;
        float c10 = c010 * (1 - x) + c110 * x;
        float c11 = c011 * (1 - x) + c111 * x;

        float c0 = c00 * (1 - y) + c10 * y;
        float c1 = c01 * (1 - y) + c11 * y;

        return c0 * (1 - z) + c1 * z;
    }
}
