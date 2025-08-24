package managers;

import javafx.scene.canvas.*;
import javafx.scene.text.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import entities.*;
import dataTypes.Coords;

public class RenderManager extends Manager {
    private final Font font = Font.font("Arial", FontWeight.BOLD, 24);
    private GraphicsContext gc;
    private Image knifeImage;
    private Image goldenApple;
    private Image redApple;
    private Image wheelImage;
    private Image backgroundImage;

    private Coords dot1;
    private Coords dot2;

    private static RenderManager instance;

    private RenderManager() {
        loadImages();
    }

    public static RenderManager getInstance() { 
        if(instance == null) {
            instance = new RenderManager();
        }
        return instance;
    }

    // Rendering functions
    @Override
    public void update() {
        gc.clearRect(0, 0, gameData.getWidth(), gameData.getHeight());

        gc.drawImage(backgroundImage, 0, 0, gameData.getWidth(), gameData.getHeight());

        for(Group group : gameData.getGroups()) {
            ArrayList<Movable> entities = group.getEntities();
            for(Movable mov : entities) {
                render(mov);
            }

            /*
            double rad = group.getRadius(); 
            gc.setFill(Color.BLACK);
            gc.strokeOval(group.getX() - rad, group.getY() - rad, rad * 2, rad * 2);
            gc.strokeOval(group.getX() - 2 * rad, group.getY() - 2 * rad, rad * 4, rad * 4);
            gc.strokeOval(group.getX() - 3 * rad, group.getY() - 3 * rad, rad * 6, rad * 6);
            gc.strokeOval(group.getX() - 4 * rad, group.getY() - 4 * rad, rad * 8, rad * 8);
            */
        }

        for(FlyingKnife knife : gameData.getKnives()) {
            render(knife);
        }
        
        if(dot1 != null && dot2 != null) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeLine(dot1.getX(), dot1.getY(), dot2.getX(), dot2.getY());
        }

        LevelManager levelMan = LevelManager.getInstance();
        gc.setFont(font);
        gc.setFill(Color.ORANGE);
        double textX = 10;
        double textY = 50;
        gc.fillText("Level: " + levelMan.getLevel(), textX, textY);
        gc.fillText("Score: " + levelMan.getScore(), textX, textY + 25);
        gc.fillText("Knives left: " + levelMan.getKnivesLeft(), textX, textY + 25 * 2);
    }

    private void render(Movable mov) {
        double x = mov.getX();
        double y = mov.getY();
 
        if(mov instanceof FlyingKnife) {
            FlyingKnife flyingKnife = (FlyingKnife)mov;

            double width = flyingKnife.getWidth();
            double height = flyingKnife.getHeight();

            Coords velocity = flyingKnife.getVelocity();
            double velocityX = velocity.getX();
            double velocityY = velocity.getY();

            double angle = Math.toDegrees(Math.atan2(velocityY, velocityX)) + 35; // +35 angle which corrects image's angle

            gc.save();
            gc.translate(x, y);
            gc.rotate(angle);
            gc.drawImage(knifeImage, -width / 2, -height / 2, width, height);
            gc.restore(); 
        } else if(mov instanceof Apple) {
            Apple apple = (Apple)mov;

            double d = 2 * apple.getRadius();

            Coords center = apple.getCenter();

            double angle = Math.toDegrees(Math.atan2(center.getY() - y, center.getX() - x) - Math.PI / 2);

            gc.save();
            gc.translate(x, y);
            gc.rotate(angle);
            if(apple instanceof RedApple) {
                gc.drawImage(redApple, -d / 2 ,-d / 2, d, d);
            } else if(apple instanceof GoldenApple) {
                gc.drawImage(goldenApple, -d / 2 ,-d / 2, d, d);
            }
            gc.restore();
        } else if(mov instanceof Wheel) {
            Wheel wheel = (Wheel)mov;

            double d = 2 * wheel.getRadius();

            double angle = wheel.getAngle();

            gc.save();
            gc.translate(x, y);
            gc.rotate(angle);
            gc.drawImage(wheelImage, -d / 2, -d / 2, d, d);
            gc.restore();
        }
    }

    public void setTrajectoryLine(Coords dot1, Coords dot2) {
        this.dot1 = dot1;
        this.dot2 = dot2;
    }

    // Graphics context functions
    public void setGraphicsContext(GraphicsContext gc) { this.gc = gc; }

    private void loadImages() {
        knifeImage = new Image("knife1.png");
        goldenApple = new Image("golden_apple.png");
        redApple = new Image("red_apple.png");
        wheelImage = new Image("wooden_wheel.png"); 
        backgroundImage = new Image("background1.jpg");
    }
}
