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
import java.util.Collections;
import java.util.Optional;

import static util.FXUtil.createWrappingCell;

public class ZaslonZaPretraguIBrisanjeDonoraController{
    @FXML
    TextField imeTextField;
    @FXML
    TextField prezimeTextField;
    @FXML
    RadioButton fizickaOsobaRadioButton;
    @FXML
    RadioButton pravnaOsobaRadioButton;
    @FXML
    ChoiceBox<Lokacija> lokacijaChoiceBox;
    @FXML
    ChoiceBox<PredmetDoniranja> doniraniPredmetiChoiceBox;
    @FXML
    ChoiceBox<PredmetDoniranja> ponudjeniPredmetiChoiceBox;
    @FXML
    TableView<Donor> donorTableView;
    @FXML
    TableColumn<Donor,String> imeIliImeTvrtkeColumn;
    @FXML
    TableColumn<Donor,String> prezimeIlIOIBTvrtkeColumn;
    @FXML
    TableColumn<Donor,String> opisDonoraColumn;
    @FXML
    TableColumn<Donor,String> gradDonoraColumn;
    @FXML
    TableColumn<Donor,String> listaDoniranihPredmetaColumn;
    @FXML
    TableColumn<Donor,String> listaPonudjenihPredmetaColumn;
    @FXML
    Label imeIliImeTvrtke;
    @FXML
    Label prezimeIliOIBTvrtke;
    public void initialize(){
        try {
            ObservableList<PredmetDoniranja> listaPredmeta = FXCollections.observableArrayList(BazaPodataka.dohvatiSvePredmeteDoniranjaIzBazePodataka());
            doniraniPredmetiChoiceBox.setItems(listaPredmeta);
            ponudjeniPredmetiChoiceBox.setItems(listaPredmeta);
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        lokacijaChoiceBox.setItems(FXCollections.observableArrayList(Lokacija.values()));
        postaviVrijednostiCelija();
        urediCelije();
        inicijalizirajTablicuThread();
    }

    private void postaviVrijednostiCelija() {
        imeIliImeTvrtkeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getImeIliImeTvrtke()));
        prezimeIlIOIBTvrtkeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getPrezimeIliOIBTvrtke()));
        opisDonoraColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOpisDonora()));
        gradDonoraColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getLokacija().getImeGrada()));
        listaDoniranihPredmetaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getListaDoniranihPredmeta().toString()));
        listaPonudjenihPredmetaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getListaPonudjenihPredmeta().toString()));
    }

    private void urediCelije() {
        imeIliImeTvrtkeColumn.setCellFactory(tc -> createWrappingCell());
        opisDonoraColumn.setCellFactory(tc -> createWrappingCell());
        gradDonoraColumn.setCellFactory(tc -> createWrappingCell());
        listaDoniranihPredmetaColumn.setCellFactory(tc -> createWrappingCell());
        listaPonudjenihPredmetaColumn.setCellFactory(tc->createWrappingCell());
    }

    public void filtriraj(){
        String imeIliImeTvrtke = imeTextField.getText();
        String prezimeIliOIBTvrtke = prezimeTextField.getText();
        Optional<String> opis = fizickaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("fizička osoba")
                : pravnaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("pravna osoba") : Optional.empty();
        Optional<Lokacija> grad = Optional.ofNullable(lokacijaChoiceBox.getValue());
        Optional<PredmetDoniranja> doniraniPredmet = Optional.ofNullable(doniraniPredmetiChoiceBox.getValue());
        Optional<PredmetDoniranja> ponudjeniPredmet = Optional.ofNullable(ponudjeniPredmetiChoiceBox.getValue());
        if(opis.isPresent()){
            if(opis.get().equalsIgnoreCase("fizička osoba")){
                postaviColumnNasloveFizickaOsoba();
            }
            else {
                postaviColumnNaslovePravnaOsoba();
            }
        }
        Donor.DonorBuilder donorBuilder = new Donor.DonorBuilder();

        if(!imeIliImeTvrtke.isBlank()){
            donorBuilder.setImeIliImeTvrtke(imeIliImeTvrtke);
        }
        if(!prezimeIliOIBTvrtke.isBlank()){
            donorBuilder.setPrezimeIliOIBTvrtke(prezimeIliOIBTvrtke);
        }
        opis.ifPresent(donorBuilder::setOpisDonora);
        grad.ifPresent(donorBuilder::setLokacija);
        doniraniPredmet.ifPresent(p -> donorBuilder.setListaDoniranihPredmeta(new PredmetiDoniranja<>(Collections.singletonList(p))));
        ponudjeniPredmet.ifPresent(p -> donorBuilder.setListaPonudjenihPredmeta(new PredmetiDoniranja<>(Collections.singletonList(p))));
        Donor donor = donorBuilder.createDonor();
        try {
            donorTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiDonorePremaKriterijima(donor)));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
    }
    public void obrisiDonora() {
        Optional<Donor> donor = Optional.ofNullable(donorTableView.getSelectionModel().getSelectedItem());
        if(donor.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Brisanje donacije", "Pogreška prilikom brisanja donora", "Potrebno je odabrati donora kojeg želite obrisati.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda brisanja donora", "Potvrdite želite li obrisati podatke o odabranom donoru.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            obrisiDonoraThread(donor.get());
        }
    }

    private void obrisiDonoraThread(Donor donor) {
        Thread thread = new Thread(()-> Platform.runLater(()-> {
            try {
                BazaPodataka.obrisiDonoraIzBazePodataka(donor);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if (trenutniKorisnik instanceof Korisnik) {
                PromjenaPodataka<Donor, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Donor", donor, null, (Korisnik) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            } else {
                PromjenaPodataka<Donor, Admin> promjenaPodataka = new PromjenaPodataka<>("Donor", donor, null, (Admin) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Brisanje donora", "Uspješno brisanje donora", "Uspješno ste obrisali podatke o donoru.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void inicijalizirajTablicuThread() {
        Thread thread = new Thread(() -> Platform.runLater(() -> {
            try {
                donorTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveDonoreIzBazePodataka()));
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    public void postaviLabelePravnaOsoba(){
        Thread thread = new Thread(()->{
            Platform.runLater(()->{
                imeIliImeTvrtke.setText("Ime tvrtke:");
                prezimeIliOIBTvrtke.setText("OIB tvrtke:");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    public void postaviLabeleFizickaOsoba(){
        Thread thread = new Thread(()->{
            Platform.runLater(()-> {
                imeIliImeTvrtke.setText("Ime: ");
                prezimeIliOIBTvrtke.setText("Prezime: ");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    public void postaviColumnNaslovePravnaOsoba(){
        Thread thread = new Thread(()->{
            Platform.runLater(()->{
                imeIliImeTvrtkeColumn.setText("Ime tvrtke:");
                prezimeIlIOIBTvrtkeColumn.setText("OIB tvrtke:");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
    public void postaviColumnNasloveFizickaOsoba(){
        Thread thread = new Thread(()->{
            Platform.runLater(()-> {
                imeIliImeTvrtkeColumn.setText("Ime: ");
                prezimeIlIOIBTvrtkeColumn.setText("Prezime: ");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
}
