package hr.java.vjezbe.entitet;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public sealed class KorisnickiPodatci implements Serializable permits Admin, Korisnik {
    @Serial
    private static final long serialVersionUID = -1127531460236381574L;
    private String korisnickoIme;
    private String hashLozinka;

    public KorisnickiPodatci(String korisnickoIme, String hashLozinka) {
        this.korisnickoIme = korisnickoIme;
        this.hashLozinka = hashLozinka;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KorisnickiPodatci that = (KorisnickiPodatci) o;
        return korisnickoIme.equals(that.korisnickoIme) && hashLozinka.equals(that.hashLozinka);
    }

    @Override
    public int hashCode() {
        return Objects.hash(korisnickoIme, hashLozinka);
    }
}