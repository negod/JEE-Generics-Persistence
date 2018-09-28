/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 *
 * @author Joakim Johansson ( joakimjohansson@outlook.com )
 */
public class EntityMock {

    public static final Boolean BOOLEAN_VALUE = Boolean.TRUE;
    public static final Double DOUBLE_VALUE = 5.5;
    public static final String GUID = UUID.randomUUID().toString();
    public static final Integer INTEGER_VALUE = 1;
    public static final Long LONG_VALUE = 10L;
    public static final Date UPDATED_DATE = new Date();
    public static final Long INTERNAL_ID = 3L;
    public static final String STRING_VALUE = "String value";

    public static TestEntity getEntity() {
        TestEntity entity = new TestEntity();

        entity.setBooleanValue(BOOLEAN_VALUE);
        entity.setDoubleValue(DOUBLE_VALUE);
        entity.setId(GUID);
        entity.setIntegerValue(INTEGER_VALUE);
        entity.setLongValue(LONG_VALUE);
        entity.setUpdatedDate(UPDATED_DATE);

        return entity;
    }

    public static Map<String, String> getEntityKeyValue() {
        Map<String, String> keyValue = new HashMap<>();

        keyValue.put(STRING_PROPERTY_NAME, STRING_VALUE);
        keyValue.put(INTEGER_PROPERTY_NAME, INTEGER_VALUE.toString());
        keyValue.put(DOUBLE_PROPERTY_NAME, DOUBLE_VALUE.toString());
        keyValue.put(LONG_PROPERTY_NAME, LONG_VALUE.toString());
        keyValue.put(BOOLEAN_PROPERTY_NAME, BOOLEAN_VALUE.toString());

        return keyValue;
    }

    public static final String STRING_PROPERTY_NAME = "stringValue";
    public static final String INTEGER_PROPERTY_NAME = "integerValue";
    public static final String DOUBLE_PROPERTY_NAME = "doubleValue";
    public static final String LONG_PROPERTY_NAME = "longValue";
    public static final String BOOLEAN_PROPERTY_NAME = "booleanValue";

}
