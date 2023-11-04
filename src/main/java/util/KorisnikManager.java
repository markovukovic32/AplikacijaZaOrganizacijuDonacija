package util;

import hr.java.vjezbe.entitet.Admin;
import hr.java.vjezbe.entitet.KorisnickiPodatci;
import hr.java.vjezbe.entitet.Korisnik;
import hr.java.vjezbe.iznimke.NeispravnaLozinkaException;
import hr.java.vjezbe.iznimke.NeuspjesnaPrijavaException;
import hr.java.vjezbe.iznimke.NeispravnoKorisnickoImeException;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class KorisnikManager {
    private static final Logger logger = LoggerFactory.getLogger(KorisnikManager.class);

    private static final String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    public static boolean provjeriKorisnickoIme(String korisnickoIme) throws NeispravnoKorisnickoImeException {
        if (korisnickoIme.length() >= 4)
            return true;
        String poruka = "Korisničko ime mora sadržavati najmanje 4 znakova.";
        logger.error(poruka);
        throw new NeispravnoKorisnickoImeException(poruka);
    }
    public static boolean provjeriEmail(String emailAddress) {
        if(Pattern.compile(regexPattern).matcher(emailAddress).matches())
            return true;
        FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Potrebno je unijeti email adresu u ispravnom formatu.\n(npr. someone@example.com)");
        return false;
    }
    public static boolean provjeriLozinku(String lozinka){
        if(!lozinka.isBlank()) {
            if (lozinka.length() >= 4)
                return true;
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Lozinka mora sadržavati najmanje 4 znakova.");
        }
        else{
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Unos korisnika", "Pogreška prilikom unosa korisnika", "Potrebno je unijeti lozinku.");
        }
        return false;
    }
    public static void provjeriPodatkeZaPrijavu(String korisnickoIme, String lozinka, Optional<String> korisnickaRola) throws NeuspjesnaPrijavaException, NeispravnoKorisnickoImeException, NeispravnaLozinkaException {
        if (korisnickoIme.isBlank()){
            String poruka = "Potrebno je unijeti korisničko ime.";
            logger.error(poruka);
            throw new NeispravnoKorisnickoImeException(poruka);
        }
        if(lozinka.isBlank()){
            String poruka = "Morate unijeti lozinku.";
            logger.error(poruka);
            throw new NeispravnaLozinkaException(poruka);
        }
        if(korisnickaRola.isEmpty()){
            String poruka = "Morate odabrati korisničku ulogu.";
            logger.error(poruka);
            throw new NeuspjesnaPrijavaException(poruka);
        }
        List<KorisnickiPodatci> listaKorisnika = Datoteke.ucitajDatotekuKorisnika();
        Optional<KorisnickiPodatci> korisnickaUloga = listaKorisnika.stream()
                .filter(s -> {
                    try {
                        return s.getKorisnickoIme().equalsIgnoreCase(korisnickoIme)
                                && s.getHashLozinka().equals(HashLozinka.hashPassword(lozinka))
                                && (korisnickaRola.get().equals("korisnik") ? s instanceof Korisnik : s instanceof Admin);
                    } catch (NoSuchAlgorithmException e) {
                        logger.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                })
                .findFirst();
        if (korisnickaUloga.isPresent()) {
            Thread thread = new Thread(() -> Datoteke.azurirajTrenutnogKorisnika(korisnickaUloga.get(), korisnickaRola.get()));
            thread.start();
            return;
        }
        String poruka = "Pogrešno korisničko ime, lozinka ili uloga.";
        logger.error(poruka);
        throw new NeuspjesnaPrijavaException(poruka);
    }
}
