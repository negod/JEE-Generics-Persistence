/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.backede.generics.mock.UserEntityMock;
import se.backede.generics.mock.service.UserEntity;
import se.backede.generics.persistence.CacheInitializer;
import se.backede.generics.persistence.PersistenceUnitTest;
import se.backede.generics.persistence.UserEntityDao;
import se.backede.generics.persistence.exception.DaoException;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class TestGenericDaoPersist extends UserEntityDao {

    static CacheInitializer CACHE;
    private static final Integer USER_AMOUNT = 10;

    public TestGenericDaoPersist() throws DaoException {
    }

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    @Before
    public void start() {
        clearDb();
    }

    @BeforeClass
    public static void init() {
        CACHE = new CacheInitializer();
    }

    private void clearDb() {
        startTransaction();
        int user = getEntityManager().createQuery("DELETE from UserEntity").executeUpdate();
        commitTransaction();
    }

    @Test
    public void testPersistBatch() {
        Set<UserEntity> userEntitySet = UserEntityMock.getUserEntitySet(USER_AMOUNT);

        super.startTransaction();
        Optional<Boolean> persist = super.persist(userEntitySet);
        super.commitTransaction();

        assertTrue(persist.isPresent());

        Optional<Set<UserEntity>> all = super.getAll();
        assertTrue(all.isPresent());
        assertEquals("There should be 10 posts inserted", USER_AMOUNT.longValue(), super.getAll().get().size());

    }

}
