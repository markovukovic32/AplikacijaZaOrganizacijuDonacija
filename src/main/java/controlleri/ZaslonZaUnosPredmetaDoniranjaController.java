package controlleri;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import hr.java.vjezbe.iznimke.UcitavanjeFxmlaException;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class ZaslonZaUnosPredmetaDoniranjaController{
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
    Label kolicinaLabel;
    public void initialize(){
        opisPredmetaTextField.setPromptText("npr. tjestenina, brašno, hlače");
        vrstaPredmetaChoiceBox.setItems(FXCollections.observableArrayList("odjeća", "hrana"));
        stanjeChoiceBox.setItems(FXCollections.observableArrayList(Stanje.values()));
        velicinaChoiceBox.setItems(FXCollections.observableArrayList(Velicina.values()));

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

        kolicinaSlider.setValue(0);
        kolicinaLabel.textProperty().bind(
                Bindings.format(
                        "%.1f",
                        kolicinaSlider.valueProperty()
                )
        );
    }
    public void unesiPredmet(){
        String opisPredmeta = opisPredmetaTextField.getText();
        Optional<String> vrstaPredmeta = Optional.ofNullable(vrstaPredmetaChoiceBox.getValue());
        Optional<LocalDate> rokTrajanja = Optional.ofNullable(rokTrajanjaDatePicker.getValue());
        Optional<Stanje> stanje = Optional.ofNullable(stanjeChoiceBox.getValue());
        Optional<Double> kolicina = Optional.of(kolicinaSlider.getValue());
        Optional<Velicina> velicina = Optional.ofNullable(velicinaChoiceBox.getValue());

        if(opisPredmeta.isBlank()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos predmeta za doniranje", "Greška pri unosu podataka o predmetu za doniranje", "Potrebno je opisati o kakvom se predmetu radi.");
            return;
        }
        if(!ValidatorUnosa.validatorUnosa(opisPredmeta)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos predmeta doniranja", "Greška prilikom unosa predmeta doniranja", "Potrebno je upisati samo slova hrvatske abecede.");
            return;
        }

        if(vrstaPredmeta.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos predmeta za doniranje", "Greška pri unosu podataka o predmetu za doniranje", "Potrebno je odabrati vrstu predmeta.");
            return;
        }

        if(vrstaPredmeta.get().equalsIgnoreCase("odjeća")){
            if(stanje.isEmpty()){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos odjeće", "Greška pri unosu podataka o odjeći", "Potrebno je odabrati u kakvom se stanju nalazi odjeća.");
                return;
            }
            if(velicina.isEmpty()){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos odjeće", "Greška pri unosu podataka o odjeći", "Potrebno je odabrati veličinu odjevnog predmeta.");
                return;
            }

            Odjeca odjeca = new Odjeca.OdjecaBuilder().setOpisPredmeta(opisPredmeta).setStanje(stanje.get())
                    .setVelicina(velicina.get()).createOdjeca();
            try {
                BazaPodataka.dodajOdjecuUBazuPodataka(odjeca);
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if(trenutniKorisnik instanceof Korisnik){
                PromjenaPodataka<PredmetDoniranja, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Predmet doniranja", null, odjeca, (Korisnik)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            else{
                PromjenaPodataka<PredmetDoniranja, Admin> promjenaPodataka = new PromjenaPodataka<>("Predmet doniranja", null, odjeca, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
        }
        else if(vrstaPredmeta.get().equalsIgnoreCase("hrana")){
            if(rokTrajanja.isEmpty()){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos hrane", "Greška pri unosu podataka o hrani", "Potrebno je odabrati rok trajanja donirane hrane.");
                return;
            }
            if(rokTrajanja.get().isBefore(LocalDate.now())){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos hrane", "Greška pri unosu podataka o hrani", "Nije moguće donirati hranu kojoj je rok istekao.");
                return;
            }
            if(rokTrajanja.get().isBefore(LocalDate.now().plusWeeks(2))){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos hrane", "Greška pri unosu podataka o hrani", "Nije moguće donirati hranu pred istekom roka. Prihvaćamo hranu s rokom trajanja od " +
                        LocalDate.now().plusWeeks(2).format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")) + " pa nadalje.");
                return;
            }
            if(kolicina.get().equals(0.0)){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos hrane", "Greška pri unosu podataka o hrani", "Potrebno je odabrati količinu donirane hrane (kg ili l).");
                return;
            }
            kolicina = Optional.of((double) Math.round(kolicina.get() * 10) / 10);
            Hrana hrana = new Hrana.HranaBuilder().setOpisPredmeta(opisPredmeta).setRokTrajanja(rokTrajanja.get())
                    .setKolicinaUKgIliL(kolicina.get()).createHrana();
            try {
                BazaPodataka.dodajHranuUBazuPodataka(hrana);
            } catch (BazaPodatakaException e) {
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Greška baze podataka", "Greška pri radu s bazom podataka", "Pogreška pri dodavanju hrane u bazu podataka.");
            }
            KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
            if(trenutniKorisnik instanceof Korisnik){
                PromjenaPodataka<PredmetDoniranja, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Predmet doniranja", null, hrana, (Korisnik) trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
            else{
                PromjenaPodataka<PredmetDoniranja, Admin> promjenaPodataka = new PromjenaPodataka<>("Predmet doniranja", null, hrana, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
                Datoteke.serijalizirajPromjenu(promjenaPodataka);
            }
        }
        FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Unos predmeta za doniranje", "Uspješan unos predmeta za doniranje", "Uspješno ste unijeli podatke o novom predmetu za doniranje.");

        try{
            Glavna.pokaziZaslon(ZasloniAplikacije.ZASLON_ZA_PRETRAGU_I_BRISANJE_PREDMETA_DONIRANJA);
        }
        catch (UcitavanjeFxmlaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Učitavanje zaslona", "Pogreška prilikom učitavanja fxml datoteke", "Neuspješno učitavanje fxml datoteke.");
        }
    }
}
