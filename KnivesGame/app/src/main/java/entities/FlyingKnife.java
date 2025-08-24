package entities;

import dataTypes.Coords;

public class FlyingKnife extends Movable {
    private static final double width = 40;
    private static final double height = 20;
    private double velocityX;
    private double velocityY;

    public FlyingKnife(Coords dot0, Coords dot1, double speed) {
        super(
                (dot0.getX() + dot1.getX()) / 2, 
                (dot0.getY() + dot1.getY()) / 2,
                speed,
                width,
                height
        );

        double dx = dot1.getX() - dot0.getX();
        double dy = dot1.getY() - dot0.getY();
        double length = Math.sqrt(dx * dx + dy * dy);

        speed = length * 10;

        velocityX = (Math.abs(dx) / length) * speed * (dx >= 0 ? -1 : 1);
        velocityY = (Math.abs(dy) / length) * speed * (dy >= 0 ? -1 : 1);
    }

    @Override
    public void setSpeed(double value) { }

    @Override
    public void update(double tpf) {
        this.x += velocityX * tpf;
        this.y += velocityY * tpf;
    }

    public Coords getVelocity() { return new Coords(velocityX, velocityY); }
}
