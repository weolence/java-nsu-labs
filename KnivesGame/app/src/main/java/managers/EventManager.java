package managers;

import javafx.beans.property.*;

import java.util.ArrayList;

import entities.*;
import dataTypes.Coords;

public class EventManager extends Manager {
    private boolean mousePressed = false;
    private double mouseX;
    private double mouseY;

    private int clicksAmount = 0;
    private long clickTime = 0;
    private Coords firstClick = null;
    private Coords secondClick = null;

    private static EventManager instance;

    private EventManager() { }

    public static EventManager getInstance() {
        if(instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    @Override
    public void update() {
        long time = System.currentTimeMillis();
        if(mousePressed && Math.abs(time - clickTime) > gameData.getClicksDelay()) {
            if(clicksAmount == 0) {
                firstClick = new Coords(mouseX, mouseY);
            } else if(clicksAmount == 1) {
                secondClick = new Coords(mouseX, mouseY);
            }
            clicksAmount++;
            clickTime = time;
        }


        if(clicksAmount == 1) {
            RenderManager renderMan = RenderManager.getInstance();
            renderMan.setTrajectoryLine(firstClick, new Coords(mouseX, mouseY));
            
            double smoothParts = 4;
            double partsOfDelay = (time - clickTime) / (gameData.getClicksDelay() / smoothParts);
            if(partsOfDelay % 1 < 0.1) partsOfDelay -= partsOfDelay % 1;
            if(partsOfDelay % 1 == 0 && partsOfDelay < smoothParts) {
                ArrayList<Group> groups = gameData.getGroups();
                for(Group group : groups) {
                    double dx = group.getX() - firstClick.getX();
                    double dy = group.getY() - firstClick.getY();

                    double distance = Math.sqrt(dx * dx + dy * dy);

                    double k = distance / group.getRadius();
                    k -= k % 1;
                    if(k > smoothParts) k = smoothParts;
                    
                    if(partsOfDelay < smoothParts - k) {
                        group.setSpeed(gameData.getMinSpeed() + partsOfDelay * 2);
                    }
                }
            }
        } else if(time - clickTime >= 5000) {
            double partsOfDelay = (time - 5000 - clickTime) / (gameData.getClicksDelay() / 4) ;
            if(partsOfDelay % 1 == 0 && partsOfDelay <= 4) {
                ArrayList<Group> groups = gameData.getGroups();
                for(Group group : groups) {
                    group.setSpeed(gameData.getMaxSpeed() - partsOfDelay * 2);
                }
            }
        }
    

        if(clicksAmount == 2) {
            EntitiesManager entitiesMan = EntitiesManager.getInstance();
            entitiesMan.generateKnife(firstClick, secondClick);

            RenderManager renderMan = RenderManager.getInstance();
            renderMan.setTrajectoryLine(null, null);

            clicksAmount = 0;            
        } 
    }

    // Configurational functions
    public void setListeners(BooleanProperty mousePressed, DoubleProperty mouseX, DoubleProperty mouseY) {
        mousePressed.addListener((obs, oldVal, newVal) -> {
            this.mousePressed = newVal;
        });

        mouseX.addListener((obs, oldVal, newVal) -> {
            this.mouseX = newVal.doubleValue();
        });


        mouseY.addListener((obs, oldVal, newVal) -> {
            this.mouseY = newVal.doubleValue();
        });
    }
 

    
}
