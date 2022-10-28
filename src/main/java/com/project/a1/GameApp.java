package com.project.a1;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

abstract class GameObject extends Group {
    boolean isBoundOn = false;
    Rectangle bound = new Rectangle(getBoundsInParent().getMinX(),
            getBoundsInParent().getMinY(),
            getBoundsInParent().getWidth(),
            getBoundsInParent().getHeight());

    public void showBoundingBox() {
        updateBound();
        checkIfBoundOn();
    }

    public void updateBoundingBox() {
        updateBound(); 
        if (isBoundOn) {
            getChildren().add(bound);
            bound.setFill(Color.TRANSPARENT);
            bound.setStroke(Color.WHITE);
        }
    }

    private void updateBound() {
        getChildren().remove(bound);
        bound = new Rectangle(getBoundsInParent().getMinX(),
                getBoundsInParent().getMinY(),
                getBoundsInParent().getWidth(),
                getBoundsInParent().getHeight());
    }

    private void checkIfBoundOn() { 
        bound.setFill(Color.TRANSPARENT);
        if (!isBoundOn) {
            getChildren().add(bound);
            bound.setStroke(Color.WHITE);
            isBoundOn = true;

        } else if (isBoundOn) {
            getChildren().remove(bound);
            isBoundOn = false;
        }
    }
}
interface Updateable {
    public void update();
}
class GameText extends GameObject {
    private static final Scale SCALE = new Scale(1, -1);
    private static final FontWeight FONT_WEIGHT = FontWeight.NORMAL;
    public static final int FONT_SIZE = 15;

    Label l = new Label();
    Color color;
    Point2D loc;

    GameText(String s, Point2D pos, Color c) {
        color = c;
        loc = pos;
        getTransforms().addAll(new Translate(loc.getX(), 
            loc.getY()), SCALE);
        generateLabel(s, c);
        getChildren().add(l);
    }

    private void generateLabel(String s, Color c) {
        l = new Label(s);
        l.setFont(Font.font("Arial", FONT_WEIGHT, FONT_SIZE));
        l.setTextFill(c);
    }

    public void moveLabel(Point2D pos) {
        getTransforms().add(new Translate(pos.getX(), pos.getY()));
    }

    public void updateLabel(String s) {
        getChildren().remove(l);
        generateLabel(s, color);
        getChildren().add(l);
    }
}

class Pond extends GameObject implements Updateable {
    private static final Color POND_COLOR = Color.BLUE;
    private static final double PERCENT_VALUE = 0.01;

    Circle circle;
    GameText pondLabel;
    Random r = new Random();
    Point2D pos;
    double maxSize, currentSize, percentAdder;

    Pond(Point2D s, double size) {
        double randomNum = ThreadLocalRandom.current().nextDouble(1, size + 1);
        pos = s;
        maxSize = size;
        currentSize = size - randomNum;
        percentAdder = (maxSize - currentSize) * PERCENT_VALUE;
        createPond();
    }

    @Override
    public void update() {
        if (currentSize < maxSize) {
            getChildren().clear();
            currentSize += percentAdder;
            createPond();
        }
    }

    private void createPond() {
        circle = new Circle(pos.getX(), pos.getY(), currentSize);
        circle.setFill(POND_COLOR);
        pondLabel = new GameText(String.format("%.0f%%",
                (currentSize / maxSize) * 100),
                new Point2D(
                        circle.getBoundsInParent().getCenterX() - 
                            (GameText.FONT_SIZE / 2),
                        circle.getBoundsInParent().getCenterY() +
                            (GameText.FONT_SIZE / 2)),
                Color.WHITE);
        getChildren().addAll(circle, pondLabel);

    }

    public double getSize() { return (currentSize / maxSize) * 100; }

}
class Cloud extends GameObject implements Updateable{
    private static final double MAX_COLOR_VALUE = 255;
    private static final double MIN_COLOR_VALUE = 155;
    private static final double PERCENT_VALUE = 0.01;
    private static final double PERCENT_ADDER = (MAX_COLOR_VALUE -
            MIN_COLOR_VALUE) * PERCENT_VALUE;

    Circle circle;
    GameText cloudLabel;
    double percentage, cloudColorValue;

    Cloud(Point2D s, double size) {
        circle = new Circle(s.getX(), s.getY(), size);
        cloudColorValue = 0;
        percentage = cloudColorValue / MAX_COLOR_VALUE;
        percentage *= 100;
        circle.setFill(Color.rgb((int)(MAX_COLOR_VALUE - cloudColorValue),
                (int)(MAX_COLOR_VALUE - cloudColorValue),
                (int)(MAX_COLOR_VALUE - cloudColorValue)));
        cloudLabel = new GameText(String.format("%.0f%%", percentage),
                    new Point2D(
                        circle.getBoundsInParent().getCenterX() - 
                                (GameText.FONT_SIZE / 2),
                        circle.getBoundsInParent().getCenterY() + 
                                (GameText.FONT_SIZE / 2)),
                        Color.BLACK);
        getChildren().addAll(circle, cloudLabel);
    }

