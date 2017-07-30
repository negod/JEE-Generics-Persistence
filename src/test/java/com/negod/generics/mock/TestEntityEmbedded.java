/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.mock;

import java.util.Date;
import org.hibernate.search.annotations.Field;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class TestEntityEmbedded {

    @Field
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
