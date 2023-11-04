package controlleri;

import hr.java.vjezbe.iznimke.NeispravnaLozinkaException;
import hr.java.vjezbe.iznimke.NeispravnoKorisnickoImeException;
import hr.java.vjezbe.iznimke.NeuspjesnaPrijavaException;
import hr.java.vjezbe.iznimke.UcitavanjeFxmlaException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.FXUtil;
import util.KorisnikManager;
import util.ZasloniAplikacije;

import java.util.Optional;

public class LoginController {
    @FXML
    TextField korisnickoImeTextField;
    @FXML
    PasswordField lozinkaPasswordField;
    @FXML
    ChoiceBox<String> roleChoiceBox;
    @FXML
    Button prijaviSeButton;

    public void initialize(){
        Glavna.getMainStage().setTitle("Prijava");
        roleChoiceBox.setItems(FXCollections.observableArrayList("admin", "korisnik"));
    }
    public void prijaviSe(){
        String korisnickoIme = korisnickoImeTextField.getText();
        String lozinka = lozinkaPasswordField.getText();
        Optional<String> korisnickaRola = Optional.ofNullable(roleChoiceBox.getValue());
        try {
            KorisnikManager.provjeriPodatkeZaPrijavu(korisnickoIme, lozinka, korisnickaRola);
        } catch (NeispravnoKorisnickoImeException | NeuspjesnaPrijavaException | NeispravnaLozinkaException e) {
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Prijava", "Neuspješna prijava", e.getMessage());
            return;
        }
        try{
            Glavna.pokaziZaslon(ZasloniAplikacije.ZASLON_ADMINA);
        }
        catch (UcitavanjeFxmlaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Učitavanje zaslona", "Pogreška prilikom učitavanja fxml datoteke", "Neuspješno učitavanje fxml datoteke.");
        }
    }
}