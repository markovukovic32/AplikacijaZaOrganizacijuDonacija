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
import util.ValidatorUnosa;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ZaslonZaIzmjenuPrimateljaDonacijeController {
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
    ListView<PredmetDoniranja> trazeniPredmetiListView;
    @FXML
    TableView<PrimateljDonacije> primateljDonacijeTableView;
    @FXML
    TableColumn<PrimateljDonacije, String> imeIliImeTvrtkeColumn;
    @FXML
    TableColumn<PrimateljDonacije, String> prezimeIliOIBTvrtkeColumn;
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
    Label prezimeIlIOIBTvrtkeLabel;
    private PrimateljDonacije primateljDonacije;
    private ObservableList<PredmetDoniranja> slobodniPredmeti = FXCollections.observableArrayList(BazaPodataka.dohvatiSlobodnePredmete());
    public void initialize(){
        imeTextField.setDisable(true);
        prezimeTextField.setDisable(true);
        fizickaOsobaRadioButton.setDisable(true);
        pravnaOsobaRadioButton.setDisable(true);
        lokacijaChoiceBox.setDisable(true);
        trazeniPredmetiListView.setDisable(true);
        lokacijaChoiceBox.setItems(FXCollections.observableArrayList(Lokacija.values()));
        trazeniPredmetiListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        trazeniPredmetiListView.setItems(slobodniPredmeti);
        postaviVrijednostiCelija();

        urediCelije();

        inicijalizirajTablicuThread();

        primateljDonacijeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(Optional.ofNullable(primateljDonacijeTableView.getSelectionModel().getSelectedItem()).isPresent()){
                imeTextField.setDisable(false);
                prezimeTextField.setDisable(false);
                lokacijaChoiceBox.setDisable(false);
                trazeniPredmetiListView.setDisable(false);
                slobodniPredmeti.setAll(BazaPodataka.dohvatiSlobodnePredmete());
                primateljDonacije = primateljDonacijeTableView.getSelectionModel().getSelectedItem();
                imeTextField.setText(primateljDonacije.getImeIliImeTvrtke());
                prezimeTextField.setText(primateljDonacije.getPrezimeIliOIBTvrtke());
                if(primateljDonacije.getOpisPrimatelja().equalsIgnoreCase("pravna osoba")) {
                    pravnaOsobaRadioButton.setSelected(true);
                    postaviLabelePravnaOsoba();
                }
                else{
                    fizickaOsobaRadioButton.setSelected(true);
                    postaviLabeleFizickaOsoba();
                }
                lokacijaChoiceBox.setValue(primateljDonacije.getLokacija());
                slobodniPredmeti.addAll(primateljDonacije.getListaPotrebnihPredmeta().dohvatiSvePredmeteDoniranja());
                primateljDonacije.getListaPotrebnihPredmeta().dohvatiSvePredmeteDoniranja().stream()
                        .filter(slobodniPredmeti::contains)
                        .forEach(potrebanPredmet -> {
                            int index = slobodniPredmeti.indexOf(potrebanPredmet);
                            trazeniPredmetiListView.getSelectionModel().select(index);
                        });
            }
        });
    }
    public void izmjeniPrimatelja() {
        if(Optional.ofNullable(primateljDonacije).isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena primatelja donacije", "Pogreška prilikom izmjene primatelja donacije", "Potrebno je odabrati primatelja donacije kojeg želite izmjeniti.");
            return;
        }
        String imeIliImeTvrtke = imeTextField.getText();
        String prezimeIliOIBTvrtke = prezimeTextField.getText();
        Optional<String> opis = fizickaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("fizička osoba")
                : pravnaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("pravna osoba") : Optional.empty();
        Optional<Lokacija> lokacija = Optional.ofNullable(lokacijaChoiceBox.getValue());
        Optional<List<PredmetDoniranja>> trazeniPredmeti = Optional.of(new ArrayList<>(trazeniPredmetiListView.getSelectionModel().getSelectedItems()));

        PrimateljDonacije.PrimateljDonacijeBuilder primateljDonacijeBuilder = new PrimateljDonacije.PrimateljDonacijeBuilder();

        if(!imeIliImeTvrtke.isBlank()) {
            if(ValidatorUnosa.validatorUnosa(imeIliImeTvrtke)){
                primateljDonacijeBuilder.setImeIliImeTvrtke(imeIliImeTvrtke);
            }
            else{
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos primatelja donacije", "Greška prilikom unosa primatelja donacije", "Potrebno je upisati samo slova hrvatske abecede.");
                return;
            }
        }
        else
            primateljDonacijeBuilder.setImeIliImeTvrtke(primateljDonacije.getImeIliImeTvrtke());

        if(!prezimeIliOIBTvrtke.isBlank())
            primateljDonacijeBuilder.setPrezimeIliOIBTvrtke(prezimeIliOIBTvrtke);
        else
            primateljDonacijeBuilder.setPrezimeIliOIBTvrtke(primateljDonacije.getPrezimeIliOIBTvrtke());

        try{
            if(BazaPodataka.dohvatiSvePrimateljeDonacijaIzBazePodataka().stream().anyMatch(s->s.getImeIliImeTvrtke().equalsIgnoreCase(imeIliImeTvrtke) && s.getPrezimeIliOIBTvrtke().equalsIgnoreCase(prezimeIliOIBTvrtke))){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješna izmjena primatelja donacije", "Greška pri izmjeni podataka o primatelju donacije", "Uneseni primatelj donacije već postoji.");
                return;
            }
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        opis.ifPresentOrElse(primateljDonacijeBuilder::setOpisPrimatelja, ()->primateljDonacijeBuilder.setOpisPrimatelja(primateljDonacije.getOpisPrimatelja()));
        lokacija.ifPresentOrElse(primateljDonacijeBuilder::setLokacija, ()->primateljDonacijeBuilder.setLokacija(primateljDonacije.getLokacija()));
        if(trazeniPredmeti.get().size() > 0){
            primateljDonacijeBuilder.setListaPotrebnihPredmeta(new PredmetiDoniranja<>(trazeniPredmeti.get()));
        }
        else{
            primateljDonacijeBuilder.setListaPotrebnihPredmeta(new PredmetiDoniranja<>(new ArrayList<>()));
        }
        primateljDonacijeBuilder.setListaPrimljenihPredmeta(primateljDonacije.getListaPrimljenihPredmeta());
        primateljDonacijeBuilder.setId(primateljDonacije.getId());

        PrimateljDonacije noviPrimatelj = primateljDonacijeBuilder.createPrimateljDonacije();

        if(noviPrimatelj.equals(primateljDonacije)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena primatelja donacije", "Pogreška prilikom izmjene primatelja donacije", "Ne možete ostaviti iste podatke.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda izmjene primatelja donacije", "Potvrdite želite li promijeniti podatke o primatelju donacije.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            izmjeniPrimateljaThread(noviPrimatelj);
        }
    }
    private void izmjeniPrimateljaThread(PrimateljDonacije noviPrimatelj) {
        Thread thread = new Thread(() -> Platform.runLater(() -> {
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if(trenutniKorisnik instanceof Korisnik){
                PromjenaPodataka<PrimateljDonacije, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Primatelj donacije", primateljDonacije, noviPrimatelj, (Korisnik)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES) );
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            else{
                PromjenaPodataka<PrimateljDonacije, Admin> promjenaPodataka = new PromjenaPodataka<>("Primatelj donacije", primateljDonacije, noviPrimatelj, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            try {
                BazaPodataka.izmjeniPrimateljaUBaziPodataka(noviPrimatelj);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Izmjena primatelja donacije", "Uspješna izmjena primatelja donacije prilikom izmjene donora", "Uspješno ste promijenili podatke o primatelju donacije.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void inicijalizirajTablicuThread(){
        Thread thread = new Thread(()-> Platform.runLater(()->{
            try {
                primateljDonacijeTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePrimateljeDonacijaIzBazePodataka()));
                slobodniPredmeti.setAll(BazaPodataka.dohvatiSlobodnePredmete());
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void postaviVrijednostiCelija() {
        imeIliImeTvrtkeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getImeIliImeTvrtke()));
        prezimeIliOIBTvrtkeColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getPrezimeIliOIBTvrtke()));
        opisColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOpisPrimatelja()));
        gradColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getLokacija().getImeGrada()));
        primljeniPredmetiColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getListaPrimljenihPredmeta().toString()));
        trazeniPredmetiColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getListaPotrebnihPredmeta().toString()));
    }

    private void urediCelije() {
        gradColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        imeIliImeTvrtkeColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        prezimeIliOIBTvrtkeColumn.setCellFactory(tc->FXUtil.createWrappingCell());
        opisColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        trazeniPredmetiColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        primljeniPredmetiColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
    }
    @FXML
    public void postaviLabelePravnaOsoba(){
        Thread thread = new Thread(()->{
            Platform.runLater(()->{
                imeIliImeTvrtkeLabel.setText("Ime tvrtke:");
                prezimeIlIOIBTvrtkeLabel.setText("OIB tvrtke:");
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
                prezimeIlIOIBTvrtkeLabel.setText("Prezime: ");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }
}
