package hr.java.vjezbe.entitet;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public sealed interface Sucelje permits Skladiste{
    default List<PredmetDoniranja> dohvatiHranuPredIstekomRoka(List<PredmetDoniranja> listaPredmeta){
        return listaPredmeta.stream().filter(s-> s instanceof Hrana).filter(s->((Hrana)s).getRokTrajanja().isBefore(LocalDate.now().plusMonths(2))).collect(Collectors.toList());
    }
    default List<PredmetDoniranja> dohvatiHranuSProsjecnimRokomTrajanja(List<PredmetDoniranja> listaPredmeta){
        return listaPredmeta.stream().filter(s->s instanceof Hrana).filter(s->((Hrana)s).getRokTrajanja().isAfter(LocalDate.now().plusMonths(2))).filter(s->((Hrana)s).getRokTrajanja().isBefore(LocalDate.now().plusYears(1))).collect(Collectors.toList());
    }
    default List<PredmetDoniranja> dohvatiHranuSDugimRokomTrajanja(List<PredmetDoniranja> listaPredmeta){
        return listaPredmeta.stream().filter(s->s instanceof Hrana).filter(s->((Hrana)s).getRokTrajanja().isAfter(LocalDate.now().plusYears(1))).collect(Collectors.toList());
    }
}
