module com.project.a1 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.project.a1 to javafx.fxml;
    exports com.project.a1;
}
