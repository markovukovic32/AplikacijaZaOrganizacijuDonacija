package util;

public enum ZasloniAplikacije {
    ZASLON_LOGIN("Prijava", "/zasloni/login.fxml"),
    ZASLON_ADMINA("Dobrodošli", "/zasloni/zaslonAdmina.fxml"),
    ZASLON_ADMINA_ZA_UNOS_KORISNIKA("Unos korisnika", "/zasloni/zaslonAdminaZaUnosKorisnika.fxml"),
    ZASLON_ADMINA_ZA_PRETRAGU_I_BRISANJE_KORISNIKA("Brisanje korisnika", "/zasloni/zaslonAdminaZaPretraguIBrisanjeKorisnika.fxml"),
    ZASLON_ADMINA_ZA_IZMJENU_KORISNIKA("Izmjena podataka o korisnicima", "/zasloni/zaslonAdminaZaIzmjenuKorisnika.fxml"),
    ZASLON_ZA_PRETRAGU_I_BRISANJE_PRIMATELJA_DONACIJE("Pretraga i brisanje primatelja donacije", "/zasloni/zaslonZaPretraguIBrisanjePrimateljaDonacija.fxml"),
    ZASLON_ZA_PRETRAGU_I_BRISANJE_DONORA("Pretraga i brisanje donora", "/zasloni/zaslonZaPretraguIBrisanjeDonora.fxml"),
    ZASLON_ZA_PRETRAGU_I_BRISANJE_DONACIJA("Pretraga i brisanje donacija", "/zasloni/zaslonZaPretraguIBrisanjeDonacija.fxml"),
    ZASLON_ZA_PRETRAGU_I_BRISANJE_PREDMETA_DONIRANJA("Pretraga i brisanje predmeta za doniranje", "/zasloni/zaslonZaPretraguIBrisanjePredmetaDoniranja.fxml"),
    ZASLON_ZA_UNOS_PRIMATELJA_DONACIJA("Unos primatelja donacija", "/zasloni/zaslonZaUnosPrimateljaDonacija.fxml"),
    ZASLON_ZA_UNOS_DONORA("Unos donora", "/zasloni/zaslonZaUnosDonora.fxml"),
    ZASLON_ZA_UNOS_DONACIJA("Unos donacija", "/zasloni/zaslonZaUnosDonacija.fxml"),
    ZASLON_ZA_UNOS_PREDMETA_DONIRANJA("Unos predmeta za doniranje", "/zasloni/zaslonZaUnosPredmetaDoniranja.fxml"),
    ZASLON_ZA_IZMJENU_PRIMATELJA_DONACIJA("Izmjena podataka o primateljima donacija", "/zasloni/zaslonZaIzmjenuPrimateljaDonacija.fxml"),
    ZASLON_ZA_IZMJENU_DONORA("Izmjena podataka o donorima", "/zasloni/zaslonZaIzmjenuDonora.fxml"),
    ZASLON_ZA_IZMJENU_DONACIJA("Izmjena podataka o donacijama", "/zasloni/zaslonZaIzmjenuDonacije.fxml"),
    ZASLON_ZA_IZMJENU_PREDMETA_DONIRANJA("Izmjena podataka o predmetu doniranja", "/zasloni/zaslonZaIzmjenuPredmetaDoniranja.fxml"),
    ZASLON_S_POPISOM_PROMJENA("Popis svih promjena podataka", "/zasloni/zaslonSPopisomIzmjenjenihPodataka.fxml");

    private final String naslov;
    private final String fxml;

    ZasloniAplikacije(String naslov, String fxmlPath) {
        this.naslov = naslov;
        this.fxml = fxmlPath;
    }

    public String getNaslov() {
        return naslov;
    }

    public String getFxml() {
        return fxml;
    }

    @Override
    public String toString() {
        return "ZasloniAplikacije{" +
                "naslov='" + naslov + '\'' +
                ", fxml='" + fxml + '\'' +
                '}';
    }
}
