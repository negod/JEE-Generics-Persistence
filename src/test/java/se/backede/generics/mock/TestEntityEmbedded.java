/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock;

import se.backede.generics.persistence.entity.GenericEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Table(name = "TEST_ENTITY_EMBEDDED", schema = "TEST")
@Entity
public class TestEntityEmbedded extends GenericEntity {

    @FullTextField
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
