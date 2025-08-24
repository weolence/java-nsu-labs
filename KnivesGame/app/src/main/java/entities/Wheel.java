package entities;

public class Wheel extends Movable {
    private static final double radius = 150;
    private double angle = 0;

    public Wheel(double x, double y, double speed) {
        super(
                x, 
                y, 
                speed,
                radius * 2,
                radius * 2
        );
    }

    @Override
    public void update(double tpf) { 
        angle += Math.toDegrees(speed) * tpf; 
    }

    public double getRadius() { return radius; }
    public double getAngle() { return angle; }  
}
