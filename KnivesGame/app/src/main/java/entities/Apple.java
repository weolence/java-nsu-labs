package entities;

import dataTypes.Coords;

public abstract class Apple extends Movable {
    protected final double radius;
    protected double distance;
    protected double wheelX;
    protected double wheelY;
    protected double angle;
    
    protected Apple(Coords center, double distance, double initAngle, double speed, double radius) {
        super(
                center.getX() + distance * Math.cos(initAngle), 
                center.getY() + distance * Math.sin(initAngle),
                speed,
                radius * 2,
                radius * 2
        );
        this.wheelX = center.getX();
        this.wheelY = center.getY();
        this.distance = distance;
        this.angle = initAngle;
        this.radius = radius;
    }

    @Override
    public void update(double tpf) {
        angle += speed * tpf;
        x = wheelX + distance * Math.cos(angle);
        y = wheelY + distance * Math.sin(angle);
    }

    public double getRadius() { return radius; }
    public Coords getCenter() { return new Coords(wheelX, wheelY); } 
}
