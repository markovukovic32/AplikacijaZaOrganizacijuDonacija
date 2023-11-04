package controlleri;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import hr.java.vjezbe.iznimke.UcitavanjeFxmlaException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import tornadofx.control.DateTimePicker;
import util.Datoteke;
import util.FXUtil;
import util.PromjenaPodataka;
import util.ZasloniAplikacije;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZaslonZaUnosDonacijaController{
    @FXML
    ChoiceBox<PredmetDoniranja> predmetDoniranjaChoiceBox;
    @FXML
    ChoiceBox<Donor> donorChoiceBox;
    @FXML
    ChoiceBox<PrimateljDonacije> primateljDonacijeChoiceBox;
    @FXML
    DateTimePicker vrijemeDonacijeTimePicker;
    AtomicBoolean donorBool = new AtomicBoolean(false);
    AtomicBoolean primateljBool = new AtomicBoolean(false);

    public void initialize(){
        donorBool.set(false);
        primateljBool.set(false);
        vrijemeDonacijeTimePicker.setDateTimeValue(null);
        try {
            predmetDoniranjaChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiPredmeteZaDonaciju()));
            donorChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSveDonoreIzBazePodataka()));
            primateljDonacijeChoiceBox.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSvePrimateljeDonacijaIzBazePodataka()));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        predmetDoniranjaChoiceBox.setOnAction(event -> {
            PredmetDoniranja predmetDoniranja = predmetDoniranjaChoiceBox.getValue();
            Optional<PrimateljDonacije> primateljDonacije = Optional.empty();
            Optional<Donor> donor = Optional.empty();
            try {
                primateljDonacije = Optional.ofNullable(BazaPodataka.dohvatiPrimateljaDonacijePoPredmetuDoniranja(predmetDoniranja));
                donor = Optional.ofNullable(BazaPodataka.dohvatiDonoraPoPredmetuDoniranja(predmetDoniranja));
            }
            catch (BazaPodatakaException e){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
            }
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
    }
    public void unesiDonaciju(){
        Optional<PredmetDoniranja> predmetDoniranja = Optional.ofNullable(predmetDoniranjaChoiceBox.getValue());
        Optional<Donor> donor = Optional.ofNullable(donorChoiceBox.getValue());
        Optional<PrimateljDonacije> primateljDonacije = Optional.ofNullable(primateljDonacijeChoiceBox.getValue());
        Optional<LocalDateTime> datumIVrijeme = Optional.ofNullable(vrijemeDonacijeTimePicker.getDateTimeValue());
        if(predmetDoniranja.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donacije", "Greška prilikom unosa donacije", "Potrebno je odabrati predmet doniranja.");
            return;
        }
        if(donor.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donacije", "Greška prilikom unosa donacije", "Potrebno je odabrati donora.");
            return;
        }
        if(primateljDonacije.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donacije", "Greška prilikom unosa donacije", "Potrebno je odabrati primatelja donacije.");
            return;
        }
        if(datumIVrijeme.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donacije", "Greška prilikom unosa donacije", "Potrebno je odabrati vrijeme donacije.");
            return;
        }
        Donacija donacija = new Donacija.DonacijaBuilder().setPredmetDoniranja(predmetDoniranja.get()).setDonor(donor.get())
                .setPrimateljDonacije(primateljDonacije.get()).setVrijemeDonacije(datumIVrijeme.get()).createDonacija();
        KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
        if(trenutniKorisnik instanceof Korisnik){
            PromjenaPodataka<Donacija, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Donacija", null, donacija, (Korisnik)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            Datoteke.serijalizirajPromjenu(promjenaPodataka);
        }
        else{
            PromjenaPodataka<Donacija, Admin> promjenaPodataka = new PromjenaPodataka<>("Donacija", null, donacija, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            Datoteke.serijalizirajPromjenu(promjenaPodataka);
        }
        try {
            BazaPodataka.dodajDonacijuUBazuPodataka(donacija, donorBool, primateljBool);
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Unos donacije", "Uspješan unos donacije", "Uspješno ste unijeli podatke o novoj donaciji.");

        try{
            Glavna.pokaziZaslon(ZasloniAplikacije.ZASLON_ZA_PRETRAGU_I_BRISANJE_DONACIJA);
        }
        catch (UcitavanjeFxmlaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Učitavanje zaslona", "Pogreška prilikom učitavanja fxml datoteke", "Neuspješno učitavanje fxml datoteke.");
        }
    }
}
