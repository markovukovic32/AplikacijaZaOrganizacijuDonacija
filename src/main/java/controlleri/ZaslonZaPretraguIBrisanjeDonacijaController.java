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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import tornadofx.control.DateTimePicker;
import util.Datoteke;
import util.FXUtil;
import util.PromjenaPodataka;


public class ZaslonZaPretraguIBrisanjeDonacijaController{
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

    public void initialize(){
        datumDatePicker.setDateTimeValue(null);

        try {
            donorChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveDonoreIzBazePodataka()));
            primateljDonacijeChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePrimateljeDonacijaIzBazePodataka()));
            predmetDoniranjaChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePredmeteDoniranjaIzBazePodataka()));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }

        postaviVrijednostiCelija();

        urediCelije();

        inicijalizirajTablicu();
    }

    public void filtriraj(){
        Optional<PredmetDoniranja> predmetDoniranja = Optional.ofNullable(predmetDoniranjaChoiceBox.getValue());
        Optional<Donor> donor = Optional.ofNullable(donorChoiceBox.getValue());
        Optional<PrimateljDonacije> primateljDonacije = Optional.ofNullable(primateljDonacijeChoiceBox.getValue());
        Optional<LocalDateTime> datumIVrijeme = Optional.ofNullable(datumDatePicker.getDateTimeValue());

        Donacija.DonacijaBuilder donacijaBuilder = new Donacija.DonacijaBuilder();

        predmetDoniranja.ifPresent(donacijaBuilder::setPredmetDoniranja);
        donor.ifPresent(donacijaBuilder::setDonor);
        primateljDonacije.ifPresent(donacijaBuilder::setPrimateljDonacije);
        datumIVrijeme.ifPresent(donacijaBuilder::setVrijemeDonacije);
        Donacija donacija = donacijaBuilder.createDonacija();

        try {
            donacijaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiDonacijePremaKriterijima(donacija)));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
    }
    public void obrisiDonaciju(){
        Optional<Donacija> donacija = Optional.ofNullable(donacijaTableView.getSelectionModel().getSelectedItem());
        if(donacija.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Brisanje donacije", "Pogreška prilikom brisanja donacije", "Potrebno je odabrati donaciju koju želite obrisati.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda brisanja donacije", "Potvrdite želite li obrisati podatke o odabranoj donaciji.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            obrisiDonacijuThread(donacija.get());
        }
    }

    private void obrisiDonacijuThread(Donacija donacija) {
        Thread thread = new Thread(() -> Platform.runLater(() -> {
            try {
                BazaPodataka.obrisiDonacijuIzBazePodataka(donacija);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if (trenutniKorisnik instanceof Korisnik) {
                PromjenaPodataka<Donacija, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Donacija", donacija, null, (Korisnik) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            } else {
                PromjenaPodataka<Donacija, Admin> promjenaPodataka = new PromjenaPodataka<>("Donacija", donacija, null, (Admin) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Brisanje donacije", "Uspješno brisanje donacije", "Uspješno ste izbrisali podatke o donaciji.");
            inicijalizirajTablicu();
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void inicijalizirajTablicu(){
        Thread thread = new Thread(()-> Platform.runLater(()-> {
            try {
                donacijaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveDonacijeIzBazePodataka()));
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }

    private void postaviVrijednostiCelija() {
        donorColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getDonor().getImeIliImeTvrtke() + " " + s.getValue().getDonor().getPrezimeIliOIBTvrtke()));
        primateljDonacijeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getPrimateljDonacije().getImeIliImeTvrtke() + " " + s.getValue().getPrimateljDonacije().getPrezimeIliOIBTvrtke()));
        predmetDoniranjaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getPredmetDoniranja().getOpisPredmeta()));
        vrijemeColumn.setCellValueFactory(cellData-> new SimpleObjectProperty<>(cellData.getValue().getVrijemeDonacije().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
    }
    private void urediCelije() {
        donorColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        primateljDonacijeColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        predmetDoniranjaColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        vrijemeColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
    }
}
