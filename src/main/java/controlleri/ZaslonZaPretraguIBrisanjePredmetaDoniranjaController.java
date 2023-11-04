package controlleri;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.Datoteke;
import util.FXUtil;
import util.PromjenaPodataka;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static util.FXUtil.createWrappingCell;

public class ZaslonZaPretraguIBrisanjePredmetaDoniranjaController{
    @FXML
    ChoiceBox<String> vrstaPredmetaChoiceBox;
    @FXML
    TextField opisPredmetaTextField;
    @FXML
    DatePicker rokTrajanjaDatePicker;
    @FXML
    ChoiceBox<Stanje> stanjeChoiceBox;
    @FXML
    Slider kolicinaSlider;
    @FXML
    ChoiceBox<Velicina> velicinaChoiceBox;
    @FXML
    TableView<PredmetDoniranja> predmetiDoniranjaTableView;
    @FXML
    TableColumn<PredmetDoniranja, String> opisPredmetaColumn;
    @FXML
    TableColumn<PredmetDoniranja, String> rokTrajanjaIliStanjeColumn;
    @FXML
    TableColumn<PredmetDoniranja, String> kolicinaUKgIliVelicinaColumn;
    @FXML
    Label kolicinaLabel;

    public void initialize(){
        vrstaPredmetaChoiceBox.setItems(FXCollections.observableArrayList("hrana", "odjeća"));

        BooleanBinding vrstaBinding = Bindings.createBooleanBinding(() -> {
            String vrsta = vrstaPredmetaChoiceBox.getSelectionModel().getSelectedItem();
            return vrsta == null || !vrsta.equalsIgnoreCase("hrana");
        }, vrstaPredmetaChoiceBox.getSelectionModel().selectedItemProperty());
        kolicinaLabel.disableProperty().bind(vrstaBinding);
        rokTrajanjaDatePicker.disableProperty().bind(vrstaBinding);
        kolicinaSlider.disableProperty().bind(vrstaBinding);
        BooleanBinding vrstaBinding2 = Bindings.createBooleanBinding(() -> {
            String vrsta = vrstaPredmetaChoiceBox.getSelectionModel().getSelectedItem();
            return vrsta == null || !vrsta.equalsIgnoreCase("odjeća");
        }, vrstaPredmetaChoiceBox.getSelectionModel().selectedItemProperty());

        stanjeChoiceBox.disableProperty().bind(vrstaBinding2);
        velicinaChoiceBox.disableProperty().bind(vrstaBinding2);

        stanjeChoiceBox.setItems(FXCollections.observableArrayList(Stanje.values()));
        velicinaChoiceBox.setItems(FXCollections.observableArrayList(Velicina.values()));

        urediIPostaviVrijednostCelija();

        try {
            predmetiDoniranjaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePredmeteDoniranjaIzBazePodataka()));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        kolicinaLabel.textProperty().bind(
                Bindings.format(
                        "%.1f",
                        kolicinaSlider.valueProperty()
                )
        );
    }

    public void filtriraj(){
        Optional<String> opisPredmeta = Optional.ofNullable(opisPredmetaTextField.getText());
        Optional<LocalDate> rokTrajanja = Optional.ofNullable(rokTrajanjaDatePicker.getValue());
        Optional<Stanje> stanje = Optional.ofNullable(stanjeChoiceBox.getValue());
        Optional<Double> kolicina = Optional.of(kolicinaSlider.getValue());
        Optional<Velicina> velicina = Optional.ofNullable(velicinaChoiceBox.getValue());
        if(Optional.ofNullable(vrstaPredmetaChoiceBox.getValue()).isPresent()){

            if(vrstaPredmetaChoiceBox.getValue().equalsIgnoreCase("odjeća")){
                kolicinaLabel.setVisible(false);
                opisPredmetaColumn.setText("Opis odjeće");
                kolicinaUKgIliVelicinaColumn.setText("Veličina");
                rokTrajanjaIliStanjeColumn.setText("Stanje");

                Odjeca.OdjecaBuilder odjecaBuilder = new Odjeca.OdjecaBuilder();

                opisPredmeta.ifPresent(odjecaBuilder::setOpisPredmeta);
                opisPredmeta.ifPresent(odjecaBuilder::setOpisPredmeta);
                stanje.ifPresent(odjecaBuilder::setStanje);
                velicina.ifPresent(odjecaBuilder::setVelicina);

                Odjeca odjeca = odjecaBuilder.createOdjeca();
                try {
                    predmetiDoniranjaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiOdjecuPremaKriterijima(odjeca)));
                }
                catch (BazaPodatakaException e){
                    FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
                }
            }
            else if(vrstaPredmetaChoiceBox.getValue().equalsIgnoreCase("hrana")){
                kolicinaLabel.setVisible(true);
                opisPredmetaColumn.setText("Opis hrane");
                kolicinaUKgIliVelicinaColumn.setText("Količina (kg / l)");
                rokTrajanjaIliStanjeColumn.setText("Rok trajanja");
                Hrana.HranaBuilder hranaBuilder = new Hrana.HranaBuilder();

                opisPredmeta.ifPresent(hranaBuilder::setOpisPredmeta);
                rokTrajanja.ifPresent(hranaBuilder::setRokTrajanja);
                if(!kolicina.get().equals(0.0)){
                    kolicina = Optional.of((double) Math.round(kolicina.get() * 10) / 10);
                    hranaBuilder.setKolicinaUKgIliL(kolicina.get());
                }
                Hrana hrana = hranaBuilder.createHrana();
                try {
                    predmetiDoniranjaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiHranuPremaKriterijima(hrana)));
                }
                catch (BazaPodatakaException e){
                    FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
                }
            }
        }
        else{
            if(opisPredmeta.isPresent()){
                PredmetDoniranja predmetDoniranja = new PredmetDoniranja.PredmetDoniranjaBuilder().setOpisPredmeta(opisPredmeta.get()).createPredmetDoniranja();
                try {
                    predmetiDoniranjaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiPredmeteDoniranjaPremaKriterijima(predmetDoniranja)));
                }
                catch (BazaPodatakaException e){
                    FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
                }
            }
        }
    }
    public void obrisiPredmet() {
        Optional<PredmetDoniranja> predmetDoniranja = Optional.ofNullable(predmetiDoniranjaTableView.getSelectionModel().getSelectedItem());
        if(predmetDoniranja.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Brisanje predmeta doniranja", "Greška prilikom brisanja predmeta doniranja", "Potrebno je odabrati predmet doniranja kojeg želite obrisati.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda brisanja predmeta za doniranje", "Potvrdite želite li obrisati podatke o odabranom predmetu za doniranje.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            obrisiPredmetDoniranjaThread(predmetDoniranja.get());
        }
    }

    private void obrisiPredmetDoniranjaThread(PredmetDoniranja predmetDoniranja) {
        Thread thread = new Thread(()-> Platform.runLater(()-> {
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if (trenutniKorisnik instanceof Korisnik) {
                PromjenaPodataka<PredmetDoniranja, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Predmet doniranja", predmetDoniranja, null, (Korisnik) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            } else {
                PromjenaPodataka<PredmetDoniranja, Admin> promjenaPodataka = new PromjenaPodataka<>("Predmet doniranja", predmetDoniranja, null, (Admin) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            try {
                BazaPodataka.obrisiPredmetDoniranjaIzBazePodataka(predmetDoniranja);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Brisanje predmeta doniranja", "Uspješno brisanje predmeta doniranja", "Uspješno ste obrisali podatke o predmetu doniranja.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void inicijalizirajTablicuThread(){
        Thread thread = new Thread(()-> Platform.runLater(()-> {
            try {
                predmetiDoniranjaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePredmeteDoniranjaIzBazePodataka()));
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }

    private void urediIPostaviVrijednostCelija() {
        opisPredmetaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOpisPredmeta()));
        rokTrajanjaIliStanjeColumn.setCellValueFactory(s ->
                new SimpleStringProperty(s.getValue() instanceof Hrana ?
                        ((Hrana)s.getValue()).getRokTrajanja().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")) :
                        ((Odjeca)s.getValue()).getStanje().toString()));
        kolicinaUKgIliVelicinaColumn.setCellValueFactory(s ->
                new SimpleStringProperty(s.getValue() instanceof Hrana ?
                        ((Hrana)s.getValue()).getKolicinaUKgIliL().toString() :
                        ((Odjeca)s.getValue()).getVelicina().toString()));

        kolicinaUKgIliVelicinaColumn.setCellFactory(tc -> createWrappingCell());
        opisPredmetaColumn.setCellFactory(tc -> createWrappingCell());
        rokTrajanjaIliStanjeColumn.setCellFactory(tc -> createWrappingCell());
    }
}
