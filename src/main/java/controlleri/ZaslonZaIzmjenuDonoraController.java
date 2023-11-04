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

import static util.FXUtil.createWrappingCell;

public class ZaslonZaIzmjenuDonoraController{
    @FXML
    TextField imeIliImeTvrtkeTextField;
    @FXML
    TextField prezimeIliOIBTvrtkeTextField;
    @FXML
    RadioButton fizickaOsobaRadioButton;
    @FXML
    RadioButton pravnaOsobaRadioButton;
    @FXML
    ChoiceBox<Lokacija> lokacijaChoiceBox;
    @FXML
    ListView<PredmetDoniranja> ponudjeniPredmetiListView;
    @FXML
    TableView<Donor> donorTableView;
    @FXML
    TableColumn<Donor,String> imeIliImeTvrtkeColumn;
    @FXML
    TableColumn<Donor,String> opisDonoraColumn;
    @FXML
    TableColumn<Donor,String> gradDonoraColumn;
    @FXML
    TableColumn<Donor,String> listaDoniranihPredmetaColumn;
    @FXML
    TableColumn<Donor,String> listaPonudjenihPredmetaColumn;
    @FXML
    TableColumn<Donor,String> prezimeIliOIBTvrtkeColumn;
    private Donor donor;
    @FXML
    Label imeIliImeTvrtke;
    @FXML
    Label prezimeIliOIBTvrtke;
    private final ObservableList<PredmetDoniranja> slobodniPredmeti = FXCollections.observableArrayList(BazaPodataka.dohvatiSlobodnePredmete());
    public void initialize(){
        fizickaOsobaRadioButton.setDisable(true);
        pravnaOsobaRadioButton.setDisable(true);
        ponudjeniPredmetiListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ponudjeniPredmetiListView.setItems(slobodniPredmeti);
        lokacijaChoiceBox.setItems(FXCollections.observableArrayList(Lokacija.values()));

        postaviVrijednostiCelija();

        urediCelije();

        inicijalizirajTablicuThread();

        donorTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(Optional.ofNullable(donorTableView.getSelectionModel().getSelectedItem()).isPresent()){
                try {
                    slobodniPredmeti.setAll(BazaPodataka.dohvatiSlobodnePredmete());
                }
                catch (BazaPodatakaException e){
                    FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
                }
                donor = donorTableView.getSelectionModel().getSelectedItem();
                imeIliImeTvrtkeTextField.setText(donor.getImeIliImeTvrtke());
                prezimeIliOIBTvrtkeTextField.setText(donor.getPrezimeIliOIBTvrtke());
                if(donor.getOpisDonora().equalsIgnoreCase("pravna osoba"))
                    pravnaOsobaRadioButton.setSelected(true);
                else{
                    fizickaOsobaRadioButton.setSelected(true);
                }
                lokacijaChoiceBox.setValue(donor.getLokacija());
                slobodniPredmeti.addAll(donor.getListaPonudjenihPredmeta().dohvatiSvePredmeteDoniranja());
                donor.getListaPonudjenihPredmeta().dohvatiSvePredmeteDoniranja().stream()
                        .filter(slobodniPredmeti::contains)
                        .forEach(potrebanPredmet -> {
                            int index = slobodniPredmeti.indexOf(potrebanPredmet);
                            ponudjeniPredmetiListView.getSelectionModel().select(index);
                        });
                if(pravnaOsobaRadioButton.isSelected()){
                    postaviLabelePravnaOsoba();
                }
                else if(fizickaOsobaRadioButton.isSelected()){
                    postaviLabeleFizickaOsoba();
                }
            }
        });
    }
    public void izmjeniDonora(){
        if(Optional.ofNullable(donor).isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena donora", "Pogreška prilikom promjene donora", "Potrebno je odabrati donora kojeg želite izmjeniti.");
            return;
        }
        String imeIliImeTvrtke = imeIliImeTvrtkeTextField.getText();
        String prezimeIliOIBTvrtke = prezimeIliOIBTvrtkeTextField.getText();
        Optional<String> opis = fizickaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("fizička osoba")
                : pravnaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("pravna osoba") : Optional.empty();
        Optional<Lokacija> lokacija = Optional.ofNullable(lokacijaChoiceBox.getValue());
        Optional<List<PredmetDoniranja>> ponudjeniPredmeti = Optional.of(new ArrayList<>(ponudjeniPredmetiListView.getSelectionModel().getSelectedItems()));

        Donor.DonorBuilder donorBuilder = new Donor.DonorBuilder();

        if(!imeIliImeTvrtke.isBlank()) {
            if(ValidatorUnosa.validatorUnosa(imeIliImeTvrtke)) {
                donorBuilder.setImeIliImeTvrtke(imeIliImeTvrtke);
            }
            else{
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena donora", "Greška prilikom izmjene donora", "Potrebno je upisati samo slova hrvatske abecede.");
                return;
            }
        }
        else
            donorBuilder.setImeIliImeTvrtke(donor.getImeIliImeTvrtke());

        if(!prezimeIliOIBTvrtke.isBlank()) {
            donorBuilder.setPrezimeIliOIBTvrtke(prezimeIliOIBTvrtke);
        }
        else
            donorBuilder.setPrezimeIliOIBTvrtke(donor.getPrezimeIliOIBTvrtke());

        try{
            if(BazaPodataka.dohvatiSveDonoreIzBazePodataka().stream().anyMatch(s->s.getImeIliImeTvrtke().equalsIgnoreCase(imeIliImeTvrtke) && s.getPrezimeIliOIBTvrtke().equalsIgnoreCase(prezimeIliOIBTvrtke))){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena donora", "Greška prilikom izmjene donora", "Uneseni donor već postoji.");
                return;
            }
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }

        opis.ifPresentOrElse(donorBuilder::setOpisDonora, ()->donorBuilder.setOpisDonora(donor.getOpisDonora()));
        lokacija.ifPresentOrElse(donorBuilder::setLokacija, ()->donorBuilder.setLokacija(donor.getLokacija()));

        if(ponudjeniPredmeti.get().size() > 0){
            donorBuilder.setListaPonudjenihPredmeta(new PredmetiDoniranja<>(ponudjeniPredmeti.get()));
        }
        else{
            donorBuilder.setListaPonudjenihPredmeta(new PredmetiDoniranja<>(new ArrayList<>()));
        }
        donorBuilder.setId(donor.getId());
        donorBuilder.setListaDoniranihPredmeta(donor.getListaDoniranihPredmeta());

        Donor noviDonor = donorBuilder.createDonor();

        if(noviDonor.equals(donor)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena donora", "Pogreška prilikom promjene donora", "Ne možete ostaviti iste podatke.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda izmjene donora", "Potvrdite želite li promijeniti podatke o donoru");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            izmjeniDonoraThread(noviDonor);
        }
    }

    private void izmjeniDonoraThread(Donor noviDonor){
        Thread thread = new Thread(()-> Platform.runLater(()-> {
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if(trenutniKorisnik instanceof Korisnik){
                PromjenaPodataka<Donor, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Donor", donor, noviDonor, (Korisnik)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            else{
                PromjenaPodataka<Donor, Admin> promjenaPodataka = new PromjenaPodataka<>("Donor", donor, noviDonor, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            try {
                BazaPodataka.izmjeniDonoraUBaziPodataka(noviDonor);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Izmjena donora", "Uspješna izmjena donora", "Uspješno ste promijenili podatke o donoru.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void inicijalizirajTablicuThread(){
        Thread thread = new Thread(()-> Platform.runLater(()->{
            try {
                donorTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveDonoreIzBazePodataka()));
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
        opisDonoraColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOpisDonora()));
        gradDonoraColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getLokacija().getImeGrada()));
        listaDoniranihPredmetaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getListaDoniranihPredmeta().toString()));
        listaPonudjenihPredmetaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getListaPonudjenihPredmeta().toString()));
    }

    private void urediCelije() {
        imeIliImeTvrtkeColumn.setCellFactory(tc -> createWrappingCell());
        prezimeIliOIBTvrtkeColumn.setCellFactory(tc -> createWrappingCell());
        opisDonoraColumn.setCellFactory(tc -> createWrappingCell());
        gradDonoraColumn.setCellFactory(tc -> createWrappingCell());
        listaDoniranihPredmetaColumn.setCellFactory(tc -> createWrappingCell());
        listaPonudjenihPredmetaColumn.setCellFactory(tc -> createWrappingCell());
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
}
