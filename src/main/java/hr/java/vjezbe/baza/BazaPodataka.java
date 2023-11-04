package hr.java.vjezbe.baza;

import hr.java.vjezbe.entitet.*;
import hr.java.vjezbe.iznimke.BazaPodatakaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BazaPodataka {
    private static final Logger logger = LoggerFactory.getLogger(BazaPodataka.class);
    private static Connection spajanjeNaBazu() throws BazaPodatakaException {
        Properties configuration = new Properties();
        try {
            configuration.load(new FileReader("dat/bazaPodataka.properties"));
        } catch (IOException e) {
            String poruka = "Neuspješno dohvaćanje podataka iz datoteke za pristupanje bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }

        String databaseURL = configuration.getProperty("databaseURL");
        String databaseUsername = configuration.getProperty("databaseUsername");
        String databasePassword = configuration.getProperty("databasePassword");

        try {
            return DriverManager.getConnection(databaseURL, databaseUsername, databasePassword);
        } catch (SQLException e) {
            String poruka = "Neuspješno povezivanje na bazu podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }
    private static List<PredmetDoniranja> dohvatiPredmeteDoniranjaPremaUpitu(String upit) throws BazaPodatakaException {
        List<PredmetDoniranja> predmetiDoniranja = new ArrayList<>();
        try (Connection veza = spajanjeNaBazu()) {
            PreparedStatement stmtPredmet = veza.prepareStatement(upit);
            ResultSet rsPredmetiDoniranja = stmtPredmet.executeQuery();
            while (rsPredmetiDoniranja.next()) {
                String tipPredmeta = rsPredmetiDoniranja.getString("TIP_PREDMETA");
                if(tipPredmeta.equalsIgnoreCase("hrana")) {
                    predmetiDoniranja.add(dohvatiHranuIzResultSeta(rsPredmetiDoniranja));
                }
                else if(tipPredmeta.equalsIgnoreCase("odjeća")){
                    predmetiDoniranja.add(dohvatiOdjecuIzResultSeta(rsPredmetiDoniranja));
                }
            }
        }
        catch(SQLException e){
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return predmetiDoniranja;
    }
    private static List<Korisnik> dohvatiKorisnikePremaUpitu(String upit) throws BazaPodatakaException{
        List<Korisnik> listaKorisnika = new ArrayList<>();
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement statement = veza.prepareStatement(upit);
            ResultSet korisniciRs = statement.executeQuery();
            while(korisniciRs.next()){
                listaKorisnika.add(dohvatiKorisnikeIzResultSeta(korisniciRs));
            }
        }
        catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return listaKorisnika;
    }
    private static List<Donor> dohvatiDonorePremaUpitu(String upit) throws BazaPodatakaException{
        List<Donor> listaDonora = new ArrayList<>();
        try (Connection veza = spajanjeNaBazu()) {
            PreparedStatement stmtDonor = veza.prepareStatement(upit);
            ResultSet rsDonor = stmtDonor.executeQuery();
            while (rsDonor.next()) {
                listaDonora.add(dohvatiDonoraIzResultSeta(rsDonor));
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return listaDonora;
    }
    private static List<PrimateljDonacije> dohvatiPrimateljeDonacijaPremaUpitu(String upit) throws BazaPodatakaException{
        List<PrimateljDonacije> listaPrimatelja = new ArrayList<>();
        try(Connection veza = spajanjeNaBazu()){
            Statement statement = veza.createStatement();
            ResultSet primateljiRs = statement.executeQuery(upit);
            while(primateljiRs.next()){
                listaPrimatelja.add(dohvatiPrimateljaDonacijeIzResultSeta(primateljiRs));
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return listaPrimatelja;
    }
    public static List<PredmetDoniranja> dohvatiSvePredmeteDoniranjaIzBazePodataka() {
        return dohvatiPredmeteDoniranjaPremaUpitu("SELECT * FROM PREDMET_DONIRANJA2");
    }
    public static Hrana dohvatiHranuIzResultSeta(ResultSet rsHrana) throws SQLException {
        Long id = rsHrana.getLong("ID");
        String opisPredmeta = rsHrana.getString("IME_PREDMETA");
        LocalDate rokTrajanja =
                rsHrana.getTimestamp("rok_trajanja").toInstant().atZone(
                        ZoneId.systemDefault()).toLocalDate();
        double kolicinaUKg = rsHrana.getDouble("kolicina_u_kg_ili_l");
        return new Hrana.HranaBuilder().setId(id).setOpisPredmeta(opisPredmeta).setRokTrajanja(rokTrajanja).setKolicinaUKgIliL(kolicinaUKg).createHrana();
    }
    public static Odjeca dohvatiOdjecuIzResultSeta(ResultSet rsOdjeca) throws SQLException {
        Long id = rsOdjeca.getLong("ID");
        String opisPredmeta = rsOdjeca.getString("IME_PREDMETA");
        String stanjeStr = rsOdjeca.getString("STANJE");
        String velicinaStr = rsOdjeca.getString("VELICINA");
        Stanje stanje;
        if (stanjeStr.equalsIgnoreCase("novo")) {
            stanje = Stanje.NOVO;
        } else {
            stanje = Stanje.RABLJENO;
        }
        Velicina velicina = switch (velicinaStr) {
            case "XS" -> Velicina.XS;
            case "S" -> Velicina.S;
            case "M" -> Velicina.M;
            case "L" -> Velicina.L;
            case "XL" -> Velicina.XL;
            default -> Velicina.XXL;
        };
        return new Odjeca.OdjecaBuilder().setId(id).setOpisPredmeta(opisPredmeta).setStanje(stanje).setVelicina(velicina).createOdjeca();
    }
    public static List<Donor> dohvatiSveDonoreIzBazePodataka(){
        return dohvatiDonorePremaUpitu("SELECT * FROM DONOR");
    }
    public static Donor dohvatiDonoraIzResultSeta(ResultSet rsDonor) throws SQLException{
        long id = rsDonor.getLong("ID");
        String opisDonora = rsDonor.getString("OPIS_SUDIONIKA");
        String lokacijaStr = rsDonor.getString("LOKACIJA");
        Lokacija lokacija = dohvatiLokaciju(lokacijaStr);
        String imeIliImeTvrtke = rsDonor.getString("IME");
        String prezimeIliOIBTvrtke = rsDonor.getString("PREZIME");
        List<PredmetDoniranja> doniraniPredmeti = new ArrayList<>();
        List<PredmetDoniranja> ponudjeniPredmeti = new ArrayList<>();
        try(Connection veza = spajanjeNaBazu()) {
            Statement stmtDonPredmeti = veza.createStatement();
            ResultSet rsDonorDoniraniPredmeti = stmtDonPredmeti.executeQuery("SELECT * FROM DONOR_DONIRANI_PREDMETI WHERE DONOR_ID = " + id);
            while (rsDonorDoniraniPredmeti.next()) {
                long idPredmeta = rsDonorDoniraniPredmeti.getLong("DONIRANI_PREDMET_ID");
                doniraniPredmeti.addAll(dohvatiPredmeteDoniranjaPremaUpitu("SELECT * FROM PREDMET_DONIRANJA2 WHERE ID = " + idPredmeta));
            }
            Statement stmtPonPredmeti = veza.createStatement();
            ResultSet rsDonorPonudjeniPredmeti = stmtPonPredmeti.executeQuery("SELECT * FROM DONOR_PONUDJENI_PREDMETI WHERE DONOR_ID = " + id);
            while (rsDonorPonudjeniPredmeti.next()) {
                long idPredmeta = rsDonorPonudjeniPredmeti.getLong("PONUDJENI_PREDMET_ID");
                ponudjeniPredmeti.addAll(dohvatiPredmeteDoniranjaPremaUpitu("SELECT * FROM PREDMET_DONIRANJA2 WHERE ID = " + idPredmeta));
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return new Donor.DonorBuilder().setId(id).setImeIliImeTvrtke(imeIliImeTvrtke).setPrezimeIliOIBTvrtke(prezimeIliOIBTvrtke).setOpisDonora(opisDonora).setLokacija(lokacija).setListaDoniranihPredmeta(new PredmetiDoniranja<>(doniraniPredmeti)).setListaPonudjenihPredmeta(new PredmetiDoniranja<>(ponudjeniPredmeti)).createDonor();
    }
    public static void dodajKorisnikaUBazuPodataka(Korisnik korisnik) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement stmt = veza.prepareStatement("INSERT INTO KORISNIK (KORISNICKO_IME, IME, PREZIME, HASH_LOZINKA, LOKACIJA, EMAIL) VALUES(?, ?, ?, ?, ?, ?)");
            stmt.setString(1, korisnik.getKorisnickoIme());
            stmt.setString(2, korisnik.getOsoba().getIme());
            stmt.setString(3, korisnik.getOsoba().getPrezime());
            stmt.setString(4, korisnik.getHashLozinka());
            stmt.setString(5, korisnik.getLokacija().getImeGrada());
            stmt.setString(6, korisnik.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspjeh prilikom dodavanja podataka u bazu podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }
    public static List<Donacija> dohvatiSveDonacijeIzBazePodataka() throws BazaPodatakaException{
        List<Donacija> listaDonacija = new ArrayList<>();
        try (Connection veza = spajanjeNaBazu()) {
            Statement stmt = veza.createStatement();
            ResultSet rsDonacija = stmt.executeQuery("SELECT * FROM DONACIJA");
            while (rsDonacija.next()) {
                listaDonacija.add(dohvatiDonacijuIzResultSeta(rsDonacija));
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return listaDonacija;
    }

    private static Donacija dohvatiDonacijuIzResultSeta(ResultSet rsDonacija) throws SQLException {
        Long id = rsDonacija.getLong("id");
        long idPredmeta = rsDonacija.getLong("predmet_id");
        PredmetDoniranja predmetDoniranja = dohvatiPredmeteDoniranjaPremaKriterijima(new PredmetDoniranja.PredmetDoniranjaBuilder().setId(idPredmeta).createPredmetDoniranja()).stream().findFirst().get();
        Long idDonora = rsDonacija.getLong("donor_id");
        Donor donor = dohvatiDonorePremaKriterijima(new Donor.DonorBuilder().setId(idDonora).createDonor()).stream().findFirst().get();
        Long idPrimatelja = rsDonacija.getLong("primatelj_id");
        PrimateljDonacije primateljDonacije = dohvatiPrimateljeDonacijaPremaKriterijima(new PrimateljDonacije.PrimateljDonacijeBuilder().setId(idPrimatelja).createPrimateljDonacije()).stream().findFirst().get();
        LocalDateTime datumIVrijemeDonacije = rsDonacija.getTimestamp("datum_i_vrijeme").toLocalDateTime();
        return new Donacija.DonacijaBuilder().setId(id).setPredmetDoniranja(predmetDoniranja).setDonor(donor).setPrimateljDonacije(primateljDonacije).setVrijemeDonacije(datumIVrijemeDonacije).createDonacija();
    }
    public static List<PredmetDoniranja> dohvatiPredmeteDoniranjaPremaKriterijima(PredmetDoniranja predmetDoniranja) throws BazaPodatakaException{
        StringBuilder sqlUpit = new StringBuilder(
                "SELECT * FROM PREDMET_DONIRANJA2 WHERE 1 = 1");
        if (Optional.ofNullable(predmetDoniranja).isPresent()) {
            if (Optional.of(predmetDoniranja).map(
                    PredmetDoniranja::getId).isPresent()) {
                sqlUpit.append(" AND ID = ").append(predmetDoniranja.getId());
            }
            if (!Optional.ofNullable(predmetDoniranja.getOpisPredmeta()).map(
                    String::isBlank).orElse(true)) {
                sqlUpit.append(" AND IME_PREDMETA LIKE '%").append(predmetDoniranja.getOpisPredmeta()).append("%'");
            }
        }
        return dohvatiPredmeteDoniranjaPremaUpitu(sqlUpit.toString());
    }
    public static List<PredmetDoniranja> dohvatiHranuPremaKriterijima(Hrana hrana) throws BazaPodatakaException{
        StringBuilder sqlUpit = new StringBuilder(
                "SELECT * FROM PREDMET_DONIRANJA2 WHERE 1 = 1");
        if (Optional.ofNullable(hrana).isPresent()) {
            if (Optional.of(hrana).map(
                    PredmetDoniranja::getId).isPresent()) {
                sqlUpit.append(" AND ID = ").append(hrana.getId());
            }
            if (!Optional.ofNullable(hrana.getOpisPredmeta()).map(
                    String::isBlank).orElse(true)) {
                sqlUpit.append(" AND IME_PREDMETA LIKE '%").append(hrana.getOpisPredmeta()).append("%'");
            }
            sqlUpit.append(" AND TIP_PREDMETA LIKE '%hrana%'");
            if (Optional.ofNullable(hrana.getRokTrajanja()).isPresent()) {
                sqlUpit.append(" AND ROK_TRAJANJA = '").append(hrana.getRokTrajanja().format(
                        DateTimeFormatter.ISO_DATE)).append("'");
            }
            if (Optional.ofNullable(hrana.getKolicinaUKgIliL()).isPresent()) {
                if(!hrana.getKolicinaUKgIliL().equals(0.0)){
                    sqlUpit.append(" AND KOLICINA_U_KG_ILI_L = ").append(hrana.getKolicinaUKgIliL());
                }
            }
        }
        return dohvatiPredmeteDoniranjaPremaUpitu(sqlUpit.toString());
    }
    public static List<PredmetDoniranja> dohvatiOdjecuPremaKriterijima(Odjeca odjeca) throws BazaPodatakaException{
        StringBuilder sqlUpit = new StringBuilder(
                "SELECT * FROM PREDMET_DONIRANJA2 WHERE 1 = 1");
        if (Optional.ofNullable(odjeca).isPresent()) {
            if (Optional.of(odjeca).map(
                    PredmetDoniranja::getId).isPresent()) {
                sqlUpit.append(" AND ID = ").append(odjeca.getId());
            }
            if (!Optional.ofNullable(odjeca.getOpisPredmeta()).map(
                    String::isBlank).orElse(true)) {
                sqlUpit.append(" AND IME_PREDMETA LIKE '%").append(odjeca.getOpisPredmeta()).append("%'");
            }
            sqlUpit.append(" AND TIP_PREDMETA LIKE '%odjeća%'");
            if (Optional.ofNullable(odjeca.getVelicina()).isPresent()) {
                sqlUpit.append(" AND VELICINA LIKE '%").append(odjeca.getVelicina().getVelicina()).append("%'");
            }
            if (Optional.ofNullable(odjeca.getStanje()).isPresent()) {
                sqlUpit.append(" AND STANJE LIKE '%").append(odjeca.getStanje().getStanje()).append("%'");
            }
        }
        return dohvatiPredmeteDoniranjaPremaUpitu(sqlUpit.toString());
    }
    public static void dodajDonacijuUBazuPodataka(Donacija donacija, AtomicBoolean donorBool, AtomicBoolean primateljBool) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza
                    .prepareStatement(
                            "INSERT INTO DONACIJA(predmet_id, donor_id, primatelj_id, datum_i_vrijeme) VALUES (?, ?, ?, ?)");
            preparedStatement.setLong(1, donacija.getPredmetDoniranja().getId());
            preparedStatement.setLong(2, donacija.getDonor().getId());
            preparedStatement.setLong(3,donacija.getPrimateljDonacije().getId());
            preparedStatement.setTimestamp(4,
                    Timestamp.valueOf(donacija.getVrijemeDonacije()));
            preparedStatement.executeUpdate();

            PreparedStatement preparedStatement2 = veza.prepareStatement("INSERT INTO DONOR_DONIRANI_PREDMETI (donor_id, donirani_predmet_id) VALUES (?,?)");
            preparedStatement2.setLong(1,donacija.getDonor().getId());
            preparedStatement2.setLong(2,donacija.getPredmetDoniranja().getId());
            preparedStatement2.executeUpdate();

            PreparedStatement preparedStatement3 = veza.prepareStatement("INSERT INTO PRIMATELJ_PRIMLJENI_PREDMETI (primatelj_id, primljeni_predmet_id) VALUES (?,?)");
            preparedStatement3.setLong(1,donacija.getPrimateljDonacije().getId());
            preparedStatement3.setLong(2,donacija.getPredmetDoniranja().getId());
            preparedStatement3.executeUpdate();

            if(donorBool.get()){
                PreparedStatement preparedStatement4 = veza.prepareStatement("DELETE FROM DONOR_PONUDJENI_PREDMETI WHERE DONOR_ID = ?");
                preparedStatement4.setLong(1,donacija.getDonor().getId());
                preparedStatement4.executeUpdate();
            }
            if(primateljBool.get()){
                PreparedStatement preparedStatement5 = veza.prepareStatement("DELETE FROM PRIMATELJ_POTREBNI_PREDMETI WHERE PRIMATELJ_ID = ?");
                preparedStatement5.setLong(1,donacija.getPrimateljDonacije().getId());
                preparedStatement5.executeUpdate();
            }
        } catch (SQLException e) {
            String poruka = "Neuspjeh prilikom dodavanja podataka u bazu podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }
    public static List<Korisnik> dohvatiKorisnikePremaKriterijima(Korisnik korisnik) throws BazaPodatakaException {
        StringBuilder sqlUpit = new StringBuilder(
                "SELECT * FROM KORISNIK WHERE 1 = 1");
        if (Optional.ofNullable(korisnik).isPresent()) {
            if (!Optional.ofNullable(korisnik.getKorisnickoIme()).map(
                    String::isBlank).orElse(true)) {
                sqlUpit.append(" AND KORISNICKO_IME LIKE '%").append(korisnik.getKorisnickoIme()).append("%'");
            }
            if (!Optional.ofNullable(korisnik.getOsoba()).map(
                    s -> s.getIme().isBlank()).orElse(true)) {
                sqlUpit.append(" AND IME LIKE '%").append(korisnik.getOsoba().getIme()).append("%'");
            }
            if (!Optional.ofNullable(korisnik.getOsoba()).map(
                    s -> s.getPrezime().isBlank()).orElse(true)) {
                sqlUpit.append(" AND PREZIME LIKE '%").append(korisnik.getOsoba().getPrezime()).append("%'");
            }
            if (!Optional.ofNullable(korisnik.getLokacija()).map(
                    s -> s.getImeGrada().isBlank()).orElse(true)) {
                sqlUpit.append(" AND LOKACIJA LIKE '%").append(korisnik.getLokacija().getImeGrada()).append("%'");
            }
            if (!Optional.ofNullable(korisnik.getEmail()).map(
                    String::isBlank).orElse(true)) {
                sqlUpit.append(" AND EMAIL LIKE '%").append(korisnik.getEmail()).append("%'");
            }
        }
        return dohvatiKorisnikePremaUpitu(sqlUpit.toString());
    }

    private static Korisnik dohvatiKorisnikeIzResultSeta(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("ID");
        String korisnickoIme = resultSet.getString("KORISNICKO_IME");
        String ime = resultSet.getString("IME");
        String prezime = resultSet.getString("PREZIME");
        Osoba osoba = new Osoba.OsobaBuilder().setIme(ime).setPrezime(prezime).createOsoba();
        String hashLozinka = resultSet.getString("HASH_LOZINKA");
        String lokacijaStr = resultSet.getString("LOKACIJA");
        Lokacija lokacija = dohvatiLokaciju(lokacijaStr);
        String email = resultSet.getString("EMAIL");
        return new Korisnik.KorisnikBuilder().setId(id).setKorisnickoIme(korisnickoIme).setOsoba(osoba).setEmail(email).setHashLozinka(hashLozinka).setLokacija(lokacija).createKorisnik();
    }

    public static Admin dohvatiAdminaIzBazePodataka() throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            Statement statement = veza.createStatement();
            ResultSet adminRs = statement.executeQuery("SELECT * FROM ADMIN");
            if(adminRs.next()){
                String korisnickoIme = adminRs.getString("KORISNICKO_IME");
                String ime = adminRs.getString("IME");
                String prezime = adminRs.getString("PREZIME");
                Osoba osoba = new Osoba.OsobaBuilder().setIme(ime).setPrezime(prezime).createOsoba();
                String hashLozinka = adminRs.getString("HASH_LOZINKA");
                return new Admin.AdminBuilder().setKorisnickoIme(korisnickoIme).setHashLozinka(hashLozinka).setOdgovornaOsoba(osoba).createAdmin();
            }
        }
        catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return new Admin.AdminBuilder().createAdmin();
    }

    public static List<Korisnik> dohvatiSveKorisnikeIzBazePodataka() throws BazaPodatakaException{
        return dohvatiKorisnikePremaUpitu("SELECT * FROM KORISNIK");
    }
    public static List<PrimateljDonacije> dohvatiSvePrimateljeDonacijaIzBazePodataka() throws BazaPodatakaException{
        return dohvatiPrimateljeDonacijaPremaUpitu("SELECT * FROM PRIMATELJ_DONACIJE");
    }
    public static PrimateljDonacije dohvatiPrimateljaDonacijeIzResultSeta(ResultSet rsPrimatelj) throws SQLException, BazaPodatakaException {
        long id = rsPrimatelj.getLong("ID");
        String opisPrimatelja = rsPrimatelj.getString("OPIS_SUDIONIKA");
        String lokacijaStr = rsPrimatelj.getString("LOKACIJA");
        Lokacija lokacija = dohvatiLokaciju(lokacijaStr);
        String imeIliImeTvrtke = rsPrimatelj.getString("IME");
        String prezimeIliOIBTvrtke = rsPrimatelj.getString("PREZIME");
        List<PredmetDoniranja> primljeniPredmeti = new ArrayList<>();
        List<PredmetDoniranja> potrebniPredmeti = new ArrayList<>();
        try (Connection veza = spajanjeNaBazu()) {
            Statement stmtPrimljeniPredmeti = veza.createStatement();
            ResultSet rsPrimateljPrimljeniPredmeti = stmtPrimljeniPredmeti.executeQuery("SELECT * FROM PRIMATELJ_PRIMLJENI_PREDMETI WHERE PRIMATELJ_ID = " + id);
            while (rsPrimateljPrimljeniPredmeti.next()) {
                long idPredmeta = rsPrimateljPrimljeniPredmeti.getLong("PRIMLJENI_PREDMET_ID");
                primljeniPredmeti.addAll(dohvatiPredmeteDoniranjaPremaUpitu("SELECT * FROM PREDMET_DONIRANJA2 WHERE ID = " + idPredmeta));
            }
            Statement stmtPotrebniPredmeti = veza.createStatement();
            ResultSet rsPotrebniPredmetiPoPrimatelju = stmtPotrebniPredmeti.executeQuery("SELECT * FROM PRIMATELJ_POTREBNI_PREDMETI WHERE PRIMATELJ_ID = " + id);
            while (rsPotrebniPredmetiPoPrimatelju.next()) {
                long idPredmeta = rsPotrebniPredmetiPoPrimatelju.getLong("POTREBAN_PREDMET_ID");
                potrebniPredmeti.addAll(dohvatiPredmeteDoniranjaPremaUpitu("SELECT * FROM PREDMET_DONIRANJA2 WHERE ID = " + idPredmeta));
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return new PrimateljDonacije.PrimateljDonacijeBuilder().setId(id).setOpisPrimatelja(opisPrimatelja).setImeIliImeTvrtke(imeIliImeTvrtke).setPrezimeIliOIBTvrtke(prezimeIliOIBTvrtke).setLokacija(lokacija).setImeIliImeTvrtke(imeIliImeTvrtke).setPrezimeIliOIBTvrtke(prezimeIliOIBTvrtke).setListaPrimljenihPredmeta(new PredmetiDoniranja<>(primljeniPredmeti)).setListaPotrebnihPredmeta(new PredmetiDoniranja<>(potrebniPredmeti)).createPrimateljDonacije();
    }
    public static List<Donor> dohvatiDonorePremaKriterijima(Donor donor) throws BazaPodatakaException {
        StringBuilder sqlUpit = new StringBuilder(
                "SELECT * FROM DONOR WHERE 1 = 1");
        if (Optional.ofNullable(donor).isPresent()) {
            if (Optional.of(donor).map(
                    Donor::getId).isPresent()) {
                sqlUpit.append(" AND ID = ").append(donor.getId());
            }
            if (!Optional.ofNullable(donor.getOpisDonora()).map(
                    String::isBlank).orElse(true)) {
                sqlUpit.append(" AND OPIS_SUDIONIKA LIKE '%").append(donor.getOpisDonora()).append("%'");
            }
            if (!Optional.ofNullable(donor.getLokacija()).map(s -> s.getImeGrada().isBlank()).orElse(true)) {
                sqlUpit.append(" AND LOKACIJA LIKE '%").append(donor.getLokacija().getImeGrada()).append("%'");
            }
            if (Optional.ofNullable(donor.getImeIliImeTvrtke()).isPresent())
                sqlUpit.append(" AND IME LIKE '%").append(donor.getImeIliImeTvrtke()).append("%'");
            if (Optional.ofNullable(donor.getPrezimeIliOIBTvrtke()).isPresent()) {
                sqlUpit.append(" AND PREZIME LIKE '%").append(donor.getPrezimeIliOIBTvrtke()).append("%'");
            }
        }
        List<Donor> listaDonora = dohvatiDonorePremaUpitu(sqlUpit.toString());
        if(Optional.ofNullable(donor).isPresent()){
            if(Optional.ofNullable(donor.getListaDoniranihPredmeta()).isPresent()){
                listaDonora = pretraziDonoraPoDoniranomPredmetuDoniranja(listaDonora, donor);
            }
            if(Optional.ofNullable(donor.getListaPonudjenihPredmeta()).isPresent()){
                listaDonora = pretraziDonoraPoPonudjenomPredmetuDoniranja(listaDonora,donor);
            }
        }
        return listaDonora;
    }

    private static List<Donor> pretraziDonoraPoPonudjenomPredmetuDoniranja(List<Donor> listaDonora, Donor donor) throws BazaPodatakaException {
        List<Donor> novaListaDonora = new ArrayList<>();
        try (Connection veza = spajanjeNaBazu()) {
            Statement statement = veza.createStatement();
            for (PredmetDoniranja predmet : donor.getListaPonudjenihPredmeta().dohvatiSvePredmeteDoniranja()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM DONOR_PONUDJENI_PREDMETI WHERE PONUDJENI_PREDMET_ID = " + predmet.getId());
                while(resultSet.next()){
                    long donorId = resultSet.getLong("DONOR_ID");
                    if(listaDonora.stream().anyMatch(s->s.getId().equals(donorId))){
                        novaListaDonora.add(dohvatiDonorePremaKriterijima(new Donor.DonorBuilder().setId(donorId).createDonor()).stream().findFirst().get());
                    }
                }
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return novaListaDonora;
    }

    private static List<Donor> pretraziDonoraPoDoniranomPredmetuDoniranja(List<Donor> listaDonora, Donor donor) throws BazaPodatakaException{
        List<Donor> novaListaDonora = new ArrayList<>();
        try (Connection veza = spajanjeNaBazu()) {
            Statement statement = veza.createStatement();
            for (PredmetDoniranja predmet : donor.getListaDoniranihPredmeta().dohvatiSvePredmeteDoniranja()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM DONOR_DONIRANI_PREDMETI WHERE DONIRANI_PREDMET_ID = " + predmet.getId());
                while(resultSet.next()){
                    long donorId = resultSet.getLong("DONOR_ID");
                    if(listaDonora.stream().anyMatch(s->s.getId().equals(donorId))){
                        novaListaDonora.add(dohvatiDonorePremaKriterijima(new Donor.DonorBuilder().setId(donorId).createDonor()).stream().findFirst().get());
                    }
                }
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return novaListaDonora;
    }

    public static List<PrimateljDonacije> dohvatiPrimateljeDonacijaPremaKriterijima(PrimateljDonacije primateljDonacije) throws BazaPodatakaException{
        StringBuilder sqlUpit = new StringBuilder(
                "SELECT * FROM PRIMATELJ_DONACIJE WHERE 1 = 1");
        if (Optional.ofNullable(primateljDonacije).isPresent()) {
            if (Optional.of(primateljDonacije).map(
                    PrimateljDonacije::getId).isPresent()) {
                sqlUpit.append(" AND ID = ").append(primateljDonacije.getId());
            }
            if (!Optional.ofNullable(primateljDonacije.getOpisPrimatelja()).map(
                    String::isBlank).orElse(true)) {
                sqlUpit.append(" AND OPIS_SUDIONIKA LIKE '%").append(primateljDonacije.getOpisPrimatelja()).append("%'");
            }
            if (!Optional.ofNullable(primateljDonacije.getLokacija()).map(s -> s.getImeGrada().isBlank()).orElse(true)) {
                sqlUpit.append(" AND LOKACIJA LIKE '%").append(primateljDonacije.getLokacija().getImeGrada()).append("%'");
            }
            if (Optional.ofNullable(primateljDonacije.getImeIliImeTvrtke()).isPresent()) {
                sqlUpit.append(" AND IME LIKE '%").append(primateljDonacije.getImeIliImeTvrtke()).append("%'");
            }
            if (Optional.ofNullable(primateljDonacije.getPrezimeIliOIBTvrtke()).isPresent())
                sqlUpit.append(" AND PREZIME LIKE '%").append(primateljDonacije.getPrezimeIliOIBTvrtke()).append("%'");
        }
        List<PrimateljDonacije> listaPrimatelja = new ArrayList<>(dohvatiPrimateljeDonacijaPremaUpitu(sqlUpit.toString()));
        if(Optional.ofNullable(primateljDonacije).isPresent()){
            if(Optional.ofNullable(primateljDonacije.getListaPrimljenihPredmeta()).isPresent()){
                listaPrimatelja = pretraziPrimateljaDonacijePoPrimljenomPredmetuDoniranja(listaPrimatelja, primateljDonacije);
            }
            if(Optional.ofNullable(primateljDonacije.getListaPotrebnihPredmeta()).isPresent()){
                listaPrimatelja = pretraziPrimateljaDonacijePoPotrebnomPredmetuDoniranja(listaPrimatelja,primateljDonacije);
            }
        }
        return listaPrimatelja;
    }

    private static List<PrimateljDonacije> pretraziPrimateljaDonacijePoPrimljenomPredmetuDoniranja(List<PrimateljDonacije> listaPrimatelja, PrimateljDonacije primateljDonacije) throws BazaPodatakaException{
        List<PrimateljDonacije> novaListaPrimatelja = new ArrayList<>();
        try (Connection veza = spajanjeNaBazu()) {
            Statement statement = veza.createStatement();
            for (PredmetDoniranja predmet : primateljDonacije.getListaPrimljenihPredmeta().dohvatiSvePredmeteDoniranja()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM PRIMATELJ_PRIMLJENI_PREDMETI WHERE PRIMLJENI_PREDMET_ID = " + predmet.getId());
                while(resultSet.next()){
                    long primateljId = resultSet.getLong("PRIMATELJ_ID");
                    if(listaPrimatelja.stream().anyMatch(s->s.getId().equals(primateljId))){
                        novaListaPrimatelja.add(dohvatiPrimateljeDonacijaPremaKriterijima(new PrimateljDonacije.PrimateljDonacijeBuilder().setId(primateljId).createPrimateljDonacije()).stream().findFirst().get());
                    }
                }
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return novaListaPrimatelja;
    }
    private static List<PrimateljDonacije> pretraziPrimateljaDonacijePoPotrebnomPredmetuDoniranja(List<PrimateljDonacije> listaPrimatelja, PrimateljDonacije primateljDonacije) throws BazaPodatakaException{
        List<PrimateljDonacije> novaListaPrimatelja = new ArrayList<>();
        try (Connection veza = spajanjeNaBazu()) {
            Statement statement = veza.createStatement();
            for (PredmetDoniranja predmet : primateljDonacije.getListaPotrebnihPredmeta().dohvatiSvePredmeteDoniranja()) {
                ResultSet resultSet = statement.executeQuery("SELECT * FROM PRIMATELJ_POTREBNI_PREDMETI WHERE POTREBAN_PREDMET_ID = " + predmet.getId());
                while(resultSet.next()){
                    long primateljId = resultSet.getLong("PRIMATELJ_ID");
                    if(listaPrimatelja.stream().anyMatch(s->s.getId().equals(primateljId))){
                        novaListaPrimatelja.add(dohvatiPrimateljeDonacijaPremaKriterijima(new PrimateljDonacije.PrimateljDonacijeBuilder().setId(primateljId).createPrimateljDonacije()).stream().findFirst().get());
                    }
                }
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return novaListaPrimatelja;
    }

    public static List<Donacija> dohvatiDonacijePremaKriterijima(Donacija donacija) throws BazaPodatakaException{
        List<Donacija> listaDonacija = new ArrayList<>();
        try(Connection veza = spajanjeNaBazu()) {
            StringBuilder sqlUpit = new StringBuilder(
                    "SELECT * FROM DONACIJA WHERE 1 = 1");
            if (Optional.ofNullable(donacija).isPresent()) {
                if (Optional.of(donacija).map(
                        Donacija::getId).isPresent()) {
                    sqlUpit.append(" AND ID = ").append(donacija.getId());
                }
                if (Optional.ofNullable(donacija.getPredmetDoniranja()).map(
                        PredmetDoniranja::getId).isPresent()) {
                    sqlUpit.append(" AND PREDMET_ID = ").append(donacija.getPredmetDoniranja().getId());
                }
                if (Optional.ofNullable(donacija.getDonor()).map(Donor::getId).isPresent()) {
                    sqlUpit.append(" AND DONOR_ID = ").append(donacija.getDonor().getId());
                }
                if (Optional.ofNullable(donacija.getPrimateljDonacije()).map(PrimateljDonacije::getId).isPresent()) {
                    sqlUpit.append(" AND PRIMATELJ_ID = ").append(donacija.getPrimateljDonacije().getId());
                }
                if (Optional.of(donacija).map(Donacija::getVrijemeDonacije).isPresent()) {
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");
                    sqlUpit.append(" AND DATUM_I_VRIJEME = '").append(donacija.getVrijemeDonacije().format(formatter)).append("'");
                }
            }
            Statement upit = veza.createStatement();
            ResultSet resultSet = upit.executeQuery(sqlUpit.toString());
            while (resultSet.next()) {
                listaDonacija.add(dohvatiDonacijuIzResultSeta(resultSet));
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
        return listaDonacija;
    }

    public static void obrisiKorisnikaIzBazePodataka(Korisnik korisnik) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza.prepareStatement("DELETE FROM KORISNIK WHERE KORISNICKO_IME = ?");
            preparedStatement.setString(1, korisnik.getKorisnickoIme());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno brisanje podataka iz baze podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }
    public static List<PredmetDoniranja> dohvatiSlobodnePredmete() throws BazaPodatakaException{
        return dohvatiPredmeteDoniranjaPremaUpitu("""
                    SELECT *
                    FROM PREDMET_DONIRANJA2 pd
                    LEFT JOIN DONOR_PONUDJENI_PREDMETI dpp ON pd.id = dpp.ponudjeni_predmet_id
                    LEFT JOIN DONOR_DONIRANI_PREDMETI ddp ON pd.id = ddp.donirani_predmet_id
                    LEFT JOIN PRIMATELJ_PRIMLJENI_PREDMETI ppp ON pd.id = ppp.primljeni_predmet_id
                    LEFT JOIN PRIMATELJ_POTREBNI_PREDMETI ppt ON pd.id = ppt.potreban_predmet_id
                    WHERE dpp.donor_id IS NULL AND ddp.donor_id IS NULL
                    AND ppp.primatelj_id IS NULL AND ppt.primatelj_id IS NULL;""");
    }

    public static void dodajPrimateljaDonacijeUBazuPodataka(PrimateljDonacije primateljDonacije) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement stmt = veza.prepareStatement("INSERT INTO PRIMATELJ_DONACIJE (IME, PREZIME, OPIS_SUDIONIKA, LOKACIJA) VALUES(?, ?, ?, ?)");
            stmt.setString(1, primateljDonacije.getImeIliImeTvrtke());
            stmt.setString(2, primateljDonacije.getPrezimeIliOIBTvrtke());
            stmt.setString(3, primateljDonacije.getOpisPrimatelja());
            stmt.setString(4, primateljDonacije.getLokacija().getImeGrada());
            stmt.executeUpdate();
            PrimateljDonacije primateljDonacije2 = new PrimateljDonacije.PrimateljDonacijeBuilder().setImeIliImeTvrtke(primateljDonacije.getImeIliImeTvrtke())
                    .setPrezimeIliOIBTvrtke(primateljDonacije.getPrezimeIliOIBTvrtke())
                    .setLokacija(primateljDonacije.getLokacija()).createPrimateljDonacije();
            primateljDonacije.setId(dohvatiPrimateljeDonacijaPremaKriterijima(primateljDonacije2).stream().findFirst().get().getId());
            PreparedStatement stmt2 = veza.prepareStatement("INSERT INTO PRIMATELJ_POTREBNI_PREDMETI (PRIMATELJ_ID, POTREBAN_PREDMET_ID) VALUES(?, ?)");
            for(PredmetDoniranja predmetDoniranja : primateljDonacije.getListaPotrebnihPredmeta().dohvatiSvePredmeteDoniranja()){
                stmt2.setLong(1, primateljDonacije.getId());
                stmt2.setLong(2, predmetDoniranja.getId());
                stmt2.executeUpdate();
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno dodavanje podataka u bazu podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void obrisiPrimateljaDonacijeIzBazePodataka(PrimateljDonacije primateljDonacije) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza.prepareStatement("DELETE FROM DONACIJA WHERE PRIMATELJ_ID = ?");
            preparedStatement.setLong(1, primateljDonacije.getId());
            preparedStatement.executeUpdate();
            PreparedStatement preparedStatement2 = veza.prepareStatement("DELETE FROM PRIMATELJ_POTREBNI_PREDMETI WHERE PRIMATELJ_ID = ?");
            preparedStatement2.setLong(1, primateljDonacije.getId());
            preparedStatement2.executeUpdate();
            PreparedStatement preparedStatement3 = veza.prepareStatement("DELETE FROM PRIMATELJ_PRIMLJENI_PREDMETI WHERE PRIMATELJ_ID = ?");
            preparedStatement3.setLong(1, primateljDonacije.getId());
            preparedStatement3.executeUpdate();
            PreparedStatement preparedStatement4 = veza.prepareStatement("DELETE FROM PRIMATELJ_DONACIJE WHERE ID = ?");
            preparedStatement4.setLong(1, primateljDonacije.getId());
            preparedStatement4.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno brisanje podataka iz baze podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }
    public static void dodajDonoraUBazuPodataka(Donor donor) throws BazaPodatakaException {
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement stmt = veza.prepareStatement("INSERT INTO DONOR (IME, PREZIME, OPIS_SUDIONIKA, LOKACIJA) VALUES(?, ?, ?, ?)");
            stmt.setString(1, donor.getImeIliImeTvrtke());
            stmt.setString(2, donor.getPrezimeIliOIBTvrtke());
            stmt.setString(3, donor.getOpisDonora());
            stmt.setString(4, donor.getLokacija().getImeGrada());
            stmt.executeUpdate();
            Donor donor2 = new Donor.DonorBuilder().setImeIliImeTvrtke(donor.getImeIliImeTvrtke())
                    .setPrezimeIliOIBTvrtke(donor.getPrezimeIliOIBTvrtke()).setLokacija(donor.getLokacija()).createDonor();
            donor.setId(dohvatiDonorePremaKriterijima(donor2).stream().findFirst().get().getId());
            PreparedStatement stmt2 = veza.prepareStatement("INSERT INTO DONOR_PONUDJENI_PREDMETI (DONOR_ID, PONUDJENI_PREDMET_ID) VALUES(?, ?)");
            for(PredmetDoniranja predmetDoniranja : donor.getListaPonudjenihPredmeta().dohvatiSvePredmeteDoniranja()){
                stmt2.setLong(1, donor.getId());
                stmt2.setLong(2, predmetDoniranja.getId());
                stmt2.executeUpdate();
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno dodavanje podataka u bazu podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void obrisiDonoraIzBazePodataka(Donor donor) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza.prepareStatement("DELETE FROM DONACIJA WHERE DONOR_ID = ?");
            preparedStatement.setLong(1, donor.getId());
            preparedStatement.executeUpdate();
            PreparedStatement preparedStatement2 = veza.prepareStatement("DELETE FROM DONOR_DONIRANI_PREDMETI WHERE DONOR_ID = ?");
            preparedStatement2.setLong(1, donor.getId());
            preparedStatement2.executeUpdate();
            PreparedStatement preparedStatement3 = veza.prepareStatement("DELETE FROM DONOR_PONUDJENI_PREDMETI WHERE DONOR_ID = ?");
            preparedStatement3.setLong(1, donor.getId());
            preparedStatement3.executeUpdate();
            PreparedStatement preparedStatement4 = veza.prepareStatement("DELETE FROM DONOR WHERE ID = ?");
            preparedStatement4.setLong(1, donor.getId());
            preparedStatement4.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno brisanje podataka iz baze podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static List<PredmetDoniranja> dohvatiPredmeteZaDonaciju() throws BazaPodatakaException{
        return dohvatiPredmeteDoniranjaPremaUpitu("""
                    SELECT *
                    FROM PREDMET_DONIRANJA2 pd
                    LEFT JOIN DONOR_DONIRANI_PREDMETI ddp ON pd.id = ddp.donirani_predmet_id
                    LEFT JOIN PRIMATELJ_PRIMLJENI_PREDMETI ppp ON pd.id = ppp.primljeni_predmet_id
                    WHERE ddp.donor_id IS NULL
                    AND ppp.primatelj_id IS NULL;""");
    }

    public static PrimateljDonacije dohvatiPrimateljaDonacijePoPredmetuDoniranja(PredmetDoniranja predmetDoniranja) throws BazaPodatakaException{
        try (Connection veza = spajanjeNaBazu()) {
            PreparedStatement statement = veza.prepareStatement("SELECT primatelj_id FROM PRIMATELJ_POTREBNI_PREDMETI " +
                    "WHERE potreban_predmet_id = ?");
            statement.setLong(1, predmetDoniranja.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long primateljId = resultSet.getLong("primatelj_id");
                    return dohvatiPrimateljeDonacijaPremaKriterijima(new PrimateljDonacije.PrimateljDonacijeBuilder().setId(primateljId).createPrimateljDonacije()).stream().findFirst().get();
                }
                return null;
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static Donor dohvatiDonoraPoPredmetuDoniranja(PredmetDoniranja predmetDoniranja)throws BazaPodatakaException {
        try (Connection veza = spajanjeNaBazu()) {
            PreparedStatement statement = veza.prepareStatement("SELECT donor_id FROM DONOR_PONUDJENI_PREDMETI " +
                    "WHERE ponudjeni_predmet_id = ?");
            statement.setLong(1, predmetDoniranja.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long donorId = resultSet.getLong("donor_id");
                    return dohvatiDonorePremaKriterijima(new Donor.DonorBuilder().setId(donorId).createDonor()).stream().findFirst().get();
                }
                return null;
            }
        } catch (SQLException e) {
            String poruka = "Neuspješno izvršavanje upita u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }
    private static Lokacija dohvatiLokaciju(String lokacijaStr) {
        return switch (lokacijaStr) {
            case "Zagreb" -> Lokacija.ZAGREB;
            case "Split" -> Lokacija.SPLIT;
            case "Rijeka" -> Lokacija.RIJEKA;
            case "Osijek" -> Lokacija.OSIJEK;
            case "Pula" -> Lokacija.PULA;
            default -> Lokacija.ZADAR;
        };
    }

    public static void obrisiDonacijuIzBazePodataka(Donacija donacija) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza.prepareStatement("DELETE FROM PRIMATELJ_PRIMLJENI_PREDMETI WHERE PRIMATELJ_ID = ?");
            preparedStatement.setLong(1, donacija.getPrimateljDonacije().getId());
            preparedStatement.executeUpdate();

            PreparedStatement preparedStatement2 = veza.prepareStatement("DELETE FROM DONOR_DONIRANI_PREDMETI WHERE DONOR_ID = ?");
            preparedStatement2.setLong(1, donacija.getDonor().getId());
            preparedStatement2.executeUpdate();

            PreparedStatement preparedStatement3 = veza.prepareStatement("DELETE FROM DONACIJA WHERE ID = ?");
            preparedStatement3.setLong(1, donacija.getId());
            preparedStatement3.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno brisanje podataka iz baze podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void dodajOdjecuUBazuPodataka(Odjeca odjeca) throws BazaPodatakaException {
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza.prepareStatement("INSERT INTO PREDMET_DONIRANJA2 (IME_PREDMETA, STANJE, VELICINA, TIP_PREDMETA) VALUES (?,?,?,?)");
            preparedStatement.setString(1, odjeca.getOpisPredmeta());
            preparedStatement.setString(2, odjeca.getStanje().getStanje());
            preparedStatement.setString(3, odjeca.getVelicina().getVelicina());
            preparedStatement.setString(4, "odjeća");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno dodavanje podataka u baze podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }
    public static void dodajHranuUBazuPodataka(Hrana hrana){
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza.prepareStatement("INSERT INTO PREDMET_DONIRANJA2 (IME_PREDMETA, ROK_TRAJANJA, KOLICINA_U_KG_ILI_L, TIP_PREDMETA) VALUES (?,?,?,?)");
            preparedStatement.setString(1, hrana.getOpisPredmeta());
            preparedStatement.setDate(2, Date.valueOf(hrana.getRokTrajanja()));
            preparedStatement.setDouble(3, hrana.getKolicinaUKgIliL());
            preparedStatement.setString(4, "hrana");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno dodavanje podataka u baze podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void obrisiPredmetDoniranjaIzBazePodataka(PredmetDoniranja predmetDoniranja) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza.prepareStatement("DELETE FROM DONACIJA WHERE PREDMET_ID = ?");
            preparedStatement.setLong(1, predmetDoniranja.getId());
            preparedStatement.executeUpdate();

            PreparedStatement preparedStatement2 = veza.prepareStatement("DELETE FROM DONOR_DONIRANI_PREDMETI WHERE DONIRANI_PREDMET_ID = ?");
            preparedStatement2.setLong(1, predmetDoniranja.getId());
            preparedStatement2.executeUpdate();

            PreparedStatement preparedStatement3 = veza.prepareStatement("DELETE FROM DONOR_PONUDJENI_PREDMETI WHERE PONUDJENI_PREDMET_ID = ?");
            preparedStatement3.setLong(1, predmetDoniranja.getId());
            preparedStatement3.executeUpdate();

            PreparedStatement preparedStatement4 = veza.prepareStatement("DELETE FROM PRIMATELJ_PRIMLJENI_PREDMETI WHERE PRIMLJENI_PREDMET_ID = ?");
            preparedStatement4.setLong(1, predmetDoniranja.getId());
            preparedStatement4.executeUpdate();

            PreparedStatement preparedStatement5 = veza.prepareStatement("DELETE FROM PRIMATELJ_POTREBNI_PREDMETI WHERE POTREBAN_PREDMET_ID = ?");
            preparedStatement5.setLong(1, predmetDoniranja.getId());
            preparedStatement5.executeUpdate();

            PreparedStatement preparedStatement6 = veza.prepareStatement("DELETE FROM PREDMET_DONIRANJA2 WHERE ID = ?");
            preparedStatement6.setLong(1, predmetDoniranja.getId());
            preparedStatement6.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno brisanje podataka iz baze podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void izmjeniKorisnikaUBaziPodataka(Korisnik noviKorisnik)throws BazaPodatakaException {
        try(Connection veza = spajanjeNaBazu()){
            StringBuilder sqlUpit = new StringBuilder("UPDATE KORISNIK SET");
            int redniBroj = 1;
            if (Optional.ofNullable(noviKorisnik.getKorisnickoIme()).isPresent()) {
                sqlUpit.append(" KORISNICKO_IME = ?,");
            }
            if (Optional.ofNullable(noviKorisnik.getOsoba()).isPresent()) {
                if(Optional.ofNullable(noviKorisnik.getOsoba().getIme()).isPresent()) {
                    sqlUpit.append(" IME = ?,");
                }
                if(Optional.ofNullable(noviKorisnik.getOsoba().getPrezime()).isPresent()){
                    sqlUpit.append(" PREZIME = ?,");
                }
            }
            if(Optional.ofNullable(noviKorisnik.getEmail()).isPresent()){
                sqlUpit.append(" EMAIL = ?,");
            }
            if(Optional.ofNullable(noviKorisnik.getLokacija()).isPresent()){
                sqlUpit.append(" LOKACIJA = ?,");
            }
            sqlUpit.deleteCharAt(sqlUpit.length() - 1);
            sqlUpit.append(" WHERE ID = ?");
            PreparedStatement preparedStatement = veza.prepareStatement(sqlUpit.toString());
            if (Optional.ofNullable(noviKorisnik.getKorisnickoIme()).isPresent()) {
                preparedStatement.setString(redniBroj++, noviKorisnik.getKorisnickoIme());
            }
            if (Optional.ofNullable(noviKorisnik.getOsoba()).isPresent()) {
                if(Optional.ofNullable(noviKorisnik.getOsoba().getIme()).isPresent()) {
                    preparedStatement.setString(redniBroj++, noviKorisnik.getOsoba().getIme());
                }
                if(Optional.ofNullable(noviKorisnik.getOsoba().getPrezime()).isPresent()){
                    preparedStatement.setString(redniBroj++, noviKorisnik.getOsoba().getPrezime());
                }
            }
            if(Optional.ofNullable(noviKorisnik.getEmail()).isPresent()){
                preparedStatement.setString(redniBroj++, noviKorisnik.getEmail());
            }
            if(Optional.ofNullable(noviKorisnik.getLokacija()).isPresent()){
                preparedStatement.setString(redniBroj++, noviKorisnik.getLokacija().getImeGrada());
            }
            preparedStatement.setLong(redniBroj, noviKorisnik.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno mijenjanje podataka u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void izmjeniPrimateljaUBaziPodataka(PrimateljDonacije noviPrimatelj) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            veza.setAutoCommit(false);
            StringBuilder sqlUpit = new StringBuilder("UPDATE PRIMATELJ_DONACIJE SET");
            int redniBroj = 1;
            if (Optional.ofNullable(noviPrimatelj.getImeIliImeTvrtke()).isPresent()) {
                sqlUpit.append(" IME = ?,");
            }
            if(Optional.ofNullable(noviPrimatelj.getPrezimeIliOIBTvrtke()).isPresent()){
                sqlUpit.append(" PREZIME = ?,");
            }
            if(Optional.ofNullable(noviPrimatelj.getOpisPrimatelja()).isPresent()){
                sqlUpit.append(" OPIS_SUDIONIKA = ?,");
            }
            if(Optional.ofNullable(noviPrimatelj.getLokacija()).isPresent()){
                sqlUpit.append(" LOKACIJA = ?,");
            }
            sqlUpit.deleteCharAt(sqlUpit.length() - 1);
            sqlUpit.append(" WHERE ID = ?");
            PreparedStatement preparedStatement = veza.prepareStatement(sqlUpit.toString());
            if(Optional.ofNullable(noviPrimatelj.getImeIliImeTvrtke()).isPresent()) {
                preparedStatement.setString(redniBroj++, noviPrimatelj.getImeIliImeTvrtke());
            }
            if(Optional.ofNullable(noviPrimatelj.getPrezimeIliOIBTvrtke()).isPresent()){
                preparedStatement.setString(redniBroj++, noviPrimatelj.getPrezimeIliOIBTvrtke());
            }
            if(Optional.ofNullable(noviPrimatelj.getOpisPrimatelja()).isPresent()){
                preparedStatement.setString(redniBroj++, noviPrimatelj.getOpisPrimatelja());
            }
            if(Optional.ofNullable(noviPrimatelj.getLokacija()).isPresent()){
                preparedStatement.setString(redniBroj++, noviPrimatelj.getLokacija().getImeGrada());
            }
            preparedStatement.setLong(redniBroj, noviPrimatelj.getId());
            preparedStatement.executeUpdate();
            if(Optional.ofNullable(noviPrimatelj.getListaPotrebnihPredmeta()).isPresent()){
                PreparedStatement preparedStatement2 = veza.prepareStatement("DELETE FROM PRIMATELJ_POTREBNI_PREDMETI WHERE PRIMATELJ_ID = ?");
                preparedStatement2.setLong(1, noviPrimatelj.getId());
                preparedStatement2.executeUpdate();

                PreparedStatement preparedStatement3 = veza.prepareStatement("INSERT INTO PRIMATELJ_POTREBNI_PREDMETI (primatelj_id, potreban_predmet_id) VALUES (?,?)");
                for(PredmetDoniranja potrebanPredmet : noviPrimatelj.getListaPotrebnihPredmeta().dohvatiSvePredmeteDoniranja()){
                    preparedStatement3.setLong(1, noviPrimatelj.getId());
                    preparedStatement3.setLong(2, potrebanPredmet.getId());
                    preparedStatement3.executeUpdate();
                }
            }
            veza.commit();
            veza.setAutoCommit(true);
        } catch (SQLException e) {
            String poruka = "Neuspješno mijenjanje podataka u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void izmjeniDonoraUBaziPodataka(Donor noviDonor) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            veza.setAutoCommit(false);
            StringBuilder sqlUpit = new StringBuilder("UPDATE DONOR SET");
            int redniBroj = 1;
            if (Optional.ofNullable(noviDonor.getImeIliImeTvrtke()).isPresent()) {
                sqlUpit.append(" IME = ?,");
            }
            if(Optional.ofNullable(noviDonor.getPrezimeIliOIBTvrtke()).isPresent()){
                sqlUpit.append(" PREZIME = ?,");
            }
            if(Optional.ofNullable(noviDonor.getOpisDonora()).isPresent()){
                sqlUpit.append(" OPIS_SUDIONIKA = ?,");
            }
            if(Optional.ofNullable(noviDonor.getLokacija()).isPresent()){
                sqlUpit.append(" LOKACIJA = ?,");
            }
            sqlUpit.deleteCharAt(sqlUpit.length() - 1);
            sqlUpit.append(" WHERE ID = ?");
            PreparedStatement preparedStatement = veza.prepareStatement(sqlUpit.toString());
            if(Optional.ofNullable(noviDonor.getImeIliImeTvrtke()).isPresent()) {
                preparedStatement.setString(redniBroj++, noviDonor.getImeIliImeTvrtke());
            }
            if(Optional.ofNullable(noviDonor.getPrezimeIliOIBTvrtke()).isPresent()){
                preparedStatement.setString(redniBroj++, noviDonor.getPrezimeIliOIBTvrtke());
            }
            if(Optional.ofNullable(noviDonor.getOpisDonora()).isPresent()){
                preparedStatement.setString(redniBroj++, noviDonor.getOpisDonora());
            }
            if(Optional.ofNullable(noviDonor.getLokacija()).isPresent()){
                preparedStatement.setString(redniBroj++, noviDonor.getLokacija().getImeGrada());
            }
            preparedStatement.setLong(redniBroj, noviDonor.getId());
            preparedStatement.executeUpdate();

            PreparedStatement preparedStatement2 = veza.prepareStatement("DELETE FROM DONOR_PONUDJENI_PREDMETI WHERE DONOR_ID = ?");
            preparedStatement2.setLong(1, noviDonor.getId());
            preparedStatement2.executeUpdate();

            PreparedStatement preparedStatement3 = veza.prepareStatement("INSERT INTO DONOR_PONUDJENI_PREDMETI (DONOR_ID, PONUDJENI_PREDMET_ID) VALUES (?,?)");
            for(PredmetDoniranja ponudjeniPredmet : noviDonor.getListaPonudjenihPredmeta().dohvatiSvePredmeteDoniranja()){
                preparedStatement3.setLong(1, noviDonor.getId());
                preparedStatement3.setLong(2, ponudjeniPredmet.getId());
                preparedStatement3.executeUpdate();
            }
            veza.commit();
            veza.setAutoCommit(true);
        } catch (SQLException e) {
            String poruka = "Neuspješno mijenjanje podataka u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void izmjeniDonacijuUBaziPodataka(Donacija novaDonacija)throws BazaPodatakaException {
        try(Connection veza = spajanjeNaBazu()){
            PreparedStatement preparedStatement = veza.prepareStatement("DELETE FROM DONOR_DONIRANI_PREDMETI WHERE DONIRANI_PREDMET_ID = ?");
            preparedStatement.setLong(1, novaDonacija.getPredmetDoniranja().getId());
            preparedStatement.executeUpdate();

            PreparedStatement preparedStatement2 = veza.prepareStatement("DELETE FROM DONOR_PONUDJENI_PREDMETI WHERE PONUDJENI_PREDMET_ID = ?");
            preparedStatement2.setLong(1, novaDonacija.getPredmetDoniranja().getId());
            preparedStatement2.executeUpdate();

            PreparedStatement preparedStatement3 = veza.prepareStatement("DELETE FROM PRIMATELJ_PRIMLJENI_PREDMETI WHERE PRIMLJENI_PREDMET_ID = ?");
            preparedStatement3.setLong(1, novaDonacija.getPredmetDoniranja().getId());
            preparedStatement3.executeUpdate();

            PreparedStatement preparedStatement4 = veza.prepareStatement("DELETE FROM PRIMATELJ_POTREBNI_PREDMETI WHERE POTREBAN_PREDMET_ID = ?");
            preparedStatement4.setLong(1, novaDonacija.getPredmetDoniranja().getId());
            preparedStatement4.executeUpdate();

            PreparedStatement preparedStatement5 = veza.prepareStatement("INSERT INTO DONOR_DONIRANI_PREDMETI (DONOR_ID, DONIRANI_PREDMET_ID) VALUES (?,?)");
            preparedStatement5.setLong(1, novaDonacija.getDonor().getId());
            preparedStatement5.setLong(2, novaDonacija.getPredmetDoniranja().getId());
            preparedStatement5.executeUpdate();

            PreparedStatement preparedStatement6 = veza.prepareStatement("INSERT INTO PRIMATELJ_PRIMLJENI_PREDMETI (PRIMATELJ_ID, PRIMLJENI_PREDMET_ID) VALUES (?,?)");
            preparedStatement6.setLong(1, novaDonacija.getPrimateljDonacije().getId());
            preparedStatement6.setLong(2, novaDonacija.getPredmetDoniranja().getId());
            preparedStatement6.executeUpdate();

            StringBuilder sqlUpit = new StringBuilder("UPDATE DONACIJA SET");
            int redniBroj = 1;
            if(Optional.ofNullable(novaDonacija.getPredmetDoniranja()).isPresent()){
                sqlUpit.append(" PREDMET_ID = ?,");
            }
            if(Optional.ofNullable(novaDonacija.getDonor()).isPresent()){
                sqlUpit.append(" DONOR_ID = ?,");
            }
            if(Optional.ofNullable(novaDonacija.getPrimateljDonacije()).isPresent()){
                sqlUpit.append(" PRIMATELJ_ID = ?,");
            }
            if(Optional.ofNullable(novaDonacija.getVrijemeDonacije()).isPresent()){
                sqlUpit.append(" DATUM_I_VRIJEME = ?,");
            }
            sqlUpit.deleteCharAt(sqlUpit.length() - 1);
            sqlUpit.append(" WHERE ID = ?");
            PreparedStatement preparedStatement7 = veza.prepareStatement(sqlUpit.toString());
            if(Optional.ofNullable(novaDonacija.getPredmetDoniranja()).isPresent()) {
                preparedStatement7.setLong(redniBroj++, novaDonacija.getPredmetDoniranja().getId());
            }
            if(Optional.ofNullable(novaDonacija.getDonor()).isPresent()){
                preparedStatement7.setLong(redniBroj++, novaDonacija.getDonor().getId());
            }
            if(Optional.ofNullable(novaDonacija.getPrimateljDonacije()).isPresent()){
                preparedStatement7.setLong(redniBroj++, novaDonacija.getPrimateljDonacije().getId());
            }
            if(Optional.ofNullable(novaDonacija.getVrijemeDonacije()).isPresent()){
                preparedStatement7.setTimestamp(redniBroj++, Timestamp.valueOf(novaDonacija.getVrijemeDonacije()));
            }
            preparedStatement7.setLong(redniBroj, novaDonacija.getId());
            preparedStatement7.executeUpdate();

        } catch (SQLException e) {
            String poruka = "Neuspješno mijenjanje podataka u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }

    public static void izmjeniPredmetDoniranjaUBaziPodataka(PredmetDoniranja noviPredmet) throws BazaPodatakaException{
        try(Connection veza = spajanjeNaBazu()){
            StringBuilder sqlUpit = new StringBuilder("UPDATE PREDMET_DONIRANJA2 SET");
            int redniBroj = 1;
            if(Optional.ofNullable(noviPredmet.getOpisPredmeta()).isPresent()){
                sqlUpit.append(" IME_PREDMETA = ?,");
            }
            if(noviPredmet instanceof Hrana){
                if(Optional.ofNullable(((Hrana) noviPredmet).getRokTrajanja()).isPresent()){
                    sqlUpit.append(" ROK_TRAJANJA = ?,");
                }
                if(!((Hrana) noviPredmet).getKolicinaUKgIliL().equals(0.0)){
                    sqlUpit.append(" KOLICINA_U_KG_ILI_L = ?,");
                }
            }
            else{
                if(Optional.ofNullable(((Odjeca) noviPredmet).getStanje()).isPresent()){
                    sqlUpit.append(" STANJE = ?,");
                }
                if(Optional.ofNullable(((Odjeca) noviPredmet).getVelicina()).isPresent()){
                    sqlUpit.append(" VELICINA = ?,");
                }
            }
            sqlUpit.deleteCharAt(sqlUpit.length() - 1);
            sqlUpit.append(" WHERE ID = ?");
            PreparedStatement preparedStatement = veza.prepareStatement(sqlUpit.toString());
            if(Optional.ofNullable(noviPredmet.getOpisPredmeta()).isPresent()) {
                preparedStatement.setString(redniBroj++, noviPredmet.getOpisPredmeta());
            }
            if(noviPredmet instanceof Hrana){
                if(Optional.ofNullable(((Hrana) noviPredmet).getRokTrajanja()).isPresent()){
                    preparedStatement.setDate(redniBroj++, Date.valueOf(((Hrana) noviPredmet).getRokTrajanja()));
                }
                if(!((Hrana) noviPredmet).getKolicinaUKgIliL().equals(0.0)){
                    preparedStatement.setDouble(redniBroj++, ((Hrana) noviPredmet).getKolicinaUKgIliL());
                }
            }
            else{
                if(Optional.ofNullable(((Odjeca)noviPredmet).getStanje()).isPresent()){
                    preparedStatement.setString(redniBroj++, ((Odjeca) noviPredmet).getStanje().getStanje());
                }
                if(Optional.ofNullable(((Odjeca)noviPredmet).getVelicina()).isPresent()){
                    preparedStatement.setString(redniBroj++, ((Odjeca) noviPredmet).getVelicina().getVelicina());
                }
            }
            preparedStatement.setLong(redniBroj, noviPredmet.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String poruka = "Neuspješno mijenjanje podataka u bazi podataka.";
            logger.error(poruka);
            throw new BazaPodatakaException(poruka, e);
        }
    }
}