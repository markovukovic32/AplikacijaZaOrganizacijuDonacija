package hr.java.vjezbe.entitet;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Getter
@Setter
public class Hrana extends PredmetDoniranja implements Serializable {
    @Serial
    private static final long serialVersionUID = 4909040425587182263L;
    private LocalDate rokTrajanja;
    private Double kolicinaUKgIliL;
    private Hrana(Long id, String opisPredmeta, LocalDate rokTrajanja, Double kolicinaUKgIliL) {
        super(id, opisPredmeta);
        this.rokTrajanja = rokTrajanja;
        this.kolicinaUKgIliL = kolicinaUKgIliL;
    }
    public static class HranaBuilder{
        private Long id;
        private String opisPredmeta;
        private LocalDate rokTrajanja;
        private double kolicinaUKgIliL;

        public HranaBuilder setId(Long id) {
            this.id = id;
            return this;
        }
        public HranaBuilder setOpisPredmeta(String opisPredmeta) {
            this.opisPredmeta = opisPredmeta;
            return this;
        }

        public HranaBuilder setRokTrajanja(LocalDate rokTrajanja) {
            this.rokTrajanja = rokTrajanja;
            return this;
        }

        public HranaBuilder setKolicinaUKgIliL(Double kolicinaUKgIliL) {
            this.kolicinaUKgIliL = kolicinaUKgIliL;
            return this;
        }

        public Hrana createHrana() {
            return new Hrana(id, opisPredmeta, rokTrajanja, kolicinaUKgIliL);
        }
    }

    @Override
    public String toString() {
        return super.getOpisPredmeta() + " " + rokTrajanja.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")) + " " + kolicinaUKgIliL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hrana hrana = (Hrana) o;
        return rokTrajanja.equals(hrana.rokTrajanja) && kolicinaUKgIliL.equals(hrana.kolicinaUKgIliL);
    }

    public String tablicniString(){
        StringBuilder stringBuilder = new StringBuilder();
        if(Optional.ofNullable(super.getOpisPredmeta()).isPresent()){
            stringBuilder.append(super.getOpisPredmeta()).append("\n");
        }
        if(Optional.ofNullable(rokTrajanja).isPresent()){
            stringBuilder.append("Rok trajanja: ").append(rokTrajanja.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."))).append("\n");
        }
        if(Optional.ofNullable(kolicinaUKgIliL).isPresent()){
            stringBuilder.append("Količina (kg ili l): ").append(kolicinaUKgIliL).append("\n");
        }
        if(stringBuilder.length() > 0){
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        return stringBuilder.toString();
    }
}
