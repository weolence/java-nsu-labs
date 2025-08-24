package managers;

import java.util.ArrayList;

import entities.*;

public class CollisionManager extends Manager {
    private final ArrayList<Group> groups = gameData.getGroups();
    private final ArrayList<FlyingKnife> knives = gameData.getKnives();

    private static CollisionManager instance;

    private CollisionManager() { }
    
    public static CollisionManager getInstance() {
        if(instance == null) {
            instance = new CollisionManager();
        }
        return instance;
    }

    @Override
    public void update() {
        LevelManager levelMan = LevelManager.getInstance();
        outerLoop:
        for(int i = 0; i < knives.size(); i++) {
            FlyingKnife knife = knives.get(i);
            for(Group group : groups) {
                ArrayList<Movable> groupEntities = group.getEntities();
                for(int j = 0; j < groupEntities.size(); j++) {
                    Movable mov = groupEntities.get(j);
                    if(!isKnifeColliding(knife, mov)) continue;

                    if(mov instanceof Wheel) {
                        knives.remove(i);
                        levelMan.breakKnife();
                        continue outerLoop;
                    } else if(mov instanceof Apple) {
                        if(mov instanceof GoldenApple) {
                            levelMan.createKnife();
                            levelMan.createKnife();
                        }
                        knives.remove(i);
                        levelMan.breakKnife();
                        groupEntities.remove(j);
                        levelMan.breakApple();
                        continue outerLoop;
                    }
                }
            }
        }
    }

    private boolean isKnifeColliding(FlyingKnife knife, Movable mov) {
        if(mov instanceof Wheel) {
            Wheel wheel = (Wheel)mov;
            double wheelX = wheel.getX();
            double wheelY = wheel.getY();
            double wheelRadius = wheel.getRadius();

            double knifeX = knife.getX();
            double knifeY = knife.getY();
            double knifeWidth = knife.getWidth();
            double knifeHeight = knife.getHeight();

            double closestX = Math.max(knifeX - knifeWidth / 2, Math.min(knifeX + knifeWidth / 2, wheelX));
            double closestY = Math.max(knifeY - knifeHeight / 2, Math.min(knifeY + knifeHeight / 2, wheelY));

            double dx = wheelX - closestX;
            double dy = wheelY - closestY;

            double distance = dx * dx + dy * dy;

            return distance < wheelRadius * wheelRadius;
        } else if(mov instanceof Apple) {
            return isColliding(knife, mov);
        }
        return false;
    }

    public boolean isColliding(Movable obj1, Movable obj2) {
        double obj1MinX = obj1.getX() - obj1.getWidth() / 2;
        double obj1MaxX = obj1.getX() + obj1.getWidth() / 2;
        double obj1MinY = obj1.getY() - obj1.getHeight() / 2;
        double obj1MaxY = obj1.getY() + obj1.getHeight() / 2;

        double obj2MinX = obj2.getX() - obj2.getWidth() / 2;
        double obj2MaxX = obj2.getX() + obj2.getWidth() / 2;
        double obj2MinY = obj2.getY() - obj2.getHeight() / 2;
        double obj2MaxY = obj2.getY() + obj2.getHeight() / 2;

        return obj1MaxX > obj2MinX &&
            obj1MinX < obj2MaxX &&
            obj1MaxY > obj2MinY &&
            obj1MinY < obj2MaxY;
    }
}
