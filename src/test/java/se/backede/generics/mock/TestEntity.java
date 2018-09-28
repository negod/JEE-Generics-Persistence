/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock;

import se.backede.generics.persistence.entity.GenericEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.search.annotations.Field;

/**
 *
 * @author Joakim Johansson ( joakimjohansson@outlook.com )
 */
@Data
@Entity
@Table(name = "TEST_ENTITY", schema = "TEST")
public class TestEntity extends GenericEntity {

    @Field(name = "name")
    private String name;
    private Integer integerValue;
    private Double doubleValue;
    private Long longValue;
    private Boolean booleanValue;

//    @IndexedEmbedded
//    private TestEntityEmbedded entity;
//
//    @IndexedEmbedded
//    private Set<TestEntityEmbedded> entitySet;

}
