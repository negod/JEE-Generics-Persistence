/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence;

import com.negod.generics.mock.TestEntity;
import com.negod.generics.persistence.entity.GenericEntity;
import com.negod.generics.persistence.exception.DaoException;
import com.negod.generics.persistence.search.GenericFilter;
import com.negod.generics.persistence.search.Pagination;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class GenericDaoIT {

    private final static Logger LOG = Logger.getLogger(GenericDaoIT.class.getName());

    public GenericDaoIT() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getEntityManager method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGetEntityManager() {
        System.out.println("getEntityManager");
        GenericDao instance = null;
        EntityManager expResult = null;
        EntityManager result = instance.getEntityManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of extractSearchFields method, of class GenericDao.
     *
     * @throws com.negod.generics.persistence.exception.DaoException
     */
    @Test
    public void testExtractSearchFields() throws DaoException {
        System.out.println("extractSearchFields");
        GenericDaoImpl instance = new GenericDaoImpl(TestEntity.class);
        Set<String> expResult = new HashSet<>(Arrays.asList(new String[]{"name", "entity.value"}));
        Set<String> result = instance.extractSearchFields(TestEntity.class);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testPersist() throws Exception {
        System.out.println("persist");
        GenericEntity entity = null;
        GenericDao instance = null;
        Optional expResult = null;
        Optional result = instance.persist(entity);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testUpdate() throws Exception {
        System.out.println("update");
        GenericEntity entity = null;
        GenericDao instance = null;
        Optional expResult = null;
        Optional result = instance.update(entity);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testDelete_String() throws Exception {
        System.out.println("delete");
        String externalId = "";
        GenericDao instance = null;
        Boolean expResult = null;
        Boolean result = instance.delete(externalId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testDelete_GenericType() throws Exception {
        System.out.println("delete");
        GenericEntity entity = null;
        GenericDao instance = null;
        Boolean expResult = null;
        Boolean result = instance.delete(entity);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getById method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGetById() throws Exception {
        System.out.println("getById");
        String id = "";
        GenericDao instance = null;
        Optional expResult = null;
        Optional result = instance.getById(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAll method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGetAll_GenericFilter() throws Exception {
        System.out.println("getAll");
        GenericFilter filter = null;
        GenericDao instance = null;
        //Optional<List<>> expResult = null;
        //Optional<List<>> result = instance.getAll(filter);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAll method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGetAll_Pagination() throws Exception {
        System.out.println("getAll");
        Pagination pagination = null;
        GenericDao instance = null;
//        Optional<List<>> expResult = null;
//        Optional<List<>> result = instance.getAll(pagination);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAll method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGetAll_0args() throws Exception {
        System.out.println("getAll");
        GenericDao instance = null;
//        Optional<List<>> expResult = null;
//        Optional<List<>> result = instance.getAll();
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGet() throws Exception {
        System.out.println("get");
        GenericDao instance = null;
        Optional expResult = null;
        Optional result = instance.get(null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeTypedQueryList method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testExecuteTypedQueryList_TypedQuery() throws Exception {
        System.out.println("executeTypedQueryList");
        GenericDao instance = null;
//        Optional<List<>> expResult = null;
//        Optional<List<>> result = instance.executeTypedQueryList(null);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeTypedQueryList method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testExecuteTypedQueryList_TypedQuery_Pagination() throws Exception {
        System.out.println("executeTypedQueryList");
        GenericDao instance = null;
//        Optional<List<>> expResult = null;
//        Optional<List<>> result = instance.executeTypedQueryList(null);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of executeTypedQuery method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testExecuteTypedQuery() throws Exception {
        System.out.println("executeTypedQuery");
        GenericDao instance = null;
        Optional expResult = null;
        Optional result = instance.executeTypedQuery(null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of indexEntity method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testIndexEntity() {
        System.out.println("indexEntity");
        GenericDao instance = null;
        Boolean expResult = null;
        Boolean result = instance.indexEntity();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEntityClass method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGetEntityClass() {
        System.out.println("getEntityClass");
        GenericDao instance = null;
        Class expResult = null;
        Class result = instance.getEntityClass();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getClassName method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGetClassName() {
        System.out.println("getClassName");
        GenericDao instance = null;
        String expResult = "";
        String result = instance.getClassName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSearchFields method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testGetSearchFields() {
        System.out.println("getSearchFields");
        GenericDao instance = null;
        Set<String> expResult = null;
        Set<String> result = instance.getSearchFields();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        GenericDao instance = null;
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canEqual method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testCanEqual() {
        System.out.println("canEqual");
        Object other = null;
        GenericDao instance = null;
        boolean expResult = false;
        boolean result = instance.canEqual(other);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        GenericDao instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class GenericDao.
     */
    @Ignore
    @Test
    public void testToString() {
        System.out.println("toString");
        GenericDao instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /*
    public class GenericDaoImpl extends GenericDao {

        public GenericDaoImpl() throws Exception {
            super(null);
        }

        public EntityManager getEntityManager() {
            return null;
        }
    }*/
}
