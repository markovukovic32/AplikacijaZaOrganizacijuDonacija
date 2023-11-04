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

public class ZaslonZaPretraguIBrisanjePrimateljaDonacijaController{
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
    ChoiceBox<PredmetDoniranja> primljeniPredmetiChoiceBox;
    @FXML
    ChoiceBox<PredmetDoniranja> trazeniPredmetiChoiceBox;
    @FXML
    TableView<PrimateljDonacije> primateljDonacijeTableView;
    @FXML
    TableColumn<PrimateljDonacije, String> imeIliImeTvrtkeColumn;
    @FXML
    TableColumn<PrimateljDonacije, String> prezimeIliOIBColumn;
    @FXML
    TableColumn<PrimateljDonacije,String> opisColumn;
    @FXML
    TableColumn<PrimateljDonacije,String> gradColumn;
    @FXML
    TableColumn<PrimateljDonacije,String> primljeniPredmetiColumn;
    @FXML
    TableColumn<PrimateljDonacije,String> trazeniPredmetiColumn;
    @FXML
    Label imeIliImeTvrtkeLabel;
    @FXML
    Label prezimeIliOIBTvrtkeLabel;

    public void initialize(){
        try {
            ObservableList<PredmetDoniranja> listaPredmeta = FXCollections.observableArrayList(BazaPodataka.dohvatiSvePredmeteDoniranjaIzBazePodataka());
            primljeniPredmetiChoiceBox.setItems(listaPredmeta);
            trazeniPredmetiChoiceBox.setItems(listaPredmeta);
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        lokacijaChoiceBox.setItems(FXCollections.observableArrayList(Lokacija.values()));
        postaviVrijednostiCelija();

        urediCelije();
        inicijalizirajTablicuThread();
    }

    public void filtriraj(){
        String imeIliImeTvrtke = imeTextField.getText();
        String prezimeIliOIBTvrtke = prezimeTextField.getText();
        Optional<String> opis = fizickaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("fizička osoba")
                : pravnaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("pravna osoba") : Optional.empty();
        Optional<Lokacija> grad = Optional.ofNullable(lokacijaChoiceBox.getValue());
        Optional<PredmetDoniranja> primljeniPredmet = Optional.ofNullable(primljeniPredmetiChoiceBox.getValue());
        Optional<PredmetDoniranja> trazeniPredmet = Optional.ofNullable(trazeniPredmetiChoiceBox.getValue());
        if(opis.isPresent()){
            if(opis.get().equalsIgnoreCase("fizička osoba")){
                postaviColumnNasloveFizickaOsoba();
            }
            else{
                postaviColumnNaslovePravnaOsoba();
            }
        }

        PrimateljDonacije.PrimateljDonacijeBuilder primateljDonacijeBuilder = new PrimateljDonacije.PrimateljDonacijeBuilder();
        if(!imeIliImeTvrtke.isBlank()){
            primateljDonacijeBuilder.setImeIliImeTvrtke(imeIliImeTvrtke);
        }
        if(!prezimeIliOIBTvrtke.isBlank()){
            primateljDonacijeBuilder.setPrezimeIliOIBTvrtke(prezimeIliOIBTvrtke);
        }
        opis.ifPresent(primateljDonacijeBuilder::setOpisPrimatelja);
        grad.ifPresent(primateljDonacijeBuilder::setLokacija);
        primljeniPredmet.ifPresent(p -> primateljDonacijeBuilder.setListaPrimljenihPredmeta(new PredmetiDoniranja<>(Collections.singletonList(p))));
        trazeniPredmet.ifPresent(p -> primateljDonacijeBuilder.setListaPotrebnihPredmeta(new PredmetiDoniranja<>(Collections.singletonList(p))));
        PrimateljDonacije primateljDonacije = primateljDonacijeBuilder.createPrimateljDonacije();
        try {
            primateljDonacijeTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiPrimateljeDonacijaPremaKriterijima(primateljDonacije)));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
    }
    public void obrisiPrimatelja(){
        Optional<PrimateljDonacije> primateljDonacije = Optional.ofNullable(primateljDonacijeTableView.getSelectionModel().getSelectedItem());
        if(primateljDonacije.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Brisanje primatelja donacije", "Greška prilikom brisanja primatelja donacije", "Potrebno je odabrati primatelja donacije kojeg želite obrisati.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda brisanja primatelja donacije", "Potvrdite želite li obrisati podatke o odabranom primatelju donacije.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            obrisiPrimateljaDonacijeThread(primateljDonacije.get());
        }
    }

    private void obrisiPrimateljaDonacijeThread(PrimateljDonacije primateljDonacije) {
        Thread thread = new Thread(()-> Platform.runLater(()-> {
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if (trenutniKorisnik instanceof Korisnik) {
                PromjenaPodataka<PrimateljDonacije, Admin> promjenaPodataka = new PromjenaPodataka<>("Primatelj donacije", primateljDonacije, null, (Admin) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            } else {
                PromjenaPodataka<PrimateljDonacije, Admin> promjenaPodataka = new PromjenaPodataka<>("Primatelj donacije", primateljDonacije, null, (Admin) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            try{
                BazaPodataka.obrisiPrimateljaDonacijeIzBazePodataka(primateljDonacije);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Brisanje primatelja donacije", "Uspješno brisanje primatelja donacije", "Uspješno ste obrisali podatke o primatelju donacije.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }

    private void inicijalizirajTablicuThread() {
        Thread thread = new Thread(()-> Platform.runLater(()-> {
            try {
                primateljDonacijeTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePrimateljeDonacijaIzBazePodataka()));
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void postaviVrijednostiCelija() {
        imeIliImeTvrtkeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getImeIliImeTvrtke()));
        prezimeIliOIBColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getPrezimeIliOIBTvrtke()));
        opisColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOpisPrimatelja()));
        gradColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getLokacija().getImeGrada()));
        primljeniPredmetiColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getListaPrimljenihPredmeta().toString()));
        trazeniPredmetiColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getListaPotrebnihPredmeta().toString()));
    }
    private void urediCelije() {
        imeIliImeTvrtkeColumn.setCellFactory(tc -> createWrappingCell());
        prezimeIliOIBColumn.setCellFactory(tc -> createWrappingCell());
        opisColumn.setCellFactory(tc -> createWrappingCell());
        gradColumn.setCellFactory(tc -> createWrappingCell());
        primljeniPredmetiColumn.setCellFactory(tc -> createWrappingCell());
        trazeniPredmetiColumn.setCellFactory(tc -> createWrappingCell());
    }
    @FXML
    public void postaviLabelePravnaOsoba(){
        Thread thread = new Thread(()->{
            Platform.runLater(()->{
                imeIliImeTvrtkeLabel.setText("Ime tvrtke:");
                prezimeIliOIBTvrtkeLabel.setText("OIB tvrtke:");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    public void postaviLabeleFizickaOsoba(){
        Thread thread = new Thread(()->{
            Platform.runLater(()-> {
                imeIliImeTvrtkeLabel.setText("Ime: ");
                prezimeIliOIBTvrtkeLabel.setText("Prezime: ");
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
                prezimeIliOIBColumn.setText("OIB tvrtke:");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
    public void postaviColumnNasloveFizickaOsoba(){
        Thread thread = new Thread(()->{
            Platform.runLater(()-> {
                imeIliImeTvrtkeColumn.setText("Ime: ");
                prezimeIliOIBColumn.setText("Prezime: ");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
}
