/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.update;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Data
public class Update implements Serializable {

    List<ObjectUpdate> updates;

}
