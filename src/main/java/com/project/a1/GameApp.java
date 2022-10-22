package com.project.a1;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.io.IOException;

abstract class GameObject extends Group {

    void showBoundingBox() {

        Rectangle r = new Rectangle(this.getBoundsInLocal().getMinX(),
                this.getBoundsInLocal().getMinY(),
                this.getBoundsInLocal().getWidth(),
                this.getBoundsInLocal().getHeight());
        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.WHITE);

        getChildren().add(r);

    }

}

interface Update {
    public void update();

}

class Helicopter extends GameObject implements Update {

    Helicopter(Point2D s, Color c, double r) {

        Circle circle = new Circle(s.getX(), s.getY(), r, c);
        Line line = new Line(s.getX() / 2, s.getY());
        line.setStroke(Color.WHITE);
        line.setStrokeWidth(10);
        this.getChildren().addAll(circle, line);
    }

    @Override
    public void update() {

    }

}

class Game extends Pane {
    public static final int GAME_WIDTH = 800;
    public static final int GAME_HEIGHT = 800;

    public static final int HELI_RADIUS = 50;
    public static final Color HELI_COLOR = Color.YELLOW;

    public static final Rectangle BACKGROUND_COLOR = new Rectangle(GAME_WIDTH, GAME_HEIGHT, Color.BLACK);

    Game() {

        Scale t = new Scale(1, -1);
        // t.setPivotY(GAME_HEIGHT / 2);

        t.pivotYProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> this.getBoundsInLocal().getMinY() + this.getBoundsInLocal().getHeight()
                                / 2,
                        this.boundsInLocalProperty()));

        this.getTransforms().add(t);

        Helicopter heli = new Helicopter(new Point2D(100, 700),
                HELI_COLOR, HELI_RADIUS);

        this.getChildren().addAll(BACKGROUND_COLOR, heli);
        heli.showBoundingBox();

    }

}

public class GameApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        Game root = new Game();
        scene = new Scene(root, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        stage.setScene(scene);

        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}