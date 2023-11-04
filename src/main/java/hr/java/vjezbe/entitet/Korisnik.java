package hr.java.vjezbe.entitet;


import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Optional;

@Getter
@Setter
public final class Korisnik extends KorisnickiPodatci {
    @Serial
    private static final long serialVersionUID = -2380188583002639154L;
    private long id;
    private Osoba osoba;
    private Lokacija lokacija;
    private String email;

    private Korisnik(Long id, Osoba osoba, Lokacija lokacija, String korisnickoIme, String email, String hashLozinka) {
        super(korisnickoIme, hashLozinka);
        this.id = id;
        this.osoba = osoba;
        this.lokacija = lokacija;
        this.email = email;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Korisnik k) {
        if(Optional.ofNullable(k.getKorisnickoIme()).isPresent() || Optional.ofNullable(this.getKorisnickoIme()).isPresent()){
            if(!(Optional.ofNullable(k.getKorisnickoIme()).isPresent() && Optional.ofNullable(this.getKorisnickoIme()).isPresent())){
                return false;
            }
        }
        if(Optional.ofNullable(k.getOsoba()).isPresent() || Optional.ofNullable(this.getOsoba()).isPresent()){
            if(!(Optional.ofNullable(k.getOsoba()).isPresent() && Optional.ofNullable(this.getOsoba()).isPresent())){
                return false;
            }
        }
        if(Optional.ofNullable(k.getEmail()).isPresent() || Optional.ofNullable(this.getEmail()).isPresent()){
            if(!(Optional.ofNullable(k.getEmail()).isPresent() && Optional.ofNullable(this.getEmail()).isPresent())){
                return false;
            }
        }
        if(Optional.ofNullable(k.getLokacija()).isPresent() || Optional.ofNullable(this.getLokacija()).isPresent()){
            if(!(Optional.ofNullable(k.getLokacija()).isPresent() && Optional.ofNullable(this.getLokacija()).isPresent())){
                return false;
            }
        }
        return k.getId() == this.id &&
                k.getKorisnickoIme().equalsIgnoreCase(this.getKorisnickoIme()) &&
                k.getLokacija().equals(this.lokacija) &&
                k.getEmail().equalsIgnoreCase(this.email) &&
                k.getOsoba().equals(this.getOsoba());
    }

    @Override
    public String toString() {
        return super.getKorisnickoIme() + " " + osoba.getIme() + " " + osoba.getPrezime() + " " + lokacija.getImeGrada() + " " + email;
    }
    public String tablicniString(){
        StringBuilder string = new StringBuilder();
        if(Optional.ofNullable(super.getKorisnickoIme()).isPresent()){
            string.append(super.getKorisnickoIme()).append("\n");
        }
        if(Optional.ofNullable(osoba).isPresent()){
            string.append(osoba).append("\n");
        }
        if(Optional.ofNullable(lokacija).isPresent()){
            string.append(lokacija).append("\n");
        }
        if(Optional.ofNullable(email).isPresent()){
            string.append(email).append("\n");
        }
        if(string.length()>0){
            string.deleteCharAt(string.length()-1);
        }
        return string.toString();
    }
    public static class KorisnikBuilder {
        private Lokacija lokacija;
        private String korisnickoIme;
        private String email;
        private String hashLozinka;
        private Osoba osoba;
        private long id;

        public KorisnikBuilder setLokacija(Lokacija lokacija) {
            this.lokacija = lokacija;
            return this;
        }

        public KorisnikBuilder setKorisnickoIme(String korisnickoIme) {
            this.korisnickoIme = korisnickoIme;
            return this;
        }

        public KorisnikBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public KorisnikBuilder setHashLozinka(String hashLozinka) {
            this.hashLozinka = hashLozinka;
            return this;
        }
        public KorisnikBuilder setOsoba(Osoba osoba) {
            this.osoba = osoba;
            return this;
        }
        public KorisnikBuilder setId(Long id){
            this.id = id;
            return this;
        }

        public Korisnik createKorisnik() {
            return new Korisnik(id, osoba, lokacija, korisnickoIme, email, hashLozinka);
        }
    }
}