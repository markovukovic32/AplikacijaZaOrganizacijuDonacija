package hr.java.vjezbe.entitet;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


@Getter
@Setter
public class Donacija extends Entitet{
    @Serial
    private static final long serialVersionUID = 7145696815665550738L;
    private PredmetDoniranja predmetDoniranja;
    private Donor donor;
    private PrimateljDonacije primateljDonacije;
    private LocalDateTime vrijemeDonacije;
    private Donacija(Long id, PredmetDoniranja predmetDoniranja, Donor donor, PrimateljDonacije primateljDonacije, LocalDateTime vrijemeDonacije) {
        super(id);
        this.predmetDoniranja = predmetDoniranja;
        this.donor = donor;
        this.primateljDonacije = primateljDonacije;
        this.vrijemeDonacije = vrijemeDonacije;
    }

    @Override
    public String toString() {
        return "Predmet doniranja = " + predmetDoniranja.getOpisPredmeta() +
                ", donor = " + donor.getImeIliImeTvrtke() + " " + donor.getPrezimeIliOIBTvrtke() +
                ", primatelj donacije = " + primateljDonacije.getImeIliImeTvrtke() + " " + primateljDonacije.getPrezimeIliOIBTvrtke() +
                ", vrijeme donacije = " + vrijemeDonacije.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Donacija donacija = (Donacija) o;
        return predmetDoniranja.equals(donacija.predmetDoniranja) && donor.equals(donacija.donor) && primateljDonacije.equals(donacija.primateljDonacije) && vrijemeDonacije.equals(donacija.vrijemeDonacije);
    }
    public String tablicniString(){
        StringBuilder string = new StringBuilder();
        if(Optional.ofNullable(predmetDoniranja).isPresent()){
            string.append(predmetDoniranja).append("\n");
        }
        if(Optional.ofNullable(donor).isPresent()){
            string.append(donor).append("\n");
        }
        if(Optional.ofNullable(primateljDonacije).isPresent()){
            string.append(primateljDonacije).append("\n");
        }
        if(Optional.ofNullable(vrijemeDonacije).isPresent()){
            string.append(vrijemeDonacije.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm"))).append("\n");
        }
        if(string.length()>0){
            string.deleteCharAt(string.length()-1);
        }
        return string.toString();
    }
    public static class DonacijaBuilder {
        private Long id;
        private PredmetDoniranja predmetDoniranja;
        private Donor donor;
        private PrimateljDonacije primateljDonacije;
        private LocalDateTime vrijemeDonacije;

        public DonacijaBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public DonacijaBuilder setPredmetDoniranja(PredmetDoniranja predmetDoniranja) {
            this.predmetDoniranja = predmetDoniranja;
            return this;
        }

        public DonacijaBuilder setDonor(Donor donor) {
            this.donor = donor;
            return this;
        }

        public DonacijaBuilder setPrimateljDonacije(PrimateljDonacije primateljDonacije) {
            this.primateljDonacije = primateljDonacije;
            return this;
        }

        public DonacijaBuilder setVrijemeDonacije(LocalDateTime vrijemeDonacije) {
            this.vrijemeDonacije = vrijemeDonacije;
            return this;
        }

        public Donacija createDonacija() {
            return new Donacija(id, predmetDoniranja, donor, primateljDonacije, vrijemeDonacije);
        }
    }
}
