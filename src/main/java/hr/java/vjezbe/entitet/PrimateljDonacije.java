package hr.java.vjezbe.entitet;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Optional; /*167*/

@Getter
@Setter
public final class PrimateljDonacije extends Entitet implements MarkerSucelje{
    @Serial
    private static final long serialVersionUID = -1661937696967264433L;
    private String imeIliImeTvrtke;
    private String prezimeIliOIBTvrtke;
    private String opisPrimatelja;
    private Lokacija lokacija;
    private PredmetiDoniranja<PredmetDoniranja> listaPrimljenihPredmeta;
    private PredmetiDoniranja<PredmetDoniranja> listaPotrebnihPredmeta;

    private PrimateljDonacije(Long id, String opisPrimatelja, Lokacija lokacija, String imeIliImeTvrtke, String prezimeIliOIBTvrtke,PredmetiDoniranja<PredmetDoniranja> listaPrimljenihPredmeta, PredmetiDoniranja<PredmetDoniranja> listaPotrebnihPredmeta) {
        super(id);
        this.opisPrimatelja = opisPrimatelja;
        this.lokacija = lokacija;
        this.imeIliImeTvrtke = imeIliImeTvrtke;
        this.prezimeIliOIBTvrtke = prezimeIliOIBTvrtke;
        this.listaPrimljenihPredmeta = listaPrimljenihPredmeta;
        this.listaPotrebnihPredmeta = listaPotrebnihPredmeta;
    }
    @Override
    public String toString() {
        return imeIliImeTvrtke + " " + prezimeIliOIBTvrtke;
    }
    public String tablicniString(){
        StringBuilder string = new StringBuilder();
        if(Optional.ofNullable(imeIliImeTvrtke).isPresent()){
            string.append(imeIliImeTvrtke).append("\n");
        }
        if(Optional.ofNullable(prezimeIliOIBTvrtke).isPresent()){
            string.append(prezimeIliOIBTvrtke).append("\n");
        }
        if(Optional.ofNullable(opisPrimatelja).isPresent()){
            string.append(opisPrimatelja).append("\n");
        }
        if(Optional.ofNullable(lokacija).isPresent()){
            string.append(lokacija).append("\n");
        }
        if(Optional.ofNullable(listaPrimljenihPredmeta).isPresent()){
            if(Optional.ofNullable(listaPrimljenihPredmeta.dohvatiSvePredmeteDoniranja()).isPresent()){
                if(listaPrimljenihPredmeta.dohvatiSvePredmeteDoniranja().size() > 0){
                    string.append("primljeni predmeti:").append("\n").append(listaPrimljenihPredmeta).append("\n");
                }
            }
        }
        if(Optional.ofNullable(listaPotrebnihPredmeta).isPresent()){
            if(Optional.ofNullable(listaPotrebnihPredmeta.dohvatiSvePredmeteDoniranja()).isPresent()){
                if(listaPotrebnihPredmeta.dohvatiSvePredmeteDoniranja().size()>0){
                    string.append("potrebni predmeti:").append("\n").append(listaPotrebnihPredmeta).append("\n");
                }
            }
        }
        if(string.length()>0){
            string.deleteCharAt(string.length() - 1);
        }
        return string.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimateljDonacije that = (PrimateljDonacije) o;
        if(Optional.ofNullable(this.listaPotrebnihPredmeta).isEmpty() || Optional.ofNullable(((PrimateljDonacije) o).getListaPotrebnihPredmeta()).isEmpty()){
            if(!(Optional.ofNullable(this.listaPotrebnihPredmeta).isEmpty() && Optional.ofNullable(((PrimateljDonacije) o).getListaPotrebnihPredmeta()).isEmpty())){
                return false;
            }
        }
        return imeIliImeTvrtke.equalsIgnoreCase(that.imeIliImeTvrtke) && prezimeIliOIBTvrtke.equalsIgnoreCase(that.prezimeIliOIBTvrtke) && opisPrimatelja.equals(that.opisPrimatelja) && lokacija == that.lokacija && this.getListaPotrebnihPredmeta().equals(((PrimateljDonacije) o).getListaPotrebnihPredmeta());
    }
    public static class PrimateljDonacijeBuilder {
        private Long id;
        private String opisPrimatelja;
        private Lokacija lokacija;
        private String imeIliImeTvrtke;
        private String prezimeIliOIBTvrtke;
        private PredmetiDoniranja<PredmetDoniranja> listaPrimljenihPredmeta;
        private PredmetiDoniranja<PredmetDoniranja> listaPotrebnihPredmeta;
        public PrimateljDonacijeBuilder setId(Long id){
            this.id = id;
            return this;
        }

        public PrimateljDonacijeBuilder setOpisPrimatelja(String opisPrimatelja) {
            this.opisPrimatelja = opisPrimatelja;
            return this;
        }

        public PrimateljDonacijeBuilder setLokacija(Lokacija lokacija) {
            this.lokacija = lokacija;
            return this;
        }
        public PrimateljDonacijeBuilder setImeIliImeTvrtke(String imeIliImeTvrtke){
            this.imeIliImeTvrtke = imeIliImeTvrtke;
            return this;
        }
        public PrimateljDonacijeBuilder setPrezimeIliOIBTvrtke(String prezimeIliOIBTvrtke){
            this.prezimeIliOIBTvrtke = prezimeIliOIBTvrtke;
            return this;
        }
        public PrimateljDonacijeBuilder setListaPrimljenihPredmeta(PredmetiDoniranja<PredmetDoniranja> listaPrimljenihPredmeta){
            this.listaPrimljenihPredmeta = listaPrimljenihPredmeta;
            return this;
        }
        public PrimateljDonacijeBuilder setListaPotrebnihPredmeta(PredmetiDoniranja<PredmetDoniranja> listaPotrebnihPredmeta){
            this.listaPotrebnihPredmeta = listaPotrebnihPredmeta;
            return this;
        }
        public PrimateljDonacije createPrimateljDonacije(){
            return new PrimateljDonacije(id, opisPrimatelja, lokacija, imeIliImeTvrtke, prezimeIliOIBTvrtke, listaPrimljenihPredmeta, listaPotrebnihPredmeta);
        }
    }
}