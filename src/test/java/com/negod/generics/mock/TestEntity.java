/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.mock;

import com.negod.generics.persistence.entity.GenericEntity;
import java.util.Date;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 *
 * @author Joakim Johansson ( joakimjohansson@outlook.com )
 */
public class TestEntity extends GenericEntity{

    @Field(name = "name")
    private String name;
    private Integer integerValue;
    private Double doubleValue;
    private Long longValue;
    private Boolean booleanValue;
    private String id;
    private Date updatedDate;
    private Long internalId;
    @IndexedEmbedded
    private TestEntityEmbedded entity;

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getInternalId() {
        return internalId;
    }

    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStringValue() {
        return name;
    }

    public void setStringValue(String stringValue) {
        this.name = stringValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {
        this.integerValue = integerValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public TestEntityEmbedded getEntity() {
        return entity;
    }

    public void setEntity(TestEntityEmbedded entity) {
        this.entity = entity;
    }

}
