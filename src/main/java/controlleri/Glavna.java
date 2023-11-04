package controlleri;


import hr.java.vjezbe.iznimke.UcitavanjeFxmlaException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FXUtil;
import util.ZasloniAplikacije;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class Glavna extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Glavna.class);
    @Getter
    private static Stage mainStage;
    @Override
    public void start(Stage stage){
        mainStage = stage;
        try{
            Glavna.pokaziZaslon(ZasloniAplikacije.ZASLON_LOGIN);
        }
        catch (UcitavanjeFxmlaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Učitavanje zaslona", "Pogreška prilikom učitavanja fxml datoteke", "Neuspješno učitavanje fxml datoteke.");
        }
    }
    public static void main(String[] args){
        launch();
    }

    public static void pokaziZaslon(ZasloniAplikacije zaslon) throws UcitavanjeFxmlaException {
        try {
            var window = (Parent) FXMLLoader.load(Objects.requireNonNull(Glavna.class.getResource(zaslon.getFxml())));
            if(Optional.ofNullable(mainStage.getScene()).isEmpty()){
                Scene scene = new Scene(window);
                scene.getStylesheets().add("main.css");
                mainStage.setScene(scene);
            }
            else {
                mainStage.getScene().setRoot(window);
            }
            mainStage.setTitle(zaslon.getNaslov());
            mainStage.show();
        } catch (IOException e) {
            String poruka = "Greška prilikom učitavanja zaslona " + zaslon;
            logger.error(poruka);
            throw new UcitavanjeFxmlaException(poruka, e.getCause());
        }
    }
}