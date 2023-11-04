package hr.java.vjezbe.entitet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



public non-sealed class Skladiste<T extends MarkerSucelje, Z extends PredmetDoniranja> implements Sucelje{
    private Lokacija lokacija;
    private List<T> donoriIPrimateljiSkladista;
    private PredmetiDoniranja<Z> predmetiUSkladistu;
    private Osoba odgovornaOsoba;

    public Skladiste(Lokacija lokacija, List<T> donoriIPrimateljiSkladista, PredmetiDoniranja<Z> predmetiUSkladistu, Osoba odgovornaOsoba) {
        this.lokacija = lokacija;
        this.donoriIPrimateljiSkladista = donoriIPrimateljiSkladista;
        this.predmetiUSkladistu = predmetiUSkladistu;
        this.odgovornaOsoba = odgovornaOsoba;
    }

    public List<T> getDonoriIPrimateljiSkladista() {
        return donoriIPrimateljiSkladista;
    }

    public void setDonoriIPrimateljiSkladista(List<T> donoriIPrimateljiSkladista) {
        this.donoriIPrimateljiSkladista = donoriIPrimateljiSkladista;
    }

    public PredmetiDoniranja<Z> getPredmetiUSkladistu() {
        return predmetiUSkladistu;
    }

    public void setPredmetiUSkladistu(PredmetiDoniranja<Z> predmetiUSkladistu) {
        this.predmetiUSkladistu = predmetiUSkladistu;
    }

    public Lokacija getLokacija() {
        return lokacija;
    }

    public void setLokacija(Lokacija lokacija) {
        this.lokacija = lokacija;
    }

    public Osoba getOdgovornaOsoba() {
        return odgovornaOsoba;
    }

    public void setOdgovornaOsoba(Osoba odgovornaOsoba) {
        this.odgovornaOsoba = odgovornaOsoba;
    }

}