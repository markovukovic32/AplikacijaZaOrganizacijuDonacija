package controlleri;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tornadofx.control.DateTimePicker;
import util.Datoteke;
import util.FXUtil;
import util.PromjenaPodataka;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static util.FXUtil.createWrappingCell;

public class ZaslonZaIzmjenuDonacijeController{
    @FXML
    ChoiceBox<PredmetDoniranja> predmetDoniranjaChoiceBox;
    @FXML
    ChoiceBox<Donor> donorChoiceBox;
    @FXML
    ChoiceBox<PrimateljDonacije> primateljDonacijeChoiceBox;
    @FXML
    DateTimePicker datumDatePicker;
    @FXML
    TableView<Donacija> donacijaTableView;
    @FXML
    TableColumn<Donacija, String> donorColumn;
    @FXML
    TableColumn<Donacija, String> primateljDonacijeColumn;
    @FXML
    TableColumn<Donacija, String> predmetDoniranjaColumn;
    @FXML
    TableColumn<Donacija, String> vrijemeColumn;
    AtomicBoolean donorBool = new AtomicBoolean(false);
    AtomicBoolean primateljBool = new AtomicBoolean(false);
    private Donacija donacija;

    public void initialize(){
        donorBool.set(false);
        primateljBool.set(false);
        datumDatePicker.setDateTimeValue(null);
        try {
            predmetDoniranjaChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiPredmeteZaDonaciju()));
            donorChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveDonoreIzBazePodataka()));
            primateljDonacijeChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePrimateljeDonacijaIzBazePodataka()));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
        }

        postaviVrijednostiCelija();

        urediCelije();

        inicijalizirajTablicuThread();

        predmetDoniranjaChoiceBox.setOnAction(event -> {
            PredmetDoniranja predmetDoniranja = predmetDoniranjaChoiceBox.getValue();
            Optional<PrimateljDonacije> primateljDonacije = Optional.ofNullable(BazaPodataka.dohvatiPrimateljaDonacijePoPredmetuDoniranja(predmetDoniranja));
            Optional<Donor> donor = Optional.ofNullable(BazaPodataka.dohvatiDonoraPoPredmetuDoniranja(predmetDoniranja));
            if(primateljDonacije.isPresent()){
                primateljBool.set(true);
                donorChoiceBox.setDisable(false);
                primateljDonacijeChoiceBox.setValue(primateljDonacije.get());
                primateljDonacijeChoiceBox.setDisable(true);
                donorBool.set(false);
            }
            else if(donor.isPresent()){
                donorBool.set(true);
                primateljDonacijeChoiceBox.setDisable(false);
                donorChoiceBox.setValue(donor.get());
                donorChoiceBox.setDisable(true);
                primateljBool.set(false);
            }
            else{
                primateljBool.set(false);
                donorChoiceBox.setDisable(false);
            }
        });

        donacijaTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(Optional.ofNullable(donacijaTableView.getSelectionModel().getSelectedItem()).isPresent()){
                donacija = donacijaTableView.getSelectionModel().getSelectedItem();
                predmetDoniranjaChoiceBox.setValue(donacija.getPredmetDoniranja());
                donorChoiceBox.setValue(donacija.getDonor());
                primateljDonacijeChoiceBox.setValue(donacija.getPrimateljDonacije());
                datumDatePicker.setDateTimeValue(donacija.getVrijemeDonacije());
            }
        });
    }

    private void urediCelije() {
        donorColumn.setCellFactory(tc -> createWrappingCell());
        primateljDonacijeColumn.setCellFactory(tc -> createWrappingCell());
        predmetDoniranjaColumn.setCellFactory(tc -> createWrappingCell());
        vrijemeColumn.setCellFactory(tc -> createWrappingCell());
    }

    private void postaviVrijednostiCelija() {
        donorColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getDonor().getImeIliImeTvrtke() + " " + s.getValue().getDonor().getPrezimeIliOIBTvrtke()));
        primateljDonacijeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getPrimateljDonacije().getImeIliImeTvrtke() + " " + s.getValue().getPrimateljDonacije().getPrezimeIliOIBTvrtke()));
        predmetDoniranjaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getPredmetDoniranja().getOpisPredmeta()));
        vrijemeColumn.setCellValueFactory(cellData-> new SimpleObjectProperty<>(cellData.getValue().getVrijemeDonacije().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
    }

    public void izmjeniDonaciju() {
        if(Optional.ofNullable(donacija).isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena donacije", "Pogreška prilikom promjene donacije", "Potrebno je odabrati donaciju koju želite izmjeniti.");
            return;
        }
        Optional<PredmetDoniranja> predmetDoniranja = Optional.ofNullable(predmetDoniranjaChoiceBox.getValue());
        Optional<Donor> donor = Optional.ofNullable(donorChoiceBox.getValue());
        Optional<PrimateljDonacije> primateljDonacije = Optional.ofNullable(primateljDonacijeChoiceBox.getValue());
        Optional<LocalDateTime> vrijemeDonacije = Optional.ofNullable(datumDatePicker.getDateTimeValue());

        Donacija.DonacijaBuilder donacijaBuilder = new Donacija.DonacijaBuilder();

        predmetDoniranja.ifPresentOrElse(donacijaBuilder::setPredmetDoniranja, ()->donacijaBuilder.setPredmetDoniranja(donacija.getPredmetDoniranja()));
        donor.ifPresentOrElse(donacijaBuilder::setDonor, ()->donacijaBuilder.setDonor(donacija.getDonor()));
        primateljDonacije.ifPresentOrElse(donacijaBuilder::setPrimateljDonacije, ()->donacijaBuilder.setPrimateljDonacije(donacija.getPrimateljDonacije()));
        vrijemeDonacije.ifPresentOrElse(donacijaBuilder::setVrijemeDonacije, ()->donacijaBuilder.setVrijemeDonacije(donacija.getVrijemeDonacije()));
        donacijaBuilder.setId(donacija.getId());

        Donacija novaDonacija = donacijaBuilder.createDonacija();

        if(donacija.equals(novaDonacija)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena donacije", "Pogreška prilikom promjene donacije", "Ne možete ostaviti iste podatke.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda izmjene donacije", "Potvrdite želite li promijeniti podatke o donaciji");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            izmjeniDonacijuThread(novaDonacija);
        }
    }

    private void izmjeniDonacijuThread(Donacija novaDonacija) {
        Thread thread = new Thread(() -> Platform.runLater(() -> {
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if (trenutniKorisnik instanceof Korisnik) {
                PromjenaPodataka<Donacija, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Donacija", donacija, novaDonacija, (Korisnik) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            } else {
                PromjenaPodataka<Donacija, Admin> promjenaPodataka = new PromjenaPodataka<>("Donacija", donacija, novaDonacija, (Admin) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            try {
                BazaPodataka.izmjeniDonacijuUBaziPodataka(novaDonacija);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Izmjena donacije", "Uspješna izmjena donacije", "Uspješno ste promijenili podatke o donaciji.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }

    private void inicijalizirajTablicuThread() {
        Thread thread = new Thread(()-> Platform.runLater(()->{
            try {
                donacijaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveDonacijeIzBazePodataka()));
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }
}