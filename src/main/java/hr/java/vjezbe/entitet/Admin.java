package hr.java.vjezbe.entitet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public non-sealed class Admin extends KorisnickiPodatci {
    private Osoba odgovornaOsoba;

    private Admin(String korisnickoIme, String hashLozinka, Osoba odgovornaOsoba) {
        super(korisnickoIme,hashLozinka);
        this.odgovornaOsoba = odgovornaOsoba;
    }

    @Override
    public String toString() {
        return "Admin: " + getKorisnickoIme();
    }
    public static class AdminBuilder {
        private String korisnickoIme;
        private String hashLozinka;
        private Osoba odgovornaOsoba;

        public AdminBuilder setKorisnickoIme(String korisnickoIme) {
            this.korisnickoIme = korisnickoIme;
            return this;
        }

        public AdminBuilder setHashLozinka(String hashLozinka) {
            this.hashLozinka = hashLozinka;
            return this;
        }

        public AdminBuilder setOdgovornaOsoba(Osoba odgovornaOsoba) {
            this.odgovornaOsoba = odgovornaOsoba;
            return this;
        }

        public Admin createAdmin() {
            return new Admin(korisnickoIme, hashLozinka, odgovornaOsoba);
        }
    }
}
