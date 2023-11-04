package util;

import hr.java.vjezbe.entitet.Donor;
import hr.java.vjezbe.entitet.KorisnickiPodatci;
import hr.java.vjezbe.entitet.PrimateljDonacije;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PromjenaPodataka<T extends Serializable, Z extends KorisnickiPodatci> implements Serializable {
    @Serial
    private static final long serialVersionUID = 6916624453202149348L;
    private String promijenjeniPodatak;
    private T staraVrijednost;
    private T novaVrijednost;
    private Z rola;
    private LocalDateTime vrijemePromjene;

    public PromjenaPodataka(String promijenjeniPodatak, T staraVrijednost, T novaVrijednost, Z rola, LocalDateTime vrijemePromjene) {
        this.promijenjeniPodatak = promijenjeniPodatak;
        this.staraVrijednost = staraVrijednost;
        this.novaVrijednost = novaVrijednost;
        this.rola = rola;
        this.vrijemePromjene = vrijemePromjene;
    }

    public String getPromijenjeniPodatak() {
        return promijenjeniPodatak;
    }

    public void setPromijenjeniPodatak(String promijenjeniPodatak) {
        this.promijenjeniPodatak = promijenjeniPodatak;
    }

    public T getStaraVrijednost() {
        return staraVrijednost;
    }

    public void setStaraVrijednost(T staraVrijednost) {
        this.staraVrijednost = staraVrijednost;
    }

    public T getNovaVrijednost() {
        return novaVrijednost;
    }

    public void setNovaVrijednost(T novaVrijednost) {
        this.novaVrijednost = novaVrijednost;
    }

    public Z getRola() {
        return rola;
    }

    public void setRola(Z rola) {
        this.rola = rola;
    }

    public LocalDateTime getVrijemePromjene() {
        return vrijemePromjene;
    }

    public void setVrijemePromjene(LocalDateTime vrijemePromjene) {
        this.vrijemePromjene = vrijemePromjene;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        if (Optional.ofNullable(novaVrijednost).isPresent()) {
            if (novaVrijednost instanceof Donor && staraVrijednost instanceof Donor) {
                string.append(((Donor) novaVrijednost).tablicniString()).append("\n");
            }
            if (novaVrijednost instanceof PrimateljDonacije && staraVrijednost instanceof PrimateljDonacije) {
                string.append(((PrimateljDonacije) novaVrijednost).tablicniString()).append("\n");
            }
        }
        if(Optional.ofNullable(staraVrijednost).isPresent()){
            if(staraVrijednost instanceof PrimateljDonacije){

            }
        }
        return  promijenjeniPodatak + ';' +
                staraVrijednost + ";" +
                novaVrijednost + ";" +
                rola + ";" +
                vrijemePromjene.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + ";";
    }
}
