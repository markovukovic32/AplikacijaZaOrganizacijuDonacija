module controlleri {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.sql;
    requires tornadofx.controls;
    requires static lombok;


    opens controlleri to javafx.fxml;
    exports controlleri;
    exports hr.java.vjezbe.entitet;
    exports util;
    exports hr.java.vjezbe.iznimke;
}