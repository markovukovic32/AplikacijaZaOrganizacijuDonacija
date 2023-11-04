package hr.java.vjezbe.entitet;

public enum Stanje {
    NOVO("novo"),RABLJENO("rabljeno");
    Stanje(String stanje){
        this.stanje = stanje;
    }
    private String stanje;

    public String getStanje() {
        return stanje;
    }
}
