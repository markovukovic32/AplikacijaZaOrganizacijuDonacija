package controlleri;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import hr.java.vjezbe.iznimke.NeispravnoKorisnickoImeException;
import hr.java.vjezbe.iznimke.UcitavanjeFxmlaException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class ZaslonAdminaZaUnosKorisnikaController {
    private static final Logger logger = LoggerFactory.getLogger(ZaslonAdminaZaUnosKorisnikaController.class);
    @FXML
    TextField korisnickoImeTextField;
    @FXML
    TextField imeTextField;
    @FXML
    TextField prezimeTextField;
    @FXML
    TextField emailTextField;
    @FXML
    PasswordField lozinkaField;
    @FXML
    ChoiceBox<Lokacija> lokacijaChoiceBox;
    private List<KorisnickiPodatci> listaKorisnika;
    private List<Korisnik> listaKorisnikaIzBazePodataka;

    public void initialize(){
        emailTextField.setPromptText("someone@example.com");
        listaKorisnika = Datoteke.ucitajDatotekuKorisnika();
        try {
            listaKorisnikaIzBazePodataka = BazaPodataka.dohvatiSveKorisnikeIzBazePodataka();
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        lokacijaChoiceBox.setItems(FXCollections.observableArrayList(Lokacija.values()));
    }
    public void unesiKorisnika() throws NoSuchAlgorithmException {
        String korisnickoIme = korisnickoImeTextField.getText();
        String email = emailTextField.getText();
        String lozinka = lozinkaField.getText();
        String ime = imeTextField.getText();
        String prezime = prezimeTextField.getText();
        Optional<Lokacija> lokacija = Optional.ofNullable(lokacijaChoiceBox.getValue());

        try {
            if (!KorisnikManager.provjeriKorisnickoIme(korisnickoIme))
                return;
            if(BazaPodataka.dohvatiSveKorisnikeIzBazePodataka().stream().anyMatch(s->s.getKorisnickoIme().equalsIgnoreCase(korisnickoIme))){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Korisničko ime je već zauzeto.");
                return;
            }
        } catch (NeispravnoKorisnickoImeException e) {
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Korisničko ime mora sadržavati najmanje 4 znakova.");
        }

        if(ime.isBlank()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Potrebno je unijeti ime korisnika.");
            return;
        }
        else{
            if(!ValidatorUnosa.validatorUnosa(ime)){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Potrebno je upisati samo slova hrvatske abecede.");
                return;
            }
        }

        if(prezime.isBlank()){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Potrebno je unijeti prezime korisnika.");
            return;
        }
        else{
            if(!ValidatorUnosa.validatorUnosa(prezime)){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Potrebno je upisati samo slova hrvatske abecede.");
                return;
            }
        }

        if (!KorisnikManager.provjeriEmail(email))
            return;
        try{
            if(BazaPodataka.dohvatiKorisnikePremaKriterijima(new Korisnik.KorisnikBuilder().setEmail(email).createKorisnik()).stream().findFirst().isPresent()){
                FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Uneseni email je već zauzet.");
                return;
            }
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška baze podataka", "Greška prilikom dohvaćanja podataka iz baze podataka.");
        }
        if (!KorisnikManager.provjeriLozinku(lozinka))
            return;

        if (lokacija.isEmpty()) {
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Potrebno je odabrati lokaciju našeg najbližeg skladišta.");
            return;
        }

        if (listaKorisnika.stream().anyMatch(s->s.getKorisnickoIme().equalsIgnoreCase(korisnickoIme))) {
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Odabrano korisničko ime je zauzeto.");
            return;
        }

        if (listaKorisnikaIzBazePodataka.stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(email))) {
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Odabrana email adresa je već zauzeta.");
            return;
        }

        Korisnik korisnik = new Korisnik.KorisnikBuilder().setOsoba(new Osoba.OsobaBuilder().setIme(ime).setPrezime(prezime).createOsoba()).
                setKorisnickoIme(korisnickoIme).setLokacija(lokacija.get()).setEmail(email).setHashLozinka(HashLozinka.hashPassword(lozinka)).createKorisnik();

        KorisnickiPodatci trenutniKorisnik = Datoteke.dohvatiTrenutnogKorisnika();
        PromjenaPodataka<Korisnik, Admin> promjenaPodataka = new PromjenaPodataka<>("Korisnik", null, korisnik, (Admin)trenutniKorisnik, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        Datoteke.serijalizirajPromjenu(promjenaPodataka);
        try {
            Datoteke.dodajKorisnikaUDatoteku(korisnik);
            BazaPodataka.dodajKorisnikaUBazuPodataka(korisnik);
        }
        catch (BazaPodatakaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Baza podataka", "Greška prilikom korištenja baze podataka", "Greška prilikom izvršavanja upita u bazi podataka.");
        }

        FXUtil.pokaziAlert(Alert.AlertType.INFORMATION, "Unos korisnika", "Uspješan unos korisnika", "Uspješno ste unijeli novog korisnika.");


        try{
            Glavna.pokaziZaslon(ZasloniAplikacije.ZASLON_ADMINA_ZA_PRETRAGU_I_BRISANJE_KORISNIKA);
        }
        catch (UcitavanjeFxmlaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Učitavanje zaslona", "Pogreška prilikom učitavanja fxml datoteke", "Neuspješno učitavanje fxml datoteke.");
        }
    }
}
