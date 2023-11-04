package hr.java.vjezbe.entitet;


import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Optional;

@Getter
@Setter
public final class Donor extends Entitet implements MarkerSucelje{
    @Serial
    private static final long serialVersionUID = -5333730968109060831L;
    private String imeIliImeTvrtke;
    private String prezimeIliOIBTvrtke;
    private String opisDonora;
    private Lokacija lokacija;
    private PredmetiDoniranja<PredmetDoniranja> listaDoniranihPredmeta;
    private PredmetiDoniranja<PredmetDoniranja> listaPonudjenihPredmeta;
    private Donor(Long id, String imeIliImeTvrtke, String prezimeIliOIBTvrtke, String opisDonora, Lokacija lokacija, PredmetiDoniranja<PredmetDoniranja> listaDoniranihPredmeta, PredmetiDoniranja<PredmetDoniranja> listaPonudjenihPredmeta) {
        super(id);
        this.imeIliImeTvrtke = imeIliImeTvrtke;
        this.prezimeIliOIBTvrtke = prezimeIliOIBTvrtke;
        this.opisDonora = opisDonora;
        this.lokacija = lokacija;
        this.listaDoniranihPredmeta = listaDoniranihPredmeta;
        this.listaPonudjenihPredmeta = listaPonudjenihPredmeta;
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
        if(Optional.ofNullable(opisDonora).isPresent()){
            string.append(opisDonora).append("\n");
        }
        if(Optional.ofNullable(lokacija).isPresent()){
            string.append(lokacija).append("\n");
        }
        if(Optional.ofNullable(listaDoniranihPredmeta).isPresent()){
            if(Optional.ofNullable(listaDoniranihPredmeta.dohvatiSvePredmeteDoniranja()).isPresent()){
                if(listaDoniranihPredmeta.dohvatiSvePredmeteDoniranja().size() > 0){
                    string.append("donirani predmeti:").append("\n").append(listaDoniranihPredmeta).append("\n");
                }
            }
        }
        if(Optional.ofNullable(listaPonudjenihPredmeta).isPresent()){
            if(Optional.ofNullable(listaPonudjenihPredmeta.dohvatiSvePredmeteDoniranja()).isPresent()){
                if(listaPonudjenihPredmeta.dohvatiSvePredmeteDoniranja().size()>0){
                    string.append("ponuđeni predmeti:").append("\n").append(listaPonudjenihPredmeta).append("\n");
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
        Donor donor = (Donor) o;
        if(Optional.ofNullable(this.listaPonudjenihPredmeta).isEmpty() || Optional.ofNullable(((Donor) o).getListaPonudjenihPredmeta()).isEmpty()){
            if(!(Optional.ofNullable(this.listaPonudjenihPredmeta).isEmpty() && Optional.ofNullable(((Donor) o).getListaPonudjenihPredmeta()).isEmpty())){
                return false;
            }
        }
        return imeIliImeTvrtke.equalsIgnoreCase(donor.imeIliImeTvrtke) && prezimeIliOIBTvrtke.equalsIgnoreCase(donor.prezimeIliOIBTvrtke) && opisDonora.equals(donor.opisDonora) && lokacija == donor.lokacija && listaPonudjenihPredmeta.equals(donor.listaPonudjenihPredmeta);
    }
    public static class DonorBuilder {
        private Long id;
        private String imeIliImeTvrtke;
        private String prezimeIliOIBTvrtke;
        private String opisDonora;
        private Lokacija lokacija;
        private PredmetiDoniranja<PredmetDoniranja> listaDoniranihPredmeta;
        private PredmetiDoniranja<PredmetDoniranja> listaPonudjenihPredmeta;

        public DonorBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public DonorBuilder setOpisDonora(String opisDonora) {
            this.opisDonora = opisDonora;
            return this;
        }

        public DonorBuilder setLokacija(Lokacija lokacija) {
            this.lokacija = lokacija;
            return this;
        }

        public DonorBuilder setListaDoniranihPredmeta(PredmetiDoniranja<PredmetDoniranja> listaDoniranihPredmeta) {
            this.listaDoniranihPredmeta = listaDoniranihPredmeta;
            return this;
        }

        public DonorBuilder setListaPonudjenihPredmeta(PredmetiDoniranja<PredmetDoniranja> listaPonudjenihPredmeta) {
            this.listaPonudjenihPredmeta = listaPonudjenihPredmeta;
            return this;
        }
        public DonorBuilder setImeIliImeTvrtke(String imeIliImeTvrtke){
            this.imeIliImeTvrtke = imeIliImeTvrtke;
            return this;
        }
        public DonorBuilder setPrezimeIliOIBTvrtke(String prezimeIliOIBTvrtke){
            this.prezimeIliOIBTvrtke = prezimeIliOIBTvrtke;
            return this;
        }
        public Donor createDonor() {
            return new Donor(id, imeIliImeTvrtke, prezimeIliOIBTvrtke, opisDonora, lokacija, listaDoniranihPredmeta, listaPonudjenihPredmeta);
        }
    }
}