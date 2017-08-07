/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics;

import com.negod.generics.persistence.TypeCheck;
import com.negod.generics.persistence.exception.TypeCastException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import com.negod.generics.mock.TestEntity;
import com.negod.generics.mock.EntityMock;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Joakim Johansson ( joakimjohansson@outlook.com )
 */
@RunWith(value = Parameterized.class)
public class TestTypeCheck {

    @Parameter(value = 0)
    public Object BOOLEAN_VALUE;
    
    @Parameter(value = 1)
    public Object DOUBLE_VALUE;
    @Parameter(value = 2)
    public Object INTEGER_VALUE;
    @Parameter(value = 3)
    public Object LONG_VALUE;
    @Parameter(value = 4)
    public Object UPDATED_DATE;
    @Parameter(value = 5)
    public Object STRING_VALUE;
    @Parameter(value = 6)
    public Object ENTITY;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public TestTypeCheck() {
    }

    @Parameters(name = "{index} test for {0}")
    public static Collection<Object[]> data() {
        Object[] objectArray = new Object[]{
            EntityMock.BOOLEAN_VALUE,
            EntityMock.DOUBLE_VALUE,
            EntityMock.INTEGER_VALUE,
            EntityMock.LONG_VALUE,
            EntityMock.UPDATED_DATE,
            EntityMock.STRING_VALUE,
            EntityMock.getEntity(),
        };
        return Arrays.<Object[]>asList(objectArray);
    }
    
    

    /**
     * Test of getAsObject method, of class TypeCheck.
     *
     * @throws com.negod.generics.persistence.exception.TypeCastException
     */
    @Test
    public void testGetAsObject() throws TypeCastException {
        System.out.println("getAsObject");
        TypeCheck instance = new TypeCheck(ENTITY);
        Optional<TestEntity> asObject = instance.getAsObject();
        assertTrue(asObject.isPresent());
        assertTrue(ENTITY.equals(asObject.get()));
    }

    @Test
    public void testGetAsObjectNull() throws TypeCastException {
        System.out.println("getAsObjectNull");
        TestEntity entity = EntityMock.getEntity();
        TypeCheck instance = new TypeCheck(null);
        Optional<TestEntity> asObject = instance.getAsObject();
        assertFalse(asObject.isPresent());
    }

    @Test
    public void testGetAsObjectWrongObject() throws TypeCastException {
        System.out.println("testGetAsObjectWrongObject");
        Integer project = 7;
        TestEntity entity = EntityMock.getEntity();
        TypeCheck instance = new TypeCheck(project);
        Optional<TestEntity> asObject = instance.getAsObject();
        assertTrue(asObject.isPresent());
    }

    /**
     * Test of getAsLong method, of class TypeCheck.
     *
     * @throws com.negod.generics.persistence.exception.TypeCastException
     */
    @Test
    public void testGetAsLong() throws TypeCastException {
        System.out.println("getAsLong");
        TypeCheck instance = new TypeCheck(LONG_VALUE);
        Optional<Long> asObject = instance.getAsObject();
        assertTrue(asObject.isPresent());
        assertTrue(LONG_VALUE.equals(asObject.get()));
    }

    /**
     * Test of getAsString method, of class TypeCheck.
     *
     * @throws com.negod.generics.persistence.exception.TypeCastException
     */
    @Test
    public void testGetAsString() throws TypeCastException {
        System.out.println("getAsString");
        TypeCheck instance = new TypeCheck(STRING_VALUE);
        Optional<String> asObject = instance.getAsObject();
        assertTrue(asObject.isPresent());
        assertTrue(STRING_VALUE.equals(asObject.get()));
    }

    /**
     * Test of getAsInteger method, of class TypeCheck.
     *
     * @throws com.negod.generics.persistence.exception.TypeCastException
     */
    @Test
    public void testGetAsInteger() throws TypeCastException {
        System.out.println("getAsInteger");
        TypeCheck instance = new TypeCheck(INTEGER_VALUE);
        Optional<Integer> asObject = instance.getAsObject();
        assertTrue(asObject.isPresent());
        assertTrue(INTEGER_VALUE.equals(asObject.get()));
    }

    /**
     * Test of getAsDouble method, of class TypeCheck.
     *
     * @throws com.negod.generics.persistence.exception.TypeCastException
     */
    @Test
    public void testGetAsDouble() throws TypeCastException {
        System.out.println("getAsDouble");
        TypeCheck instance = new TypeCheck(DOUBLE_VALUE);
        Optional<Double> asObject = instance.getAsObject();
        assertTrue(asObject.isPresent());
        assertTrue(DOUBLE_VALUE.equals(asObject.get()));
    }

}
