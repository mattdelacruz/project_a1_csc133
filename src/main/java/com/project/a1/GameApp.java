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

abstract class GameObject extends Group {
    boolean isBoundOn = false;
    Rectangle bound;

    void showBoundingBox() {
        bound = new Rectangle(getBoundsInLocal().getMinX(),
                getBoundsInLocal().getMinY(),
                getBoundsInLocal().getWidth(),
                getBoundsInLocal().getHeight());

        bound.setFill(Color.TRANSPARENT);
        bound.setStroke(Color.WHITE);

        if (!isBoundOn) {
            getChildren().add(bound);
            isBoundOn = true;

        } else if (isBoundOn) {
            getChildren().remove(bound);
            isBoundOn = false;
        }
    }
}

interface Update {
    public void update();

}

class GameText extends GameObject {
    private static final Scale SCALE = new Scale(1, -1);
    private static final FontWeight FONT_WEIGHT = FontWeight.NORMAL;
    private static final int FONT_SIZE = 15;

    Label l = new Label();
    Color color;
    Point2D loc;

    GameText(String s, Point2D pos, Color c) {
        color = c;
        loc = pos;
        generateLabel(s, c);
    }

    private void generateLabel(String s, Color c) {
        l = new Label(s);
        l.setFont(Font.font("Arial", FONT_WEIGHT, FONT_SIZE));
        l.setTextFill(c);
        getTransforms().add(new Translate(loc.getX(), loc.getY()));
        getTransforms().add(SCALE);
        getChildren().add(l);
    }

    public void updateLabel(String s) {
        getChildren().remove(l);
        l = new Label(s);
        l.setFont(Font.font("Arial", FONT_WEIGHT, FONT_SIZE));
        l.setTextFill(color);
        getChildren().add(l);
    }
}

class Pond extends GameObject {

    private static final Color POND_COLOR = Color.BLUE;

    Circle circle;
    GameText pondLabel;

    Pond(Point2D s, double size) {
        circle = new Circle(s.getX(), s.getY(), size);
        circle.setFill(POND_COLOR);
        pondLabel = new GameText(Double.toString(size),
                new Point2D(
                        circle.getCenterX(),
                        circle.getCenterY()),
                Color.WHITE);
        getChildren().addAll(circle, pondLabel);

    }
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
        this.getChildren().addAll(rect, circle);

    }

    public Point2D getCenter() {
        return new Point2D(circle.getCenterX(), circle.getCenterY());
    }
}

class Helicopter extends GameObject {

    private static final int ROTATION_ANGLE = 15;
    private static final int HEADING_LENGTH = 50;
    private static final int FUEL_CONSUMPTION = -1;
    private static final int MAX_SPEED = 10;
    private static final int MIN_SPEED = -2;

    Circle circle;
    Line line;
    GameText fuelLabel;
    boolean engineStart = false;
    double speed = 0;
    int fuelValue = 0;

    Helicopter(Point2D s, Color c, double r, int startFuel) {
        circle = new Circle(s.getX(), s.getY(), r, c);
        line = new Line(circle.getCenterX(),
                circle.getCenterY(),
                circle.getCenterX(),
                circle.getCenterY() + circle.getRadius() + HEADING_LENGTH);
        fuelLabel = new GameText(Integer.toString(startFuel), new Point2D(
                circle.getCenterX() -
                        circle.getRadius(),
                circle.getCenterY() -
                        circle.getRadius() * 3),
                c);
        fuelValue = startFuel;
        line.setStroke(c);
        getChildren().addAll(circle, line, fuelLabel);
    }

    public void consumeFuel() {
        updateFuel(fuelValue += FUEL_CONSUMPTION);
    }

    private void updateFuel(int f) {
        fuelLabel.updateLabel(Integer.toString(f));
        getChildren().remove(fuelLabel);
        getChildren().add(fuelLabel);
    }

    public void Left() {
        getTransforms()
                .add(new Rotate(
                        ROTATION_ANGLE,
                        circle.getBoundsInLocal().getCenterX(),
                        circle.getBoundsInLocal().getCenterY()));
    }

    public void Right() {
        getTransforms()
                .add(new Rotate(
                        -ROTATION_ANGLE,
                        circle.getBoundsInLocal().getCenterX(),
                        circle.getBoundsInLocal().getCenterY()));
    }

    public void moveHeli() {
        if (engineStart)
            getTransforms().add(new Translate(0, speed));
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
        if (engineStart && speed <= MAX_SPEED) {
            speed += 0.1;
        } else
            return;
    }

    public void decreaseSpeed() {
        if (engineStart && speed >= MIN_SPEED) {
            speed -= 0.1;
        } else
            return;
    }

    public double getSpeed() {
        return speed;
    }

}

class Game extends Pane {
    public static final int GAME_WIDTH = 400;
    public static final int GAME_HEIGHT = 800;

    public static final int HELI_RADIUS = GAME_WIDTH / 30;
    public static final Color HELI_COLOR = Color.YELLOW;
    private static final int START_FUEL = 250000;

    private static final int HELIPAD_SIZE = GAME_WIDTH / 5;
    public static final Color HELIPAD_COLOR = Color.GRAY;

    private static final Scale SCALE = new Scale(1, -1);

    Random r = new Random();
    Helicopter heli;
    Helipad helipad;
    Pond pond;

    Game() {
        SCALE.setPivotY(GAME_HEIGHT / 2);

        helipad = new Helipad(new Point2D(
                GAME_WIDTH / 2 - HELIPAD_SIZE / 2,
                GAME_HEIGHT / 10),
                HELIPAD_COLOR,
                HELIPAD_SIZE,
                HELIPAD_SIZE);

        heli = new Helicopter(new Point2D(
                helipad.getCenter().getX(),
                helipad.getCenter().getY()),
                HELI_COLOR,
                HELI_RADIUS,
                START_FUEL);
        // rand.nextInt((max - min) + 1) + min;

        Point2D pondLoc = new Point2D(r.nextInt(GAME_WIDTH),
                r.nextInt((GAME_HEIGHT - 2 * GAME_HEIGHT / 3) + 1) + 2 * GAME_HEIGHT / 3);
        pond = new Pond(pondLoc, 50);

        setStyle("-fx-background-color: black;");
        getTransforms().add(SCALE);
        getChildren().addAll(helipad, heli, pond);

        startAnimation();

    }

    public void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {

            @Override
            public void handle(long now) {
                heli.moveHeli();
                heli.consumeFuel();
            }
        };
        loop.start();
    }

    public void handleMovement(KeyEvent e) {
        if (e.getCode() == KeyCode.LEFT) {
            heli.Left();
        }

        if (e.getCode() == KeyCode.RIGHT) {
            heli.Right();
        }

        if (e.getCode() == KeyCode.UP) {
            heli.increaseSpeed();
        }

        if (e.getCode() == KeyCode.DOWN) {
            heli.decreaseSpeed();
        }
    }

    public void handleIgnition() {
        heli.engineStart();
    }

    public void handleBoundBoxes() {
        heli.showBoundingBox();
        helipad.showBoundingBox();
    }

    public void handleReset() {

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
        });
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}