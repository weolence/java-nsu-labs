package dataTypes;

public class Coords {
    private double x;
    private double y;

    public Coords(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // X functions
    public double getX() {
        return x;
    }

    public void setX(double value) {
        x = value;
    }

    // Y functions
    public double getY() {
        return y;
    }

    public void setY(double value) {
        y = value;
    }
}
