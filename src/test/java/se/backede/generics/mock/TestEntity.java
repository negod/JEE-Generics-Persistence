/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import se.backede.generics.persistence.entity.GenericEntity;
import lombok.Data;

/**
 *
 * @author Joakim Johansson ( joakimjohansson@outlook.com )
 */
@Data
@Entity
@Table(name = "TEST_ENTITY", schema = "TEST")
public class TestEntity extends GenericEntity {

    private String name;
    private Integer integerValue;
    private Double doubleValue;
    private Long longValue;
    private Boolean booleanValue;

}
