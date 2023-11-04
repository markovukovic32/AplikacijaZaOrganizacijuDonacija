package hr.java.vjezbe.entitet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Entitet implements Serializable {
    private Long id;
}
