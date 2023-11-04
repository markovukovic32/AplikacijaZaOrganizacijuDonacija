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
import util.ValidatorUnosa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


public class ZaslonZaIzmjenuPredmetaDoniranjaController{
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
    private PredmetDoniranja predmetDoniranja;
    public void initialize(){
        vrstaPredmetaChoiceBox.setDisable(true);
        opisPredmetaTextField.setDisable(true);
        vrstaPredmetaChoiceBox.setItems(FXCollections.observableArrayList("hrana", "odjeća"));

        BooleanBinding vrstaBinding = Bindings.createBooleanBinding(() -> {
            String vrsta = vrstaPredmetaChoiceBox.getSelectionModel().getSelectedItem();
            return vrsta == null || !vrsta.equalsIgnoreCase("hrana");
        }, vrstaPredmetaChoiceBox.getSelectionModel().selectedItemProperty());

        rokTrajanjaDatePicker.disableProperty().bind(vrstaBinding);
        kolicinaSlider.disableProperty().bind(vrstaBinding);

        BooleanBinding vrstaBinding2 = Bindings.createBooleanBinding(() -> {
            String vrsta = vrstaPredmetaChoiceBox.getSelectionModel().getSelectedItem();
            return vrsta == null || !vrsta.equalsIgnoreCase("odjeća");
        }, vrstaPredmetaChoiceBox.getSelectionModel().selectedItemProperty());

        stanjeChoiceBox.disableProperty().bind(vrstaBinding2);
        velicinaChoiceBox.disableProperty().bind(vrstaBinding2);
        kolicinaLabel.setVisible(false);
        kolicinaSlider.setValue(0);
        stanjeChoiceBox.setItems(FXCollections.observableArrayList(Stanje.values()));
        velicinaChoiceBox.setItems(FXCollections.observableArrayList(Velicina.values()));

        urediIPostaviVrijednostiCelija();

        inicijalizirajTablicuThread();

        kolicinaLabel.textProperty().bind(
                Bindings.format(
                        "%.1f",
                        kolicinaSlider.valueProperty()
                )
        );
        predmetiDoniranjaTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(Optional.ofNullable(predmetiDoniranjaTableView.getSelectionModel().getSelectedItem()).isPresent()){
                opisPredmetaTextField.setDisable(false);
                predmetDoniranja = predmetiDoniranjaTableView.getSelectionModel().getSelectedItem();
                opisPredmetaTextField.setText(predmetDoniranja.getOpisPredmeta());
                if(predmetDoniranja instanceof Odjeca){
                    vrstaPredmetaChoiceBox.setValue("odjeća");
                    vrstaPredmetaChoiceBox.setDisable(true);
                    stanjeChoiceBox.setValue(((Odjeca) predmetDoniranja).getStanje());
                    velicinaChoiceBox.setValue(((Odjeca) predmetDoniranja).getVelicina());
                    kolicinaLabel.setVisible(false);
                }
                else if(predmetDoniranja instanceof Hrana){
                    kolicinaLabel.setVisible(true);
                    vrstaPredmetaChoiceBox.setValue("hrana");
                    vrstaPredmetaChoiceBox.setDisable(true);
                    kolicinaSlider.setValue(((Hrana) predmetDoniranja).getKolicinaUKgIliL());
                    rokTrajanjaDatePicker.setValue(((Hrana) predmetDoniranja).getRokTrajanja());
                }
            }
        });
    }

    public void izmjeniPredmet(){
        if(Optional.ofNullable(predmetDoniranja).isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena donora", "Pogreška prilikom izmjene donora", "Potrebno je odabrati predmet kojeg želite izmjeniti.");
            return;
        }
        String opisPredmeta = opisPredmetaTextField.getText();
        Optional<String> vrstaPredmeta = Optional.ofNullable(vrstaPredmetaChoiceBox.getValue());
        Optional<LocalDate> rokTrajanja = Optional.ofNullable(rokTrajanjaDatePicker.getValue());
        Optional<Double> kolicina = Optional.of(kolicinaSlider.getValue());
        Optional<Stanje> stanje = Optional.ofNullable(stanjeChoiceBox.getValue());
        Optional<Velicina> velicina = Optional.ofNullable(velicinaChoiceBox.getValue());
        PredmetDoniranja noviPredmet;

        if(!ValidatorUnosa.validatorUnosa(opisPredmeta)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena predmeta doniranja", "Greška prilikom izmjene predmeta doniranja", "Potrebno je upisati samo slova hrvatske abecede.");
            return;
        }

        if(predmetDoniranja instanceof Hrana){
            Hrana.HranaBuilder hranaBuilder = new Hrana.HranaBuilder();
            if(!opisPredmeta.isBlank())
                hranaBuilder.setOpisPredmeta(opisPredmeta);
            else
                hranaBuilder.setOpisPredmeta(predmetDoniranja.getOpisPredmeta());

            if(vrstaPredmeta.isPresent()){
                rokTrajanja.ifPresentOrElse(hranaBuilder::setRokTrajanja, ()->hranaBuilder.setRokTrajanja(((Hrana) predmetDoniranja).getRokTrajanja()));
                kolicina = Optional.of((double) Math.round(kolicina.get() * 10) / 10);
                if(!kolicina.get().equals(0.0))
                    hranaBuilder.setKolicinaUKgIliL(kolicina.get());
                else
                    hranaBuilder.setKolicinaUKgIliL(((Hrana) predmetDoniranja).getKolicinaUKgIliL());
            }
            hranaBuilder.setId(predmetDoniranja.getId());
            noviPredmet = hranaBuilder.createHrana();
        }
        else{
            Odjeca.OdjecaBuilder odjecaBuilder = new Odjeca.OdjecaBuilder();
            if(!opisPredmeta.isBlank())
                odjecaBuilder.setOpisPredmeta(opisPredmeta);
            else
                odjecaBuilder.setOpisPredmeta(predmetDoniranja.getOpisPredmeta());
            if(vrstaPredmeta.isPresent()){
                velicina.ifPresentOrElse(odjecaBuilder::setVelicina,()->odjecaBuilder.setVelicina(((Odjeca) predmetDoniranja).getVelicina()));
                stanje.ifPresentOrElse(odjecaBuilder::setStanje, ()->odjecaBuilder.setStanje(((Odjeca) predmetDoniranja).getStanje()));
            }
            odjecaBuilder.setId(predmetDoniranja.getId());
            noviPredmet = odjecaBuilder.createOdjeca();
        }

        if(noviPredmet.equals(predmetDoniranja)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Izmjena predmeta za doniranje", "Pogreška prilikom izmjene predmeta za doniranje", "Ne možete ostaviti iste podatke.");
            return;
        }

        Alert alert = FXUtil.dohvatiAlert(Alert.AlertType.CONFIRMATION, "Potvrda akcije", "Potvrda izmjene predmeta za doniranje", "Potvrdite želite li promijeniti podatke o predmetu za doniranje");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            izmjeniPredmetDoniranjaThread(noviPredmet);
        }
    }

    private void izmjeniPredmetDoniranjaThread(PredmetDoniranja noviPredmet){
        Thread thread = new Thread(() -> Platform.runLater(() -> {
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if(trenutniKorisnik instanceof Korisnik){
                PromjenaPodataka<PredmetDoniranja, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Predmet doniranja", predmetDoniranja, noviPredmet, (Korisnik)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            else{
                PromjenaPodataka<PredmetDoniranja, Admin> promjenaPodataka = new PromjenaPodataka<>("Predmet doniranja", predmetDoniranja, noviPredmet, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            try {
                BazaPodataka.izmjeniPredmetDoniranjaUBaziPodataka(noviPredmet);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
            }
            FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Izmjena donora", "Uspješna izmjena predmeta za doniranje", "Uspješno ste promijenili podatke o predmetu za doniranje.");
            inicijalizirajTablicuThread();
        }));
        thread.setDaemon(true);
        thread.start();
    }
    private void inicijalizirajTablicuThread(){
        Thread thread = new Thread(()-> Platform.runLater(()->{
            try {
                predmetiDoniranjaTableView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePredmeteDoniranjaIzBazePodataka()));
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
            }
        }));
        thread.setDaemon(true);
        thread.start();
    }

    private void urediIPostaviVrijednostiCelija() {
        opisPredmetaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getOpisPredmeta()));
        rokTrajanjaIliStanjeColumn.setCellValueFactory(s ->
                new SimpleStringProperty(s.getValue() instanceof Hrana ?
                        ((Hrana)s.getValue()).getRokTrajanja().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")) :
                        ((Odjeca)s.getValue()).getStanje().toString()));
        kolicinaUKgIliVelicinaColumn.setCellValueFactory(s ->
                new SimpleStringProperty(s.getValue() instanceof Hrana ?
                        ((Hrana)s.getValue()).getKolicinaUKgIliL().toString() :
                        ((Odjeca)s.getValue()).getVelicina().toString()));

        kolicinaUKgIliVelicinaColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        rokTrajanjaIliStanjeColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        opisPredmetaColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
    }
}
