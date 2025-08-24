package game;

import java.util.ArrayList;

import entities.*;

public class GameData {
    // field characteristics
    private final double width = 1920;
    private final double height = 1080;
    // rendering 
    private final double fps = 60.0;
    private final long clicksDelay = 500; // ms
    // speed of movable objects
    private final double minSpeed = 2;
    private final double maxSpeed = 10;
    // level-constructing
    private final int maxLevel = 3;

    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<FlyingKnife> knives = new ArrayList<>();

    private static GameData instance;

    private GameData() { };

    public static GameData getInstance() {
        if(instance == null) {
            instance = new GameData();
        }
        return instance;
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public double getFPS() { return fps; }
    public long getClicksDelay() { return clicksDelay; }

    public double getMinSpeed() { return minSpeed; }
    public double getMaxSpeed() { return maxSpeed; }

    public int getMaxLevel() { return maxLevel; }
    
    public ArrayList<Group> getGroups() { return groups; }
    public ArrayList<FlyingKnife> getKnives() { return knives; }
}