    @Override
    public void update() {
        if (cloudColorValue < (MAX_COLOR_VALUE - MIN_COLOR_VALUE)) {
            cloudColorValue += PERCENT_ADDER;
            updateCloudValue();
        }           
    }

    public void decrease() {
        if (cloudColorValue > 0) {
            cloudColorValue -= PERCENT_ADDER;
            updateCloudValue();
        }
    }

    private void updateCloudValue() {
        circle.setFill(Color.rgb((int)(MAX_COLOR_VALUE - cloudColorValue),
                    (int)(MAX_COLOR_VALUE - cloudColorValue),
                    (int)(MAX_COLOR_VALUE - cloudColorValue)));
        percentage = cloudColorValue / (MAX_COLOR_VALUE - MIN_COLOR_VALUE);
        percentage *= 100;
        updateLabel(percentage);
    }

    private void updateLabel(double val) {
        cloudLabel.updateLabel(String.format("%.0f%%", val));
    }

    public double getPercentage() { return percentage; }
}
class Helipad extends GameObject {

    private static final int GAP = 5;

    Rectangle rect;
    Circle circle;

    Helipad(Point2D s, Color c, int w, int h) {
        rect = new Rectangle(s.getX(), s.getY(), w, h);
        circle = new Circle(rect.getX() + rect.getWidth() / 2,
                rect.getY() + rect.getHeight() / 2,
                (rect.getWidth() / 2) - GAP);

        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(c);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(c);
        getChildren().addAll(rect, circle);
    }

    public Point2D getCenter() {
        return new Point2D(circle.getCenterX(), circle.getCenterY());
    }
}
class Helicopter extends GameObject {

    private static final int ROTATION_ANGLE = 15;
    private static final int HEADING_LENGTH = 30;
    private static final int FUEL_CONSUMPTION = 1;
    private static final int MAX_SPEED = 10;
    private static final int MIN_SPEED = -2;
    private static final int LABEL_GAP = 10;
    private static final double ACCELERATION = 0.1;

    Circle circle;
    Line line;
    GameText fuelLabel;
    Group helicopter, label;
    boolean engineStart = false;
    double speed = 0;
    int fuelValue = 0;

    Helicopter(Point2D s, Color c, double r, int startFuel) {
        helicopter = new Group();
        circle = new Circle(s.getX(), s.getY(), r, c);
        line = new Line(circle.getCenterX(),
                circle.getCenterY(),
                circle.getCenterX(),
                circle.getCenterY() + circle.getRadius() + HEADING_LENGTH);
        
        fuelValue = startFuel;
        fuelLabel = new GameText(String.format("F: %d",fuelValue), 
                new Point2D(
                circle.getBoundsInParent().getCenterX() -
                        LABEL_GAP,
                circle.getBoundsInParent().getCenterY() -
                        LABEL_GAP),
                c);
        line.setStroke(c);
        helicopter.getChildren().addAll(circle, line, fuelLabel);
        getChildren().add(helicopter);
    }

    public void consumeFuel() { 
        if (fuelValue > 0)
            updateFuel(fuelValue -= FUEL_CONSUMPTION);
    }

    public boolean isIgnitionOn() { return engineStart; }

    private void updateFuel(int f) {
        fuelLabel.updateLabel(String.format("F: %d", f));
    }

    public void left() {
        helicopter.getTransforms()
                .add(new Rotate(
                        ROTATION_ANGLE,
                        circle.getBoundsInParent().getCenterX(),
                        circle.getBoundsInParent().getCenterY()));
    }

    public void right() {
        helicopter.getTransforms()
                .add(new Rotate(
                        -ROTATION_ANGLE,
                        circle.getBoundsInParent().getCenterX(),
                        circle.getBoundsInParent().getCenterY()));
    }

    public void moveHeli() {
        if (engineStart)
            helicopter.getTransforms().add(new Translate(0, speed));
        else
            return;
    }

    public void engineStart() {
        if (!engineStart)
            engineStart = true;
        else
            engineStart = false;
    }

    public void increaseSpeed() {
        if (engineStart && speed <= MAX_SPEED)
            speed += ACCELERATION;
        else
            return;
    }

    public void decreaseSpeed() {
        if (engineStart && speed >= MIN_SPEED) 
            speed -= ACCELERATION;
        else
            return;
    }

    public double getSpeed() { return speed; }

    public int getFuel() { return fuelValue; }
}

class Game extends Pane {
    public static final int GAME_WIDTH = 400;
    public static final int GAME_HEIGHT = 800;

    private static final int HELI_RADIUS = GAME_WIDTH / 30;
    private static final Color HELI_COLOR = Color.YELLOW;
    private static final int START_FUEL = 250000;

    private static final int POND_SIZE = GAME_WIDTH / 3;
    private static final int CLOUD_SIZE = GAME_WIDTH / 10;

