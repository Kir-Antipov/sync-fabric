package me.kirantipov.mods.sync.util.math;

public class QuarticFunction extends Function {
    private final double a;
    private final double b;
    private final double c;
    private final double d;
    private final double e;

    private final double d0;
    private final double d1;
    private final double d2;

    public QuarticFunction(double a, double b, double c, double d, double e) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;

        double p0 = 2 * cube(c) - 9 * b * c * d + 27 * a * d * d + 27 * b * b * e - 72 * a * c * e;
        double p1 = c * c - 3 * b * d + 12 * a * e;
        double p2 = p0 + Math.sqrt(-4 * cube(p1) + p0 * p0);
        double p3 = p1 / (3 * a * Math.pow(p2 / 2, 1 / 3.0)) + Math.pow(p2 / 2, 1 / 3.0) / 3 / a;

        this.d0 = Math.sqrt(b * b / 4 / a / a - 2 * c / 3 / a + p3);
        this.d1 = b * b / 2 / a / a - 4 * c / 3 / a - p3;
        this.d2 = (-cube(b) / cube(a) + 4 * b * c / a / a - 8 * d / a) / 4 / this.d0;
    }

    @Override
    public int getDegree() {
        return 4;
    }

    @Override
    public double evaluate(double x) {
        return this.a * square(square(x)) + this.b * cube(x) + this.c * square(x) + this.d * x + this.e;
    }

    @Override
    protected double computeRoot(int i) {
        int sign0 = i < 2 ? -1 : 1;
        int sign1 = i % 2 == 0 ? -1 : 1;

        return -this.b / (4 * this.a) + sign0 * this.d0 / 2 + sign1 * Math.sqrt(this.d1 + sign0 * this.d2) / 2;
    }
}