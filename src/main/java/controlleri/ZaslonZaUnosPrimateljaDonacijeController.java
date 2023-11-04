package controlleri;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import hr.java.vjezbe.iznimke.UcitavanjeFxmlaException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ZaslonZaUnosPrimateljaDonacijeController{
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
    Label imeIliImeTvrtkeLabel;
    @FXML
    Label prezimeIliOIBTvrtkeLabel;

    public void initialize(){
        try {
            trazeniPredmetiListView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSlobodnePredmete()));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        trazeniPredmetiListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lokacijaChoiceBox.setItems(FXCollections.observableArrayList(Lokacija.values()));
    }
    public void unesiPrimatelja(){
        String ime = imeTextField.getText();
        String prezime = prezimeTextField.getText();
        Optional<String> opis = fizickaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("fizička osoba")
                : pravnaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("pravna osoba") : Optional.empty();
        Optional<Lokacija> grad = Optional.ofNullable(lokacijaChoiceBox.getValue());
        List<PredmetDoniranja> trazeniPredmeti = new ArrayList<>(trazeniPredmetiListView.getSelectionModel().getSelectedItems());
        if(opis.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos primatelja donacije", "Greška pri unosu podataka o primatelju donacije", "Potrebno je odabrati radi li se o fizičkoj ili pravnoj osobi.");
            return;
        }
        if(ime.isBlank()) {
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos primatelja donacije", "Greška pri unosu podataka o primatelju donacije", "Potrebno je unijeti ime primatelja donacije.");
            return;
        }
        else if(!ValidatorUnosa.validatorUnosa(ime)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos primatelja donacije", "Greška prilikom unosa primatelja donacije", "Potrebno je upisati samo slova hrvatske abecede.");
            return;
        }
        if(prezime.isBlank()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos primatelja donacije", "Greška pri unosu podataka o primatelju donacije", "Potrebno je unijeti prezime primatelja donacije.");
            return;
        }
        try{
            if(BazaPodataka.dohvatiSvePrimateljeDonacijaIzBazePodataka().stream().anyMatch(s->s.getImeIliImeTvrtke().equalsIgnoreCase(ime) && s.getPrezimeIliOIBTvrtke().equalsIgnoreCase(prezime))){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos primatelja donacije", "Greška pri unosu podataka o primatelju donacije", "Uneseni primatelj donacije već postoji.");
                return;
            }
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        if(grad.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Neuspješan unos primatelja donacije", "Greška pri unosu podataka o primatelju donacije", "Potrebno je odabrati grad u kojem se nalazi primatelj donacije.\n(Ako primatelj donacije nije iz navedenih gradova onda se bira najbliži grad.)");
            return;
        }
        PrimateljDonacije primateljDonacije = new PrimateljDonacije.PrimateljDonacijeBuilder().setOpisPrimatelja(opis.get())
                .setLokacija(grad.get()).setImeIliImeTvrtke(ime).setPrezimeIliOIBTvrtke(prezime).setListaPotrebnihPredmeta(new PredmetiDoniranja<>(trazeniPredmeti)).createPrimateljDonacije();
        try {
            BazaPodataka.dodajPrimateljaDonacijeUBazuPodataka(primateljDonacije);
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
        if(trenutniKorisnik instanceof Korisnik){
            PromjenaPodataka<PrimateljDonacije, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Primatelj donacije", null, primateljDonacije, (Korisnik)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            Datoteke.serijalizirajPromjenu(promjenaPodataka);
        }
        else{
            PromjenaPodataka<PrimateljDonacije, Admin> promjenaPodataka = new PromjenaPodataka<>("Primatelj donacije", null, primateljDonacije, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            Datoteke.serijalizirajPromjenu(promjenaPodataka);
        }
        FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Unos primatelja donacije", "Uspješan unos primatelja donacije", "Uspješno ste unijeli podatke o novom primatelju donacije.");


        try{
            Glavna.pokaziZaslon(ZasloniAplikacije.ZASLON_ZA_PRETRAGU_I_BRISANJE_PRIMATELJA_DONACIJE);
        }
        catch (UcitavanjeFxmlaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Učitavanje zaslona", "Pogreška prilikom učitavanja fxml datoteke", "Neuspješno učitavanje fxml datoteke.");
        }
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
}
