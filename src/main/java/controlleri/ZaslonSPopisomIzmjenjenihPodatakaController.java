package controlleri;

import hr.java.vjezbe.entitet.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import util.Datoteke;
import util.FXUtil;
import util.PromjenaPodataka;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ZaslonSPopisomIzmjenjenihPodatakaController<T extends Serializable, Z extends KorisnickiPodatci> {
    @FXML
    TableView<PromjenaPodataka<T,Z>> promjenaPodatakaTableView;
    @FXML
    TableColumn<PromjenaPodataka<T,Z>, String> promijenjeniPodatak;
    @FXML
    TableColumn<PromjenaPodataka<T,Z>, String> staraVrijednostColumn;
    @FXML
    TableColumn<PromjenaPodataka<T,Z>, String> novaVrijednostColumn;
    @FXML
    TableColumn<PromjenaPodataka<T,Z>, String> rolaColumn;
    @FXML
    TableColumn<PromjenaPodataka<T,Z>, String> vrijemePromjeneColumn;
    public void initialize(){
        List<PromjenaPodataka<T,Z>> listaPromjena = Datoteke.deserijalizirajPromjene();
        promijenjeniPodatak.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getPromijenjeniPodatak()));
        promijenjeniPodatak.setCellFactory(tc -> {
            TableCell<PromjenaPodataka<T,Z>, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(promijenjeniPodatak.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        staraVrijednostColumn.setCellValueFactory(s -> {
            Optional<T> staraVrijednost = Optional.ofNullable(s.getValue().getStaraVrijednost());
            return staraVrijednost.map(t -> {
                if(t instanceof Donor){
                    return new SimpleStringProperty(((Donor)t).tablicniString());
                }
                else if(t instanceof PrimateljDonacije){
                    return new SimpleStringProperty(((PrimateljDonacije)t).tablicniString());
                }
                else if(t instanceof Korisnik){
                    return new SimpleStringProperty(((Korisnik)t).tablicniString());
                }
                else if(t instanceof Donacija){
                    return new SimpleStringProperty(((Donacija)t).tablicniString());
                }
                else if(t instanceof Hrana){
                    return new SimpleStringProperty(((Hrana)t).tablicniString());
                }
                else if(t instanceof Odjeca){
                    return new SimpleStringProperty(((Odjeca)t).tablicniString());
                }
                return new SimpleStringProperty(t.toString());
            }).orElseGet(() -> new SimpleStringProperty(""));
        });
        novaVrijednostColumn.setCellValueFactory(s -> {
            Optional<T> novaVrijednost = Optional.ofNullable(s.getValue().getNovaVrijednost());
            return novaVrijednost.map(t -> {
                if(t instanceof Donor){
                    return new SimpleStringProperty(((Donor)t).tablicniString());
                }
                else if(t instanceof PrimateljDonacije){
                    return new SimpleStringProperty(((PrimateljDonacije)t).tablicniString());
                }
                else if(t instanceof Korisnik){
                    return new SimpleStringProperty(((Korisnik)t).tablicniString());
                }
                else if(t instanceof Donacija){
                    return new SimpleStringProperty(((Donacija)t).tablicniString());
                }
                else if(t instanceof Hrana){
                    return new SimpleStringProperty(((Hrana)t).tablicniString());
                }
                else if(t instanceof Odjeca){
                    return new SimpleStringProperty(((Odjeca)t).tablicniString());
                }
                return new SimpleStringProperty(t.toString());
            }).orElseGet(() -> new SimpleStringProperty(""));
        });
        rolaColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getRola().getClass().getSimpleName() + ": " + s.getValue().getRola().getKorisnickoIme()));
        vrijemePromjeneColumn.setCellValueFactory(s->new SimpleStringProperty(s.getValue().getVrijemePromjene().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm"))));
        staraVrijednostColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        novaVrijednostColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        rolaColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        vrijemePromjeneColumn.setCellFactory(tc -> FXUtil.createWrappingCell());
        promjenaPodatakaTableView.setItems(FXCollections.observableArrayList(listaPromjena));
    }
}
