/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.update;

import java.io.Serializable;
import lombok.Data;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Data
public class ObjectUpdate implements Serializable{

    private String objectId;
    private String object;
    private UpdateType type;

}
