/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import se.backede.generics.persistence.entity.GenericEntity;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Table(name = "TEST_ENTITY_EMBEDDED", schema = "TEST")
@Entity
public class TestEntityEmbedded extends GenericEntity {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