    private static final int HELIPAD_SIZE = GAME_WIDTH / 5;
    private static final Color HELIPAD_COLOR = Color.GRAY;

    private static final Scale SCALE = new Scale(1, -1);

    private static final double PERCENT_THRESHOLD = 30.0;

    Random r = new Random();
    Helicopter heli;
    Helipad helipad;
    Pond pond;
    Cloud cloud;

    public Game() {
        createGameObjects();
        setUpUI();
        startAnimation();
    }

    public void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {

            @Override
            public void handle(long now) {

                checkIfWon();

                heli.moveHeli();
                if (heli.isIgnitionOn()) {
                    heli.consumeFuel();
                }

                heli.updateBoundingBox();
                pond.updateBoundingBox();

                if (now % 2000 == 0) {
                    cloud.decrease();
                    System.out.println(heli.getSpeed());
                }

                if (now % 600 == 0) {
                    if (cloud.getPercentage() > PERCENT_THRESHOLD) {
                        pond.update();
                    }
                }
            }

            private void checkIfWon() {
                if (heli.getFuel() > 0 && 
                    heli.getBoundsInParent().
                        intersects(helipad.getBoundsInParent()) &&
                    (int) heli.getSpeed() == 0 && 
                    pond.getSize() >= 100.0) {
                        System.out.println("you won!");
                        handleReset();
                }
            }
        };
        loop.start();
    }
    
    public void createGameObjects() {
        createHelipad();
        createHelicopter();
        createPond();
        createCloud();
    }

    public void setUpUI() {
        SCALE.setPivotY(GAME_HEIGHT / 2);
        setStyle("-fx-background-color: black;");
        getTransforms().add(SCALE);
        getChildren().addAll(helipad, pond, cloud, heli);
    }

    public void createHelipad() {
        helipad = new Helipad(new Point2D(
            GAME_WIDTH / 2 - HELIPAD_SIZE / 2,
            GAME_HEIGHT / 10),
            HELIPAD_COLOR,
            HELIPAD_SIZE,
            HELIPAD_SIZE);
    }

    public void createHelicopter() {
        heli = new Helicopter(new Point2D(
            helipad.getCenter().getX(),
            helipad.getCenter().getY()),
            HELI_COLOR,
            HELI_RADIUS,
            START_FUEL);
    }

    public void createPond() {
        pond = new Pond(new Point2D(
            r.nextInt(((GAME_WIDTH - POND_SIZE) - 
            POND_SIZE) + 1) +
            POND_SIZE, 
            r.nextInt(((GAME_HEIGHT - POND_SIZE) - 
            ((int)(helipad.getBoundsInParent().getMaxY() + POND_SIZE))) + 1) +
            (int)(helipad.getBoundsInParent().getMaxY() + POND_SIZE)),
            POND_SIZE);
    }

    public void createCloud() {
        cloud = new Cloud(new Point2D(
            r.nextInt(((GAME_WIDTH - CLOUD_SIZE) -
            CLOUD_SIZE) + 1) +
            CLOUD_SIZE,
            r.nextInt(((GAME_HEIGHT - CLOUD_SIZE) -
            ((int)(helipad.getBoundsInParent().getMaxY()) + CLOUD_SIZE) + 1)) +
            (int)(helipad.getBoundsInParent().getMaxY() + CLOUD_SIZE)),
            CLOUD_SIZE);
    }

    public void handleMovement(KeyEvent e) {
        if (e.getCode() == KeyCode.LEFT) {
            heli.left();
        }

        if (e.getCode() == KeyCode.RIGHT) {
            heli.right();
        }

        if (e.getCode() == KeyCode.UP) {
            heli.increaseSpeed();
        }

        if (e.getCode() == KeyCode.DOWN) {
            heli.decreaseSpeed();
        }
    }

    public void handleSeeding() {
        if (heli.getBoundsInParent().
            intersects(cloud.getBoundsInParent())) {
                cloud.update();   
        }
    }

    public void handleIgnition() {
        heli.engineStart();
    }

    public void handleBoundBoxes() {
        heli.showBoundingBox();
        helipad.showBoundingBox();
        pond.showBoundingBox();
        cloud.showBoundingBox();
    }

    public void handleReset() {
        getChildren().clear();
        getTransforms().clear();
        createGameObjects();
        setUpUI();
    }

}

public class GameApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        Game root = new Game();
        scene = new Scene(root, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        stage.setScene(scene);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT ||
                    e.getCode() == KeyCode.RIGHT ||
                    e.getCode() == KeyCode.UP ||
                    e.getCode() == KeyCode.DOWN) {
                root.handleMovement(e);
            }
            if (e.getCode() == KeyCode.I) {
                root.handleIgnition();
            }
            if (e.getCode() == KeyCode.B) {
                root.handleBoundBoxes();
            }
            if (e.getCode() == KeyCode.R) {
                root.handleReset();
            }
            if (e.getCode() == KeyCode.SPACE) {
                root.handleSeeding();
            }
        });
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}