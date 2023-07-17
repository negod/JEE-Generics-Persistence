/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import se.backede.generics.persistence.CacheInitializer;
import se.backede.generics.persistence.DomainEntityDao;
import se.backede.generics.persistence.PersistenceUnitTest;
import se.backede.generics.persistence.UserEntityDao;
import se.backede.generics.persistence.exception.DaoException;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class TestGenericDaoGet extends UserEntityDao {

    static CacheInitializer CACHE;
    DomainEntityDao DOMAIN_DAO = new DomainEntityDao();
    UserEntityDao USER_DAO = new UserEntityDao();

    public TestGenericDaoGet() throws DaoException {
    }

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    @BeforeClass
    public static void init() {
        CACHE = new CacheInitializer();
    }

    @Test
    public void testGetAll() {

    }

}
