package controlleri;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import hr.java.vjezbe.iznimke.NeispravnoKorisnickoImeException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ZaslonAdminaZaIzmjenuKorisnikaController {
    private static final Logger logger = LoggerFactory.getLogger(ZaslonAdminaZaIzmjenuKorisnikaController.class);

    @FXML
    TextField korisnickoImeTextField;
    @FXML
    TextField imeTextField;
    @FXML
    TextField prezimeTextField;
    @FXML
    TextField emailTextField;
    @FXML
    ChoiceBox<Lokacija> lokacijaChoiceBox;
    @FXML
    TableView<Korisnik> korisnikTableView;
    @FXML
    TableColumn<Korisnik,String> korisnickoImeColumn;
    @FXML
    TableColumn<Korisnik, String> imeColumn;
    @FXML
    TableColumn<Korisnik, String> prezimeColumn;
    @FXML
    TableColumn<Korisnik, String> emailColumn;
    @FXML
    TableColumn<Korisnik, String> lokacijaColumn;
    private Korisnik korisnik;
    public void initialize(){
        lokacijaChoiceBox.setItems(FXCollections.observableArrayList(Lokacija.values()));

        postaviVrijednostiCelija();

        urediCelije();

        inicijalizirajTablicuThread();


        korisnikTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(Optional.ofNullable(korisnikTableView.getSelectionModel().getSelectedItem()).isPresent()){
                korisnik = korisnikTableView.getSelectionModel().getSelectedItem();
                korisnickoImeTextField.setText(korisnik.getKorisnickoIme());
                imeTextField.setText(korisnik.getOsoba().getIme());
                prezimeTextField.setText(korisnik.getOsoba().getPrezime());
                emailTextField.setText(korisnik.getEmail());
                lokacijaChoiceBox.setValue(korisnik.getLokacija());
            }
        });
    }

    public void izmjeniKorisnika(){
        if(Optional.ofNullable(korisnik).isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena korisnika", "Pogreška prilikom izmjene korisnika", "Potrebno je odabrati korisnika kojeg želite izmjeniti.");
            return;
        }
        String korisnickoIme = korisnickoImeTextField.getText();
        String ime = imeTextField.getText();
        String prezime = prezimeTextField.getText();
        String email = emailTextField.getText();
        Optional<Lokacija> lokacija = Optional.ofNullable(lokacijaChoiceBox.getValue());

        Korisnik.KorisnikBuilder korisnikBuilder = new Korisnik.KorisnikBuilder();

        if(!korisnickoIme.isBlank()) {
            try {
                if (!KorisnikManager.provjeriKorisnickoIme(korisnickoIme))
                    return;
                if (BazaPodataka.dohvatiSveKorisnikeIzBazePodataka().stream().anyMatch(s->s.getKorisnickoIme().equalsIgnoreCase(korisnickoIme))) {
                    FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena korisnika", "Pogreška prilikom izmjene korisnika", "Korisničko ime je već zauzeto.");
                    return;
                } else
                    korisnikBuilder.setKorisnickoIme(korisnickoIme);
            }
            catch (NeispravnoKorisnickoImeException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena korisnika", "Pogreška prilikom izmjene korisnika", "Korisničko ime mora sadržavati najmanje 4 znakova.");
            }
        }
        else
            korisnikBuilder.setKorisnickoIme(korisnik.getKorisnickoIme());

        Osoba.OsobaBuilder osobaBuilder = new Osoba.OsobaBuilder();

        if(ime.isBlank()){
            osobaBuilder.setIme(korisnik.getOsoba().getIme());
        }
        else{
            if(ValidatorUnosa.validatorUnosa(ime)){
                osobaBuilder.setIme(ime);
            }
            else{
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena korisnika", "Pogreška prilikom izmjene korisnika", "Potrebno je upisati samo slova hrvatske abecede.");
                return;
            }
        }

        if(prezime.isBlank()){
            osobaBuilder.setPrezime(korisnik.getOsoba().getPrezime());
        }
        else{
            if(ValidatorUnosa.validatorUnosa(prezime)){
                osobaBuilder.setPrezime(prezime);
            }
            else{
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena korisnika", "Pogreška prilikom izmjene korisnika", "Potrebno je upisati samo slova hrvatske abecede.");
                return;
            }
        }

        if(!email.isBlank()){
            try{
                if (!KorisnikManager.provjeriEmail(email))
                    return;
                if(BazaPodataka.dohvatiKorisnikePremaKriterijima(new Korisnik.KorisnikBuilder().setEmail(email).createKorisnik()).stream().findFirst().isPresent()){
                    FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena korisnika", "Pogreška prilikom izmjene korisnika", "Uneseni email je već zauzet.");
                    return;
                }
                korisnikBuilder.setEmail(email);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
        }
        else
            korisnikBuilder.setEmail(korisnik.getEmail());


        lokacija.ifPresentOrElse(korisnikBuilder::setLokacija, ()->korisnikBuilder.setLokacija(korisnik.getLokacija()));
        korisnikBuilder.setHashLozinka(korisnik.getHashLozinka());
        korisnikBuilder.setOsoba(osobaBuilder.createOsoba());
        korisnikBuilder.setId(korisnik.getId());

        Korisnik noviKorisnik = korisnikBuilder.createKorisnik();

        if(korisnik.equals(noviKorisnik)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena korisnika", "Pogreška prilikom izmjene korisnika.", "Ne možete ostaviti iste podatke.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda izmjene korisnika", "Potvrdite želite li promijeniti podatke o odabranom korisniku.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            izmjeniKorisnikaThread(noviKorisnik);
        }
    }

    public void izmjeniKorisnikaThread(Korisnik noviKorisnik){
        Thread thread = new Thread(() -> Platform.runLater(() -> {
            try {
                BazaPodataka.izmjeniKorisnikaUBaziPodataka(noviKorisnik);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
            Admin trenutniKorisnik = BazaPodataka.dohvatiAdminaIzBazePodataka();
            PromjenaPodataka<Korisnik, Admin> promjenaPodataka = new PromjenaPodataka<>("Korisnik", korisnik, noviKorisnik, trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            Datoteke.serijalizirajPromjenu(promjenaPodataka);
            Datoteke.izmjeniKorisnikaIzDatoteke(korisnik, noviKorisnik);
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Izmjena korisnika", "Uspješna izmjena korisnika", "Uspješno ste promijenili podatke korisnika.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }

    public void inicijalizirajTablicuThread() {
        Thread thread = new Thread(()-> Platform.runLater(()->{
            try {
                korisnikTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveKorisnikeIzBazePodataka()));
                korisnikTableView.refresh();
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void urediCelije() {
        korisnickoImeColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        imeColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        prezimeColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        emailColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        lokacijaColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
    }

    private void postaviVrijednostiCelija() {
        korisnickoImeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getKorisnickoIme()));
        imeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOsoba().getIme()));
        prezimeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOsoba().getPrezime()));
        emailColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getEmail()));
        lokacijaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getLokacija().getImeGrada()));
    }
}
