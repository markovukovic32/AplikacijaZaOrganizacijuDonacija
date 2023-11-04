package hr.java.vjezbe.entitet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Osoba implements Serializable {
    @Serial
    private static final long serialVersionUID = 467750773781081405L;
    private String ime;
    private String prezime;

    @Override
    public String toString() {
        return ime + " " + prezime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Osoba osoba = (Osoba) o;
        return Objects.equals(ime, osoba.ime) && Objects.equals(prezime, osoba.prezime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ime, prezime);
    }
    public static class OsobaBuilder {
        private String ime;
        private String prezime;

        public OsobaBuilder setIme(String ime) {
            this.ime = ime;
            return this;
        }

        public OsobaBuilder setPrezime(String prezime) {
            this.prezime = prezime;
            return this;
        }

        public Osoba createOsoba() {
            return new Osoba(ime, prezime);
        }
    }
}