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

public class ZaslonZaUnosDonoraController{
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
    ListView<PredmetDoniranja> ponudjeniPredmetiListView;
    @FXML
    Label imeIliImeTvrtke;
    @FXML
    Label prezimeIliOIBTvrtke;

    public void initialize(){
        try{
            ponudjeniPredmetiListView.setItems(FXCollections.observableArrayList(BazaPodataka.dohvatiSlobodnePredmete()));
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        ponudjeniPredmetiListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lokacijaChoiceBox.setItems(FXCollections.observableArrayList(Lokacija.values()));
    }
    public void unesiDonora(){
        String imeIliImeTvrtke = imeTextField.getText();
        String prezimeIliOIBTvrtke = prezimeTextField.getText();
        Optional<String> opis = fizickaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("fizička osoba")
                : pravnaOsobaRadioButton.selectedProperty().get()
                ? Optional.of("pravna osoba") : Optional.empty();
        Optional<Lokacija> grad = Optional.ofNullable(lokacijaChoiceBox.getValue());
        List<PredmetDoniranja> trazeniPredmeti = new ArrayList<>(ponudjeniPredmetiListView.getSelectionModel().getSelectedItems());
        if(opis.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donora", "Greška prilikom unosa donora", "Potrebno je odabrati radi li se o fizičkoj ili pravnoj osobi.");
            return;
        }
        if(imeIliImeTvrtke.isBlank()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donora", "Greška prilikom unosa donora", "Potrebno je unijeti ime.");
            return;
        }
        if(!ValidatorUnosa.validatorUnosa(imeIliImeTvrtke)){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donora", "Greška prilikom unosa donora", "Potrebno je upisati samo slova hrvatske abecede.");
            return;
        }
        if(prezimeIliOIBTvrtke.isBlank()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donora", "Greška prilikom unosa donora", "Potrebno je unijeti prezime.");
            return;
        }
        try{
            if(BazaPodataka.dohvatiSveDonoreIzBazePodataka().stream().anyMatch(s->s.getImeIliImeTvrtke().equalsIgnoreCase(imeIliImeTvrtke) && s.getPrezimeIliOIBTvrtke().equalsIgnoreCase(prezimeIliOIBTvrtke)))  {
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donora", "Greška prilikom unosa donora", "Uneseni donor već postoji.");
                return;
            }
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }

        if(grad.isEmpty()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos donora", "Greška prilikom unosa donora", "Potrebno je odabrati grad u kojem se nalazi donor.\n(Ako donor nije iz navedenih gradova onda se \nbira najbliži grad.)");
            return;
        }
        Donor donor = new Donor.DonorBuilder().setOpisDonora(opis.get())
                .setLokacija(grad.get()).setImeIliImeTvrtke(imeIliImeTvrtke).setPrezimeIliOIBTvrtke(prezimeIliOIBTvrtke).setListaPonudjenihPredmeta(new PredmetiDoniranja<>(trazeniPredmeti)).createDonor();
        KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
        if(trenutniKorisnik instanceof Korisnik){
            PromjenaPodataka<Donor, Korisnik> promjenaPodataka = new PromjenaPodataka<>("Donor", null, donor, (Korisnik)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            Datoteke.serijalizirajPromjenu(promjenaPodataka);
        }
        else{
            PromjenaPodataka<Donor, Admin> promjenaPodataka = new PromjenaPodataka<>("Donor", null, donor, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            Datoteke.serijalizirajPromjenu(promjenaPodataka);
        }
        try {
            BazaPodataka.dodajDonoraUBazuPodataka(donor);
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Unos donora", "Uspješan unos donora", "Uspješno ste unijeli podatke o novom donoru.");

        try{
            Glavna.pokaziZaslon(ZasloniAplikacije.ZASLON_ZA_PRETRAGU_I_BRISANJE_DONORA);
        }
        catch (UcitavanjeFxmlaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Učitavanje zaslona", "Pogreška prilikom učitavanja fxml datoteke", "Neuspješno učitavanje fxml datoteke.");
        }
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
