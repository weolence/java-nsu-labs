package entities;

import dataTypes.Coords;

public class RedApple extends Apple {
    private static final double radius = 25;
    public RedApple(Coords center, double distance, double initAngle, double speed) {
        super(
                center,
                distance,
                initAngle,
                speed,
                radius
        );
    }
}
