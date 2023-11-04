package controlleri;

import hr.java.vjezbe.entitet.KorisnickiPodatci;
import hr.java.vjezbe.entitet.Korisnik;
import hr.java.vjezbe.iznimke.UcitavanjeFxmlaException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import util.Datoteke;
import util.FXUtil;
import util.ZasloniAplikacije;

public class IzbornikController {
    @FXML
    MenuBar menuBar;
    public void initialize(){
        KorisnickiPodatci korisnik = Datoteke.dohvatiTrenutnogKorisnika();
        if(korisnik instanceof Korisnik){
            menuBar.getMenus().get(0).setVisible(false);
        }
    }
    private void prikaziZaslon(ZasloniAplikacije zasloniAplikacije){
        try{
            Glavna.pokaziZaslon(zasloniAplikacije);
        }
        catch (UcitavanjeFxmlaException e){
            FXUtil.pokaziAlert(Alert.AlertType.ERROR, "Učitavanje zaslona", "Pogreška prilikom učitavanja fxml datoteke", "Neuspješno učitavanje fxml datoteke.");
        }
    }
    public void prikaziZaslonZaPretraguKorisnika(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ADMINA_ZA_PRETRAGU_I_BRISANJE_KORISNIKA);
    }
    public void prikaziZaslonZaPretraguPrimateljaDonacija(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_PRETRAGU_I_BRISANJE_PRIMATELJA_DONACIJE);
    }
    public void prikaziZaslonZaPretraguDonora(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_PRETRAGU_I_BRISANJE_DONORA);
    }
    public void prikaziZaslonZaPretraguDonacija(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_PRETRAGU_I_BRISANJE_DONACIJA);
    }
    public void prikaziZaslonZaPretraguPredmetaDoniranja(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_PRETRAGU_I_BRISANJE_PREDMETA_DONIRANJA);
    }
    public void prikaziZaslonZaUnosKorisnika(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ADMINA_ZA_UNOS_KORISNIKA);
    }
    public void prikaziZaslonZaUnosPrimateljaDonacija(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_UNOS_PRIMATELJA_DONACIJA);
    }
    public void prikaziZaslonZaUnosDonora(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_UNOS_DONORA);
    }
    public void prikaziZaslonZaUnosDonacija(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_UNOS_DONACIJA);
    }
    public void prikaziZaslonZaUnosPredmetaDoniranja(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_UNOS_PREDMETA_DONIRANJA);
    }
    public void prikaziZaslonZaIzmjenuKorisnika(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ADMINA_ZA_IZMJENU_KORISNIKA);
    }
    public void prikaziZaslonZaIzmjenuPrimateljaDonacija(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_IZMJENU_PRIMATELJA_DONACIJA);
    }
    public void prikaziZaslonZaIzmjenuDonora(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_IZMJENU_DONORA);
    }
    public void prikaziZaslonZaIzmjenuDonacije(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_IZMJENU_DONACIJA);
    }
    public void prikaziZaslonZaIzmjenuPredmetaDoniranja(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_ZA_IZMJENU_PREDMETA_DONIRANJA);
    }
    public void prikaziZaslonSPopisomIzmjenjenihPodataka(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_S_POPISOM_PROMJENA);
    }
    public void prikaziZaslonPrijave(){
        prikaziZaslon(ZasloniAplikacije.ZASLON_LOGIN);
    }
}
