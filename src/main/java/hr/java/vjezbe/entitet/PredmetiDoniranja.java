package hr.java.vjezbe.entitet;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PredmetiDoniranja<T extends PredmetDoniranja> implements Serializable {
    @Serial
    private static final long serialVersionUID = 6903595061886803729L;
    private List<T> predmetiDoniranja;

    public PredmetiDoniranja(List<T> predmetiDoniranja) {
        this.predmetiDoniranja = predmetiDoniranja;
    }
    public void dodajPredmetDoniranja(T objekt){
        predmetiDoniranja.add(objekt);
    }
    public T dohvatiPredmetDoniranja(int index){
        return predmetiDoniranja.get(index);
    }
    public List<T> dohvatiSvePredmeteDoniranja(){
        return new ArrayList<>(predmetiDoniranja);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredmetiDoniranja<?> that = (PredmetiDoniranja<?>) o;
        return predmetiDoniranja.equals(that.predmetiDoniranja);
    }

    @Override
    public String toString() {
        StringBuilder array = new StringBuilder();
        if(Optional.ofNullable(this.predmetiDoniranja).isPresent()){
            for (T t : this.predmetiDoniranja) {
                array.append(t).append("\n");
            }
            if(array.length()>0){
                array.deleteCharAt(array.length() - 1);
            }
        }
        return array.toString();
    }
}
