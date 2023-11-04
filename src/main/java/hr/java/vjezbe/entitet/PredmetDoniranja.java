package hr.java.vjezbe.entitet;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
@Getter
@Setter

public class PredmetDoniranja extends Entitet{
    @Serial
    private static final long serialVersionUID = 4981220365230607773L;
    private String opisPredmeta;

    PredmetDoniranja(Long id, String opisPredmeta){
        super(id);
        this.opisPredmeta = opisPredmeta;
    }
    public static class PredmetDoniranjaBuilder {
        private Long id;
        private String opisPredmeta;

        public PredmetDoniranjaBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public PredmetDoniranjaBuilder setOpisPredmeta(String opisPredmeta) {
            this.opisPredmeta = opisPredmeta;
            return this;
        }

        public PredmetDoniranja createPredmetDoniranja() {
            return new PredmetDoniranja(id, opisPredmeta);
        }

    }
    @Override
    public String toString() {
        return opisPredmeta;
    }
}
