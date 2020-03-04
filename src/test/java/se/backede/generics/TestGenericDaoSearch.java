/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics;

import static se.backede.generics.mock.EntityMock.GUID;
import se.backede.generics.mock.ServiceEntitiesMock;
import se.backede.generics.mock.service.ServiceEntity;
import se.backede.generics.persistence.CacheInitializer;
import se.backede.generics.persistence.DomainEntityDao;
import se.backede.generics.persistence.PersistenceUnitTest;
import se.backede.generics.persistence.ServiceEntityDao;
import se.backede.generics.persistence.UserEntityDao;
import se.backede.generics.persistence.exception.ConstraintException;
import se.backede.generics.persistence.exception.DaoException;
import se.backede.generics.persistence.search.GenericFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class TestGenericDaoSearch extends ServiceEntityDao {

    DomainEntityDao DOMAIN_DAO;
    UserEntityDao USER_DAO;
    static CacheInitializer CACHE;

    String[] serviceEntityNames = new String[]{"NAME1", "name2", "n2ame", "Company1", "AbCd", "backede ab"};
    String[] serviceEntityNamesInteger = new String[]{"1234", "4567", "7890", "9876", "5432", "1098"};
    String[] userEntityNames = new String[]{"NAME1", "name2", "n2ame", "Company1", "AbCd", "backede ab"};
    String[] domainEntityNames = new String[]{"NAME1", "name2", "n2ame", "Company1", "AbCd", "backede ab"};

    String[] serviceEntitySearchFields = new String[]{"detail.name", "users.name", "domain.name", "name"};

    public TestGenericDaoSearch() throws DaoException {
    }

    @BeforeClass
    public static void initClass() {
        CACHE = new CacheInitializer();
    }

    @Before
    public void init() {
        USER_DAO = new UserEntityDao();
        DOMAIN_DAO = new DomainEntityDao();
        clearDb();
    }

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    @Test
    public void assertFields() throws DaoException {
        log.debug("Asserting fields");
        assert getEntityClass().equals(ServiceEntity.class);
        assert getClassName().equals(ServiceEntity.class.getSimpleName());
        assert getSearchFields().equals(new HashSet<>(Arrays.asList(serviceEntitySearchFields)));
    }

    private void clearDb() {
        startTransaction();
        int disconnectDomains = getEntityManager().createQuery("UPDATE ServiceEntity set domain_id = null").executeUpdate();
        commitTransaction();

        startTransaction();
        int domain = getEntityManager().createQuery("DELETE from DomainEntity").executeUpdate();
        commitTransaction();

        startTransaction();
        int user = getEntityManager().createQuery("DELETE from UserEntity").executeUpdate();
        commitTransaction();

        startTransaction();
        int service = getEntityManager().createQuery("DELETE from ServiceEntity").executeUpdate();
        commitTransaction();
    }

    public void addNewValuesToDb(String[] names) throws DaoException, ConstraintException {
        for (String name : names) {
            ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
            entity.setName(name);
            startTransaction();
            Optional<ServiceEntity> persistedEntity = persist(entity);
            commitTransaction();
        }
    }

    @Test
    public void testSearchServiceEntityWildcard() throws DaoException, ConstraintException {
        log.debug("Test Search Service Entity.");

        addNewValuesToDb(userEntityNames);

        Optional<Set<ServiceEntity>> all = getAll();
        assert all.isPresent();
        assert all.get().size() == serviceEntityNames.length;

        //Try wildcard search with one letter
        String SEARCHFIELD = "name";
        String SEARCHWORD = "n*";
        Integer LIST_SIZE = 100;
        Integer PAGE = 0;

        Set<String> searchFields = new HashSet<>();
        searchFields.add(SEARCHFIELD);

        GenericFilter filter = ServiceEntitiesMock.getGenericFilter(searchFields, SEARCHWORD, LIST_SIZE, PAGE);
        Optional<Set<ServiceEntity>> search = search(filter);
        assert search.isPresent();
        Set<ServiceEntity> searchRerult = search.get();
        assertEquals("Size is not 3", 3, searchRerult.size());

        //Try with Capital letter 
        filter.setGlobalSearchWord(StringUtils.capitalize(SEARCHWORD));
        search = search(filter);
        assert search.isPresent();
        searchRerult = search.get();
        assertEquals("Size is not 3", 3, searchRerult.size());

        //Exclude wildcard
        filter.setGlobalSearchWord("n");
        search = search(filter);
        assert search.isPresent();
        searchRerult = search.get();
        assertEquals("List is not empty", true, searchRerult.isEmpty());

    }

    @Test
    public void testSearchServiceEntityNotWildcard() throws DaoException, DaoException, ConstraintException {

        clearDb();

        log.debug("Test Search Service Entity.");

        addNewValuesToDb(userEntityNames);

        Optional<Set<ServiceEntity>> all = getAll();

        assert all.isPresent();

        assert all.get().size() == serviceEntityNames.length;

        //Try wildcard search with one letter
        String SEARCHFIELD = "name";
        String SEARCHWORD = "n*";
        Integer LIST_SIZE = 100;
        Integer PAGE = 0;

        Set<String> searchFields = new HashSet<>();

        searchFields.add(SEARCHFIELD);

        GenericFilter filter = ServiceEntitiesMock.getGenericFilter(searchFields, SEARCHWORD, LIST_SIZE, PAGE);
        Optional<Set<ServiceEntity>> search = search(filter);

        assert search.isPresent();
        Set<ServiceEntity> searchRerult = search.get();

        assertEquals("Size is not 3", 3, searchRerult.size());

        //Try with Capital letter 
        filter.setGlobalSearchWord(StringUtils.capitalize(SEARCHWORD));
        search = search(filter);

        assert search.isPresent();
        searchRerult = search.get();

        assertEquals("Size is not 3", 3, searchRerult.size());

        //Exclude wildcard
        filter.setGlobalSearchWord("name");
        search = search(filter);

        assert search.isPresent();
        searchRerult = search.get();

        assertEquals("List is not empty", true, searchRerult.isEmpty());

    }

    @Test
    public void testSearchWithSpecialChars() throws DaoException, ConstraintException {

        clearDb();
        addNewValuesToDb(serviceEntityNames);

        log.debug("Test Search Service Entity.");

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        entity.setName("GUID-TEST");
        startTransaction();
        Optional<ServiceEntity> persistedEntity = persist(entity);
        commitTransaction();

        String UUID = persistedEntity.get().getId();

        //Try search with whole uuid
        String SEARCHFIELD = "id";
        String SEARCHWORD = UUID;
        Integer LIST_SIZE = 100;
        Integer PAGE = 0;

        Set<String> searchFields = new HashSet<>();

        searchFields.add(SEARCHFIELD);

        GenericFilter filter = ServiceEntitiesMock.getGenericFilter(searchFields, SEARCHWORD, LIST_SIZE, PAGE);
        Optional<Set<ServiceEntity>> search = search(filter);

        assert search.isPresent();
        Set<ServiceEntity> searchRerult = search.get();

        assertEquals("Size is not 1", 1, searchRerult.size());

    }

    @Test
    public void testSearchWithIntegerValues() throws DaoException, ConstraintException {

        clearDb();
        addNewValuesToDb(serviceEntityNamesInteger);

        log.debug("Test Search Service Entity with Integer value.");

        String INtegerValue = "9764";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        entity.setName(INtegerValue);
        startTransaction();
        Optional<ServiceEntity> persistedEntity = persist(entity);
        commitTransaction();

        //Try search with whole uuid
        String SEARCHFIELD = "name";
        String SEARCHWORD = INtegerValue;
        Integer LIST_SIZE = 100;
        Integer PAGE = 0;

        Set<String> searchFields = new HashSet<>();

        searchFields.add(SEARCHFIELD);

        GenericFilter filter = ServiceEntitiesMock.getGenericFilter(searchFields, SEARCHWORD, LIST_SIZE, PAGE);
        Optional<Set<ServiceEntity>> search = search(filter);

        assert search.isPresent();
        Set<ServiceEntity> searchRerult = search.get();

        assertEquals("Size is not 1", 1, searchRerult.size());

    }

}
