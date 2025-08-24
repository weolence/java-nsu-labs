package managers;

import java.util.ArrayList;
import java.util.Random;

import dataTypes.Coords;

public class LevelManager extends Manager {
    private final ArrayList<Coords> pattern = new ArrayList<Coords>();

    private boolean isFirstGeneration = true;

    private int applesAmount;
    private int wheelsAmount;
    private int knivesAmount;
    
    private int applesLeft;
    private int knivesLeft;
    private int level;

    private final Random random = new Random();

    private static LevelManager instance;

    private LevelManager() {
        generatePattern();
        level = 1;
    }

    public static LevelManager getInstance() {
        if(instance == null) {
            instance = new LevelManager();
        }
        return instance;
    }

    @Override
    public void update() { 
        if(!anyKnives()) {
            reset();
            return;
        }

        if(!isLevelCompleted() && !isFirstGeneration) return;

        EntitiesManager entitiesMan = EntitiesManager.getInstance();
        entitiesMan.cleanEntities();
        startNextLevel();
        entitiesMan.generateEntities();
    }

    // Functions for checking current values
    public boolean anyKnives() { return knivesLeft > 0; }
    public void createKnife() { knivesLeft++; }
    public void breakKnife() { knivesLeft--; }
    public void breakApple() { applesLeft--; }

    // Functions for getting level data
    public int getScore() { return applesAmount - applesLeft; }
    public int getKnivesLeft() { return knivesLeft; }
    public int getLevel() { return level; }

    // Functions for getting prepared values
    public int getAppleAmount() { return applesAmount; }
    public int getWheelsAmount() { return wheelsAmount; }
    public int getKnivesAmount() { return knivesAmount; }
    public ArrayList<Coords> getPattern() { return pattern; }

    // Level-control functions
    private boolean isLevelCompleted() {
        return applesLeft <= 0;
    }

    private void startNextLevel() {
        if(level + 1 > gameData.getMaxLevel()) level = 1;
        else if(!isFirstGeneration) level++;
        generateLevel();
    } 

    private void generateLevel() {
        applesAmount = level + 2 * random.nextInt(1, 4);
        wheelsAmount = applesAmount >= 10 ? 2 : 1;
        knivesAmount = applesAmount + 3;

        applesLeft = applesAmount;
        knivesLeft = knivesAmount;

        isFirstGeneration = false;
    }

    private void reset() {
        EntitiesManager entitiesMan = EntitiesManager.getInstance();
        entitiesMan.cleanEntities();
        level = 1;
        generateLevel();
        entitiesMan.generateEntities();
        isFirstGeneration = true;
    }

    // Pattern creation(once for whole game)
    private void generatePattern() {
        int number = random.nextInt(2);
        double width = gameData.getWidth();
        double height = gameData.getHeight();
        Coords c1 = new Coords(width / 3, height / 3); // 1st place for wheel
        Coords c2 = new Coords(width - width / 4, height - height / 3); // 2nd place for wheel
        switch(number) {
            case 0:
                pattern.add(c1);
                pattern.add(c2);
                break;
            case 1:
                pattern.add(c2);
                pattern.add(c1);
                break;
        }
    }
}
