package dev.kir.sync.util.math;

public abstract class Function {
    public abstract int getDegree();

    public abstract double evaluate(double x);

    public double getRoot(int i) {
        if (i < 0 || i >= this.getDegree()) {
            throw new IndexOutOfBoundsException();
        }
        return this.computeRoot(i);
    }

    public double[] getRoots() {
        int degree = this.getDegree();
        double[] roots = new double[degree];
        for (int i = 0; i < degree; ++i) {
            roots[i] = this.computeRoot(i);
        }
        return roots;
    }

    protected abstract double computeRoot(int i);

    protected static double square(double x) {
        return x * x;
    }

    protected static double cube(double x) {
        return x * x * x;
    }
}