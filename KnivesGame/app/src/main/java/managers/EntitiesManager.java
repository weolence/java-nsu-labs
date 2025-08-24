package managers;

import java.util.ArrayList;
import java.util.Random;

import dataTypes.Coords;
import entities.*;

public class EntitiesManager extends Manager {
    private final Random random = new Random();
    private double tpf;

    private static EntitiesManager instance;

    private EntitiesManager() { }

    public static EntitiesManager getInstance() {
        if(instance == null) {
            instance = new EntitiesManager();
        }
        return instance;
    }

    @Override
    public void update() {
        for(Group group : gameData.getGroups()) {
            group.update(tpf);
        }
        
        for(FlyingKnife knife : gameData.getKnives()) {
            knife.update(tpf);
        }
    }
    
    public void generateKnife(Coords dot0, Coords dot1) {
        LevelManager levelMan = LevelManager.getInstance();
        if(!levelMan.anyKnives()) return;
        FlyingKnife knife = new FlyingKnife(dot0, dot1, 0);
        gameData.getKnives().add(knife);
    }
        
    public void generateEntities() {
        LevelManager levelMan = LevelManager.getInstance();
        CollisionManager collisionMan = CollisionManager.getInstance();

        int applesAmount = levelMan.getAppleAmount();
        int wheelsAmount = levelMan.getWheelsAmount(); 

        ArrayList<Coords> pattern = levelMan.getPattern();

        for(int i = 0; i < wheelsAmount; ++i) {
            Coords coords = pattern.get(i);
            double x = coords.getX();
            double y = coords.getY();

            Group group = new Group();
            group.add(new Wheel(x, y, gameData.getMinSpeed()));

            gameData.getGroups().add(group);
        } 

        for(int i = 0; i < applesAmount; ++i) {
            Group group = gameData.getGroups().get(i % wheelsAmount);
            double wheelX = group.getX();
            double wheelY = group.getY();
            double wheelRadius = group.getRadius() + 10;  

            double angle; 
            Apple apple;

            outerLoop:
            while(true) {
                angle = random.nextDouble(Math.toDegrees(0), Math.toDegrees(360));
                int randomNum = random.nextInt(0, 3);
                switch(randomNum % 4) {
                    case 0: apple = new GoldenApple(new Coords(wheelX, wheelY), wheelRadius, angle, gameData.getMinSpeed());
                            break;
                    default: apple = new RedApple(new Coords(wheelX, wheelY), wheelRadius, angle, gameData.getMinSpeed());
                }
                for(Movable mov : group.getEntities()) {
                    if(mov instanceof Wheel) continue;
                    if(collisionMan.isColliding(apple, mov)) {
                        continue outerLoop;
                    }
                }
                break;
            }

            if(apple != null) group.add(apple);
        }
    }
    
    public void setTpf(double value) { tpf = value; }

    public void cleanEntities() {
        gameData.getGroups().clear();
        gameData.getKnives().clear();
    } 
}
