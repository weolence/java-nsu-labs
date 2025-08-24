import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.canvas.*;
import javafx.scene.input.MouseButton;
import javafx.beans.property.*;

import game.GameData;
import managers.*;

public class KnivesGame extends Application {
    private Canvas canvas;
    private GraphicsContext gc;

    private final GameData gameData = GameData.getInstance();
    private final LevelManager levelMan = LevelManager.getInstance();
    private final EntitiesManager entitiesMan = EntitiesManager.getInstance();
    private final EventManager eventMan = EventManager.getInstance();
    private final RenderManager renderMan = RenderManager.getInstance();
    private final CollisionManager collisionMan = CollisionManager.getInstance();
 
    @Override
    public void start(Stage stage) {
        double width = gameData.getWidth();
        double height = gameData.getHeight();

        StackPane root = new StackPane();
        Scene scene = new Scene(root, width, height);
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        renderMan.setGraphicsContext(gc);

        DoubleProperty mouseX = new SimpleDoubleProperty();
        DoubleProperty mouseY = new SimpleDoubleProperty();
        BooleanProperty mousePressed = new SimpleBooleanProperty(false);
        eventMan.setListeners(mousePressed, mouseX, mouseY);

        // binding mouse button
        canvas.setOnMouseMoved(e -> {
            mouseX.set(e.getX());
            mouseY.set(e.getY());
        });

        canvas.setOnMousePressed(e -> {
            if(e.getButton() == MouseButton.PRIMARY) {
                mousePressed.set(true);
            }
        });

        canvas.setOnMouseReleased(e -> {
            if(e.getButton() == MouseButton.PRIMARY && mousePressed.get()) {
                mousePressed.set(false);
            }
        });

        new AnimationTimer() {
            private long lastTime = -1;
            private double accumulatedTime = 0;
            private double frameTime = 1.0 / gameData.getFPS();

            @Override
            public void handle(long now) {
                if(lastTime == -1) {
                    lastTime = now;
                    return;
                }

                double dTime = (now - lastTime) / 800_000_000.0;
                lastTime = now;

                accumulatedTime += dTime;

                while(accumulatedTime >= frameTime) {
                    update(dTime);
                    accumulatedTime -= frameTime;
                }

                render();
            }
        }.start();

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void update(double tpf) {
        levelMan.update();

        collisionMan.update();

        entitiesMan.setTpf(tpf);
        entitiesMan.update();

        eventMan.update();
    }

    private void render() {
        renderMan.update();
    } 

    public static void main(String[] args) {
        launch(args);
    }
}
