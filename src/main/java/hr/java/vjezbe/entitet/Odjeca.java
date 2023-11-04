package hr.java.vjezbe.entitet;


import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

@Getter
@Setter
public class Odjeca extends PredmetDoniranja implements Serializable {
    @Serial
    private static final long serialVersionUID = -4272930673694139833L;
    private Stanje stanje;
    private Velicina velicina;

    private Odjeca(Long id, String opisPredmeta, Stanje stanje, Velicina velicina) {
        super(id, opisPredmeta);
        this.stanje = stanje;
        this.velicina = velicina;
    }
    public static class OdjecaBuilder{
        private Long id;
        private String opisPredmeta;
        private Stanje stanje;
        private Velicina velicina;

        public OdjecaBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public OdjecaBuilder setOpisPredmeta(String opisPredmeta) {
            this.opisPredmeta = opisPredmeta;
            return this;
        }

        public OdjecaBuilder setStanje(Stanje stanje) {
            this.stanje = stanje;
            return this;
        }

        public OdjecaBuilder setVelicina(Velicina velicina) {
            this.velicina = velicina;
            return this;
        }

        public Odjeca createOdjeca() {
            return new Odjeca(id, opisPredmeta, stanje, velicina);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Odjeca odjeca = (Odjeca) o;
        return stanje == odjeca.stanje && velicina == odjeca.velicina;
    }

    @Override
    public String toString() {
        return (super.getOpisPredmeta() + " " + stanje + " " + velicina).toLowerCase();
    }
    public String tablicniString(){
        StringBuilder stringBuilder = new StringBuilder();
        if(Optional.ofNullable(super.getOpisPredmeta()).isPresent()){
            stringBuilder.append(super.getOpisPredmeta()).append("\n");
        }
        if(Optional.ofNullable(stanje).isPresent()){
            stringBuilder.append("Stanje: ").append(stanje).append("\n");
        }
        if(Optional.ofNullable(velicina).isPresent()){
            stringBuilder.append("Veličina: ").append(velicina).append("\n");
        }
        if(stringBuilder.length()>0){
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        return stringBuilder.toString();
    }
}