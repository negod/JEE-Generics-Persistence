/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.mock;

import com.negod.generics.persistence.entity.GenericEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.search.annotations.Field;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Table(name = "TEST_ENTITY_EMBEDDED", schema = "TEST")
@Entity
public class TestEntityEmbedded extends GenericEntity {

    @Field
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
