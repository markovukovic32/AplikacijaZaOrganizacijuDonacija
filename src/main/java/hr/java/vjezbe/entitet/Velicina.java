package hr.java.vjezbe.entitet;

public enum Velicina {
    XS("XS"),S("S"),M("M"),L("L"),XL("XL"),XXL("XXL");
    Velicina(String velicina){
        this.velicina = velicina;
    }
    private String velicina;

    public String getVelicina() {
        return velicina;
    }
}
