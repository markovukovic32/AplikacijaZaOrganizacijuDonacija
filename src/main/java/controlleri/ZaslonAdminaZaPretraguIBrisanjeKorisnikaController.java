package controlleri;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.Datoteke;
import util.FXUtil;
import util.PromjenaPodataka;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static util.FXUtil.createWrappingCell;

public class ZaslonAdminaZaPretraguIBrisanjeKorisnikaController{
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
    private final ObservableList<Lokacija> listaLokacija = FXCollections.observableArrayList(Lokacija.values());
    public void initialize(){
        lokacijaChoiceBox.setItems(listaLokacija);
        postaviVrijednostiCelija();
        urediCelije();
        inicijalizirajTablicuThread();
    }

    public void filtriraj() {
        Optional<String> korisnickoIme = Optional.ofNullable(korisnickoImeTextField.getText());
        Optional<String> ime = Optional.ofNullable(imeTextField.getText());
        Optional<String> prezime = Optional.ofNullable(prezimeTextField.getText());
        Optional<String> email = Optional.ofNullable(emailTextField.getText());
        Optional<Lokacija> lokacija = Optional.ofNullable(lokacijaChoiceBox.getValue());

        Osoba.OsobaBuilder osobaBuilder = new Osoba.OsobaBuilder();
        ime.ifPresent(osobaBuilder::setIme);
        prezime.ifPresent(osobaBuilder::setPrezime);

        Korisnik.KorisnikBuilder korisnikBuilder = new Korisnik.KorisnikBuilder();
        korisnickoIme.ifPresent(korisnikBuilder::setKorisnickoIme);
        korisnikBuilder.setOsoba(osobaBuilder.createOsoba());
        email.ifPresent(korisnikBuilder::setEmail);
        lokacija.ifPresent(korisnikBuilder::setLokacija);
        Korisnik korisnik = korisnikBuilder.createKorisnik();
        try {
            korisnikTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiKorisnikePremaKriterijima(korisnik)));
        } catch (BazaPodatakaException e) {
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
    }
    public void obrisiKorisnikaThread(){
        Optional<Korisnik> korisnik = Optional.ofNullable(korisnikTableView.getSelectionModel().getSelectedItem());
        if(korisnik.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Brisanje korisnika", "Pogreška prilikom brisanja korisnika", "Morate odabrati korisnika kojeg želite obrisati.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda brisanja korisnika", "Potvrdite želite li obrisati podatke o odabranom korisniku.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            obrisiKorisnikaThread(korisnik.get());
        }
    }

    private void obrisiKorisnikaThread(Korisnik korisnik) {
        Thread thread = new Thread(() -> Platform.runLater(() -> {
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            PromjenaPodataka<Korisnik, Admin> promjenaPodataka = new PromjenaPodataka<>("Korisnik", korisnik, null, (Admin) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            Datoteke.serijalizirajPromjenu(promjenaPodataka);
            Datoteke.obrisiKorisnikaIzDatoteke(korisnik);
            try {
                BazaPodataka.obrisiKorisnikaIzBazePodataka(korisnik);
            }
            catch (BazaPodatakaException e) {
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Brisanje korisnika", "Uspješno brisanje korisnika", "Proces brisanja korisnika je uspješno obavljen.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void inicijalizirajTablicuThread(){
        Thread thread = new Thread(() -> Platform.runLater(() -> {
            try {
                korisnikTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveKorisnikeIzBazePodataka()));
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }

    private void postaviVrijednostiCelija() {
        korisnickoImeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getKorisnickoIme()));
        imeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOsoba().getIme()));
        prezimeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOsoba().getPrezime()));
        emailColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getEmail()));
        lokacijaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getLokacija().getImeGrada()));
    }
    private void urediCelije() {
        korisnickoImeColumn.setCellFactory(tc -> createWrappingCell());
        imeColumn.setCellFactory(tc -> createWrappingCell());
        prezimeColumn.setCellFactory(tc -> createWrappingCell());
        emailColumn.setCellFactory(tc -> createWrappingCell());
        lokacijaColumn.setCellFactory(tc -> createWrappingCell());
    }

}
