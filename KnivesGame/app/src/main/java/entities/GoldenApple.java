package entities;

import dataTypes.Coords;

public class GoldenApple extends Apple {
    private static final double radius = 20;
    public GoldenApple(Coords center, double distance, double initAngle, double speed) {
        super(
                center,
                distance,
                initAngle,
                speed,
                radius
        );
    }
}
