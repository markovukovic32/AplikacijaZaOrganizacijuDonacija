package hr.java.vjezbe.entitet;

public enum Lokacija {
    ZAGREB("Zagreb", 10000),SPLIT("Split", 21000),
    RIJEKA("Rijeka", 51000),OSIJEK("Osijek", 31000),
    ZADAR("Zadar", 23000),PULA("Pula", 52100);
    private final String imeGrada;
    private final Integer postanskiBroj;

    Lokacija(String imeGrada, Integer postanskiBroj) {
        this.imeGrada = imeGrada;
        this.postanskiBroj = postanskiBroj;
    }
    public String getImeGrada() {
        return imeGrada;
    }

    @Override
    public String toString() {
        return imeGrada;
    }

    public Integer getPostanskiBroj() {
        return postanskiBroj;
    }
}
