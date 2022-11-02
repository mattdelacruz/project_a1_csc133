package com.project.a1;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
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
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

abstract class GameObject extends Group {
    private static final Color BOUND_FILL = Color.TRANSPARENT;
    private static final Color BOUND_STROKE = Color.WHITE;
    
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
            bound.setFill(BOUND_FILL);
            bound.setStroke(BOUND_STROKE);
        }
    }

    public GameText createLabel(String s, Point2D p, Color c) {
        return new GameText(s,p,c);
    }

    private void updateBound() {
        getChildren().remove(bound);
        bound = new Rectangle(getBoundsInParent().getMinX(),
                getBoundsInParent().getMinY(),
                getBoundsInParent().getWidth(),
                getBoundsInParent().getHeight());
    }

    private void checkIfBoundOn() { 
        bound.setFill(BOUND_FILL);
        if (!isBoundOn) {
            getChildren().add(bound);
            bound.setStroke(BOUND_STROKE);
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
    private static final String FONT_FAMILY = "Arial";
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
        l.setFont(Font.font(FONT_FAMILY, FONT_WEIGHT, FONT_SIZE));
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
    private static final Color FONT_COLOR = Color.WHITE;
    private static final double PERCENT_VALUE = 0.01;
    private static final String LABEL_FORMAT = "%.0f%%";

    Circle circle;
    GameText pondLabel;
    Random r = new Random();
    Point2D pos;
    double currentRadius, maxRadius, percentAdder, currentArea, maxArea;
    

    Pond(Point2D s, double radius) {
        double randomNum = ThreadLocalRandom.current().
                nextDouble(1, radius + 1);
        pos = s;
        maxRadius = radius;
        currentRadius = radius - randomNum;
        currentArea = Math.PI * Math.pow(currentRadius, 2);
        maxArea = Math.PI * Math.pow(maxRadius, 2);
        percentAdder =  (maxArea - currentArea) * PERCENT_VALUE;
        createPond();
    }

    @Override
    public void update() {
        if (currentArea < maxArea) {
            getChildren().clear();
            currentArea += percentAdder;
            createPond();
        }
    }

    private void createPond() {
        circle = new Circle(pos.getX(), pos.getY(), 
                Math.sqrt(currentArea / Math.PI));
        circle.setFill(POND_COLOR);
        pondLabel = createLabel(String.format(LABEL_FORMAT,
                getSize()),
                new Point2D(
                        circle.getBoundsInParent().getCenterX() - 
                            (GameText.FONT_SIZE / 2),
                        circle.getBoundsInParent().getCenterY() +
                            (GameText.FONT_SIZE / 2)),
                FONT_COLOR);
        getChildren().addAll(circle, pondLabel);
    }

    public double getSize() { return (currentArea / maxArea) * 100; }
}
class Cloud extends GameObject implements Updateable {
    private static final double MAX_COLOR_VALUE = 255;
    private static final double MIN_COLOR_VALUE = 155;
    private static final double PERCENT_VALUE = 0.01;
    private static final double PERCENT_ADDER = (MAX_COLOR_VALUE -
            MIN_COLOR_VALUE) * PERCENT_VALUE;
    private static final String LABEL_FORMAT = "%.0f%%";
    private static final Color FONT_COLOR = Color.BLACK;

    Circle circle;
    GameText cloudLabel;
    double percentage, cloudColorValue;

    Cloud(Point2D s, double size) {
        circle = new Circle(s.getX(), s.getY(), size);
        cloudColorValue = 0;
        percentage = cloudColorValue / MAX_COLOR_VALUE;
        percentage *= 100;
        circle.setFill(Color.rgb(
                (int)(MAX_COLOR_VALUE - cloudColorValue),
                (int)(MAX_COLOR_VALUE - cloudColorValue),
                (int)(MAX_COLOR_VALUE - cloudColorValue)));
        cloudLabel = createLabel(String.format(LABEL_FORMAT, percentage),
                    new Point2D(
                        circle.getBoundsInParent().getCenterX() - 
                                (GameText.FONT_SIZE / 2),
                        circle.getBoundsInParent().getCenterY() + 
                                (GameText.FONT_SIZE / 2)),
                        FONT_COLOR);
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
        cloudLabel.updateLabel(String.format(LABEL_FORMAT, val));
    }

    public double getPercentage() { return percentage; }
}
class Helipad extends GameObject {

    private static final int GAP = 5;
    private static final Color HELIPAD_FILL = Color.TRANSPARENT;

    Rectangle rect;
    Circle circle;

    Helipad(Point2D s, Color c, int w, int h) {
        rect = new Rectangle(s.getX(), s.getY(), w, h);
        circle = new Circle(rect.getX() + rect.getWidth() / 2,
                rect.getY() + rect.getHeight() / 2,
                (rect.getWidth() / 2) - GAP);
        rect.setFill(HELIPAD_FILL);
        rect.setStroke(c);
        circle.setFill(HELIPAD_FILL);
        circle.setStroke(c);
        getChildren().addAll(rect, circle);
    }

    public Point2D getCenter() {
        return new Point2D(circle.getCenterX(), circle.getCenterY());
    }
}
class Helicopter extends GameObject {
    private static final int ROTATION_ANGLE = 15;
    private static final int HEADING_LENGTH = 50;
    private static final int FUEL_CONSUMPTION = 1;
    private static final int MAX_SPEED = 10;
    private static final int MIN_SPEED = -2;
    private static final int LABEL_GAP = 15;
    private static final double ACCELERATION = 0.1;
    private static final String LABEL_FORMAT = "F: %d";

    Circle circle;
    Line line;
    GameText fuelLabel;
    Group helicopter, label;
    boolean engineOn = false;
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
        fuelLabel = createLabel(String.format(LABEL_FORMAT, fuelValue), 
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

    public boolean isIgnitionOn() { return engineOn; }

    private void updateFuel(int f) {
        fuelLabel.updateLabel(String.format(LABEL_FORMAT, f));
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
        if (engineOn)
            helicopter.getTransforms().add(new Translate(0, speed));
        else
            return;
    }

    public void engineStart() {
        if (!engineOn)
            engineOn = true;
        else
            engineOn = false;
    }

    public void increaseSpeed() {
        if (engineOn && speed <= MAX_SPEED)
            speed += ACCELERATION;
        else
            return;
    }

    public void decreaseSpeed() {
        if (engineOn && speed >= MIN_SPEED) 
            speed -= ACCELERATION;
        else
            return;
    }

    public double getSpeed() { return speed; }
    public int getFuel() { return fuelValue; }
}

class Game extends Pane implements Updateable{
    public static final int GAME_WIDTH = 400;
    public static final int GAME_HEIGHT = 800;
    private static final int HELI_RADIUS = GAME_WIDTH / 30;
    private static final int START_FUEL = 250000;
    private static final int POND_SIZE = GAME_WIDTH / 3;
    private static final int CLOUD_SIZE = GAME_WIDTH / 10;
    private static final int HELIPAD_SIZE = GAME_WIDTH / 5;
    private static final int CLOUD_DECAY_TIME = 2000;
    private static final int POND_UPDATE_TIME = 600;
    private static final double PERCENT_THRESHOLD = 30.0;
    private static final Color HELIPAD_COLOR = Color.GRAY;
    private static final Color HELI_COLOR = Color.YELLOW;
    private static final Scale SCALE = new Scale(1, -1);
    private static final String LOSE_TEXT = "You have lost! Play again?";
    private static final String WIN_TEXT = "You have won! Play again?";
    
    Random r = new Random();
    Helicopter heli;
    Helipad helipad;
    Pond pond;
    Cloud cloud;
    Alert alert = new Alert(AlertType.CONFIRMATION);
    ButtonType Yes = new ButtonType("Yes", ButtonData.YES);
    ButtonType No = new ButtonType("No", ButtonData.NO);

    public Game() {
        init();
        startAnimation();
    }

    private void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                
                if (heli.isIgnitionOn()) { heli.consumeFuel(); }

                if (now % CLOUD_DECAY_TIME == 0) { cloud.decrease(); }

                if (now % POND_UPDATE_TIME == 0) {
                    if (cloud.getPercentage() > PERCENT_THRESHOLD)
                        pond.update();
                }

                if (checkIfLose() || checkIfWon()) {
                    stop();
                    getEndResult();
                }
            }
        };
        loop.start();
    }

    private void init() {
        createGameObjects();
        setUpUI();
    }

    private void getEndResult() {
        Platform.runLater(() -> {
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == Yes) {
                handleReset();
            }
            else if (result.isPresent() && result.get() == No) {
                Platform.exit();
            }
        });        
    }

    private boolean checkIfLose() {
        if (heli.getFuel() == 0) {
            createDialogBox(LOSE_TEXT);
            return true;
        }
        return false;
    }

    private boolean checkIfWon() {
        if (heli.getFuel() > 0 && 
            heli.getBoundsInParent().
                intersects(helipad.getBoundsInParent()) &&
            (int)heli.getSpeed() == 0 && 
            pond.getSize() >= 100.0) {
                createDialogBox(WIN_TEXT);
                return true;
        }
        return false;
    }

    private void createDialogBox(String s) {
        alert = new Alert(AlertType.CONFIRMATION);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(Yes, No);
        alert.setTitle(s);
        alert.setContentText(s);             
    }
    
    private void createGameObjects() {
        createHelipad();
        createHelicopter();
        createPond();
        createCloud();
    }

    private void setUpUI() {
        SCALE.setPivotY(GAME_HEIGHT / 2);
        setStyle("-fx-background-color: black;");
        getTransforms().add(SCALE);
        getChildren().addAll(helipad, pond, cloud, heli);
    }

    private void createHelipad() {
        helipad = new Helipad(new Point2D(
            GAME_WIDTH / 2 - HELIPAD_SIZE / 2,
            GAME_HEIGHT / 10),
            HELIPAD_COLOR,
            HELIPAD_SIZE,
            HELIPAD_SIZE);
    }

    private void createHelicopter() {
        heli = new Helicopter(new Point2D(
            helipad.getCenter().getX(),
            helipad.getCenter().getY()),
            HELI_COLOR,
            HELI_RADIUS,
            START_FUEL);
    }

    private void createPond() {
        pond = new Pond(new Point2D(
            r.nextInt(((GAME_WIDTH - POND_SIZE) - 
            POND_SIZE) + 1) +
            POND_SIZE, 
            r.nextInt(((GAME_HEIGHT - POND_SIZE) - 
            ((int)(helipad.getBoundsInParent().getMaxY() + POND_SIZE))) + 1) +
            (int)(helipad.getBoundsInParent().getMaxY() + POND_SIZE)),
            POND_SIZE);
    }

    private void createCloud() {
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
        if (e.getCode() == KeyCode.LEFT) { heli.left(); }
        if (e.getCode() == KeyCode.RIGHT) { heli.right(); }
        if (e.getCode() == KeyCode.UP) { heli.increaseSpeed(); }
        if (e.getCode() == KeyCode.DOWN) { heli.decreaseSpeed(); }
    }

    public void handleSeeding() {
        if (heli.getBoundsInParent().
            intersects(cloud.getBoundsInParent())) {
                cloud.update();   
        }
    }

    public void handleIgnition() { heli.engineStart(); }

    public void handleBoundBoxes() {
        heli.showBoundingBox();
        helipad.showBoundingBox();
        pond.showBoundingBox();
        cloud.showBoundingBox();
    }

    public void handleReset() {
        getChildren().clear();
        getTransforms().clear();
        init();
        startAnimation();
    }

    @Override
    public void update() {
        heli.moveHeli();
        heli.updateBoundingBox();
        pond.updateBoundingBox();   
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
            if (    e.getCode() == KeyCode.LEFT ||
                    e.getCode() == KeyCode.RIGHT ||
                    e.getCode() == KeyCode.UP ||
                    e.getCode() == KeyCode.DOWN) { root.handleMovement(e); }

            if (e.getCode() == KeyCode.I) { root.handleIgnition(); }
            if (e.getCode() == KeyCode.B) { root.handleBoundBoxes(); }
            if (e.getCode() == KeyCode.R) { root.handleReset(); }
            if (e.getCode() == KeyCode.SPACE) { root.handleSeeding(); }
        });
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}