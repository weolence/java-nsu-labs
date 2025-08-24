package entities;

import java.util.ArrayList;

public class Group {
    private final ArrayList<Movable> entities = new ArrayList<>(); 
    private final ArrayList<Apple> apples = new ArrayList<>();
    private Wheel wheel;

    public void update(double tpf) {
        for(Movable mov : entities) {
            mov.update(tpf);
        }
    }

    public void add(Movable mov) {
        if(mov instanceof Apple) {
            apples.add((Apple)mov);
        } else if(mov instanceof Wheel) {
            wheel = (Wheel)mov;
        }

        entities.add(mov);
    }

    // x-y position of group(wheel exactly)
    public double getX() { return wheel.getX(); }
    public double getY() { return wheel.getY(); }

    // radius of group
    public double getRadius() { return wheel.getRadius(); }

    public void setSpeed(double value) {
        for(Movable mov : entities) {
            mov.setSpeed(value);
        }
    }

    // group data
    public ArrayList<Movable> getEntities() { return entities; }
}
