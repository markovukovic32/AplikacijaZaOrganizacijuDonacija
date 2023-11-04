package util;

import hr.java.vjezbe.baza.BazaPodataka;
import hr.java.vjezbe.entitet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Datoteke {
    private static final Logger logger = LoggerFactory.getLogger(Datoteke.class);
    public static final String SVI_KORISNICI_DATOTEKA = "dat\\korisnici.txt";
    public static final String TRENUTNI_KORISNIK_DATOTEKA = "dat\\trenutniKorisnik.txt";
    public static final String SERIJALIZACIJA_PROMJENA_DATOTEKA = "dat\\serijalizacija.ser";
    public static List<KorisnickiPodatci> ucitajDatotekuKorisnika() {
        List<KorisnickiPodatci> listaKorisnika = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(SVI_KORISNICI_DATOTEKA))) {
            while (true) {
                Optional<String> korisnickoIme = Optional.ofNullable(in.readLine());
                Optional<String> hashLozinka = Optional.ofNullable(in.readLine());
                Optional<String> korisnickaUloga = Optional.ofNullable(in.readLine());

                if (korisnickoIme.isEmpty() || hashLozinka.isEmpty() || korisnickaUloga.isEmpty())
                    break;

                if(korisnickaUloga.get().equalsIgnoreCase("admin"))
                    listaKorisnika.add(new Admin.AdminBuilder().setKorisnickoIme(korisnickoIme.get()).setHashLozinka(hashLozinka.get()).createAdmin());

                else
                    listaKorisnika.add(new Korisnik.KorisnikBuilder().setKorisnickoIme(korisnickoIme.get()).setHashLozinka(hashLozinka.get()).createKorisnik());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return listaKorisnika;
    }
    public synchronized static void azurirajTrenutnogKorisnika(KorisnickiPodatci korisnik, String korisnickaUloga){
        try (BufferedWriter in = new BufferedWriter(new FileWriter(TRENUTNI_KORISNIK_DATOTEKA))) {
            in.write(korisnik.getKorisnickoIme() + "\n");
            in.write(korisnik.getHashLozinka() + "\n");
            in.write(korisnickaUloga + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized static KorisnickiPodatci dohvatiTrenutnogKorisnika() {
        try (BufferedReader in = new BufferedReader(new FileReader(TRENUTNI_KORISNIK_DATOTEKA))) {
            Optional<String> korisnickoIme = Optional.ofNullable(in.readLine());
            Optional<String> hashLozinka = Optional.ofNullable(in.readLine());
            Optional<String> korisnickaUloga = Optional.ofNullable(in.readLine());
            if (korisnickoIme.isPresent() && hashLozinka.isPresent() && korisnickaUloga.isPresent()) {
                if (korisnickaUloga.get().equalsIgnoreCase("admin")) {
                    return BazaPodataka.dohvatiAdminaIzBazePodataka();
                } else if (korisnickaUloga.get().equalsIgnoreCase("korisnik")) {
                    return BazaPodataka.dohvatiKorisnikePremaKriterijima(new Korisnik.KorisnikBuilder().setKorisnickoIme(korisnickoIme.get()).setHashLozinka(hashLozinka.get()).createKorisnik()).stream().findFirst().get();
                }
            } else {
                throw new RuntimeException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public static void dodajKorisnikaUDatoteku(Korisnik korisnik){
        List<KorisnickiPodatci> listaKorisnika = ucitajDatotekuKorisnika();
        listaKorisnika.add(new KorisnickiPodatci(korisnik.getKorisnickoIme(), korisnik.getHashLozinka()));
        azurirajDatotekuKorisnika(listaKorisnika);
    }

    private static void azurirajDatotekuKorisnika(List<KorisnickiPodatci> listaKorisnika) {
        try (BufferedWriter in = new BufferedWriter(new FileWriter(SVI_KORISNICI_DATOTEKA))) {
            for(KorisnickiPodatci korisnickiPodatci : listaKorisnika){
                in.write(korisnickiPodatci.getKorisnickoIme() + "\n");
                in.write(korisnickiPodatci.getHashLozinka() + "\n");
                in.write(korisnickiPodatci instanceof Admin ? "admin\n" : "korisnik\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void obrisiKorisnikaIzDatoteke(Korisnik korisnik) {
        List<KorisnickiPodatci> listaKorisnika = ucitajDatotekuKorisnika().stream()
                .filter(s -> !(s.getKorisnickoIme().equalsIgnoreCase(korisnik.getKorisnickoIme()))).toList();
        try (BufferedWriter in = new BufferedWriter(new FileWriter(SVI_KORISNICI_DATOTEKA))) {
            for(KorisnickiPodatci korisnickiPodatci : listaKorisnika){
                in.write(korisnickiPodatci.getKorisnickoIme() + "\n");
                in.write(korisnickiPodatci.getHashLozinka() + "\n");
                in.write(korisnickiPodatci instanceof Admin ? "admin\n" : "korisnik\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void izmjeniKorisnikaIzDatoteke(Korisnik korisnik, Korisnik noviKorisnik) {
        List<KorisnickiPodatci> listaKorisnika = ucitajDatotekuKorisnika().stream()
                .map(k -> k.getKorisnickoIme().equalsIgnoreCase(korisnik.getKorisnickoIme())
                        ? noviKorisnik
                        : k)
                .collect(Collectors.toList());
        azurirajDatotekuKorisnika(listaKorisnika);
    }
    public static <T extends Serializable, Z extends KorisnickiPodatci> void serijalizirajPromjenu(PromjenaPodataka<T, Z> promjenaPodataka){
        List<PromjenaPodataka<T,Z>> lista = deserijalizirajPromjene();
        lista.add(promjenaPodataka);
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(SERIJALIZACIJA_PROMJENA_DATOTEKA))) {
            for (PromjenaPodataka<T, Z> tzPromjenaPodataka : lista) {
                out.writeObject(tzPromjenaPodataka);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    public static <T extends Serializable,Z extends KorisnickiPodatci> List<PromjenaPodataka<T,Z>> deserijalizirajPromjene(){
        List<PromjenaPodataka<T,Z>> listaPromjena = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(SERIJALIZACIJA_PROMJENA_DATOTEKA);
            ObjectInputStream iis = new ObjectInputStream(fis)) {
            while (true) {
                listaPromjena.add((PromjenaPodataka<T, Z>) iis.readObject());
            }
        }
        catch (EOFException e){
            return listaPromjena;
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return listaPromjena;
    }
}