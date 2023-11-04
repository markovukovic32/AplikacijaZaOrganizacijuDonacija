package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.text.Text;

import java.io.Serializable;

public class FXUtil {
    public static void pokaziAlert(Alert.AlertType alertType, String naslov, String header, String poruka) {
        Alert alert = new Alert(alertType);
        alert.setTitle(naslov);
        alert.setHeaderText(header);
        alert.setContentText(poruka);
        alert.show();
    }
    public static <T extends Serializable> TableCell<T, String> createWrappingCell() {
        TableCell<T, String> cell = new TableCell<>();
        Text text = new Text();
        cell.setGraphic(text);
        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(cell.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        return cell;
    }
    public static Alert dohvatiAlert(Alert.AlertType alertType, String naslov, String header, String poruka){
        Alert alert = new Alert(alertType);
        alert.setTitle(naslov);
        alert.setHeaderText(header);
        alert.setContentText(poruka);
        return alert;
    }
}
