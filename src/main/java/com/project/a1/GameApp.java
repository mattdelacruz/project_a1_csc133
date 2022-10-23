package com.project.a1;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
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
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.IOException;

abstract class GameObject extends Group {
    boolean isBoundOn = false;
    Rectangle bound = new Rectangle(this.getBoundsInLocal().getMinX(),
            this.getBoundsInLocal().getMinY(),
            this.getBoundsInLocal().getWidth(),
            this.getBoundsInLocal().getHeight());

    void showBoundingBox() {
        bound.setFill(Color.TRANSPARENT);
        bound.setStroke(Color.WHITE);

        if (!isBoundOn) {
            isBoundOn = true;
            getChildren().add(bound);

        } else if (isBoundOn) {
            isBoundOn = false;
            bound.setStroke(Color.TRANSPARENT);
            getChildren().remove(bound);
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

    GameText(String s, Color c) {
        getTransforms().add(SCALE);
        generateLabel(s, c);
    }

    private void generateLabel(String s, Color c) {
        Label l = new Label(s);
        l.setTextFill(c);
        l.setFont(Font.font("Arial", FONT_WEIGHT, FONT_SIZE));
        SCALE.setPivotY(l.getHeight() / 2);
        l.getTransforms().add(SCALE);

    }

    public Label getLabel() {
        return l;
    }
}

class Helipad extends GameObject {

    private static final int GAP = 5;

    Rectangle rect = new Rectangle();
    Circle circle = new Circle();

    Helipad(Point2D s, Color c, int w, int h) {
        rect = new Rectangle(s.getX(), s.getY(), 100, 100);
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

    Circle circle = new Circle();
    Line line = new Line();
    GameText fuel = new GameText("", null);
    boolean engineStart = false;
    double speed = 0;

    Helicopter(Point2D s, Color c, double r) {
        circle = new Circle(s.getX(), s.getY(), r, c);
        line = new Line(circle.getCenterX(),
                circle.getCenterY(),
                circle.getCenterX(),
                circle.getCenterY() + circle.getRadius() + HEADING_LENGTH);
        fuel = new GameText("hello", c);
        fuel.getTransforms().add(new Translate(s.getX(), s.getY() - 50));
        line.setStroke(c);
        this.getChildren().addAll(circle, line, fuel.getLabel());
    }

    public void Left() {
        this.getTransforms()
                .add(new Rotate(
                        ROTATION_ANGLE,
                        circle.getBoundsInLocal().getCenterX(),
                        circle.getBoundsInLocal().getCenterY()));
    }

    public void Right() {
        this.getTransforms()
                .add(new Rotate(
                        -ROTATION_ANGLE,
                        circle.getBoundsInLocal().getCenterX(),
                        circle.getBoundsInLocal().getCenterY()));
    }

    public void increaseSpeed() {
        if (engineStart) {
            speed += 0.1;
        } else
            return;
    }

    public void moveHeli() {
        if (engineStart)
            this.getTransforms().add(new Translate(0, speed));
        else
            return;
    }

    public void engineStart() {
        if (!engineStart)
            engineStart = true;
        else
            engineStart = false;
    }

    public void decreaseSpeed() {
        if (engineStart) {
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

    public static final int HELI_RADIUS = GAME_WIDTH / 25;
    public static final Color HELI_COLOR = Color.YELLOW;

    private static final int HELIPAD_WIDTH = GAME_WIDTH / 5;
    private static final int HELIPAD_HEIGHT = GAME_HEIGHT / 5;
    public static final Color HELIPAD_COLOR = Color.GRAY;

    private static final Scale SCALE = new Scale(1, -1);

    Helicopter heli = new Helicopter(new Point2D(0, 0),
            HELI_COLOR, HELI_RADIUS);
    Helipad helipad = new Helipad(new Point2D(0, 0),
            HELIPAD_COLOR, 0, 0);

    Game() {
        SCALE.setPivotY(GAME_HEIGHT / 2);

        helipad = new Helipad(new Point2D(
                GAME_WIDTH / 2 - HELIPAD_WIDTH / 2,
                GAME_HEIGHT / 10),
                HELIPAD_COLOR,
                HELIPAD_WIDTH,
                HELIPAD_HEIGHT);

        heli = new Helicopter(new Point2D(
                helipad.getCenter().getX(),
                helipad.getCenter().getY()),
                HELI_COLOR,
                HELI_RADIUS);

        // SCALE.pivotYProperty()
        // .bind(Bindings.createDoubleBinding(
        // () -> this.getBoundsInLocal().getMinY() +
        // this.getBoundsInLocal().getHeight() / 2,
        // this.boundsInLocalProperty()));
        this.setStyle("-fx-background-color: black;");
        this.getTransforms().add(SCALE);
        this.getChildren().addAll(helipad, heli);

        this.setBackground(null);

        startAnimation();

    }

    public void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {

            @Override
            public void handle(long now) {
                heli.moveHeli();
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