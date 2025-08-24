package entities;

import game.GameData;

public abstract class Movable {
    protected GameData gameData = GameData.getInstance();
    protected double width;
    protected double height;
    protected double x;
    protected double y;
    protected double speed;

    protected Movable(double x, double y, double speed, double width, double height) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.width = width;
        this.height = height;
    }

    // Functions for overriding
    public abstract void update(double tpf);

    public void setSpeed(double value) {
        double maxSpeed = gameData.getMaxSpeed();
        double minSpeed = gameData.getMinSpeed();
        if(value <= maxSpeed && value >= minSpeed) {
            speed = value;
        } else {
            speed = value > maxSpeed ? maxSpeed : minSpeed;
        }
    }

    // X functions
    public double getX() { return x; }
    public void setX(double value) { x = value; }

    // Y functions
    public double getY() { return y; }
    public void setY(double value) { y = value; }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
