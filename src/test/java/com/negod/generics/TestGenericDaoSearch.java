/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics;

import com.negod.generics.mock.ServiceEntitiesMock;
import com.negod.generics.mock.service.ServiceEntity;
import com.negod.generics.persistence.CacheInitializer;
import com.negod.generics.persistence.ServiceEntityDao;
import com.negod.generics.persistence.exception.DaoException;
import com.negod.generics.persistence.search.GenericFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class TestGenericDaoSearch extends ServiceEntityDao {

    CacheInitializer CACHE = new CacheInitializer();

    String[] serviceEntityNames = new String[]{"NAME1", "name2", "n2ame", "Company1", "AbCd", "backede ab"};
    String[] userEntityNames = new String[]{"NAME1", "name2", "n2ame", "Company1", "AbCd", "backede ab"};
    String[] domainEntityNames = new String[]{"NAME1", "name2", "n2ame", "Company1", "AbCd", "backede ab"};

    String[] serviceEntitySearchFields = new String[]{"detail.name", "users.name", "domain.name", "name"};

    public TestGenericDaoSearch() throws DaoException {
        CACHE.init();
    }

    @Test
    public void assertFields() throws DaoException {
        log.debug("Asserting fields");
        assert getEntityClass().equals(ServiceEntity.class);
        assert getClassName().equals(ServiceEntity.class.getSimpleName());
        assert getSearchFields().equals(new HashSet<>(Arrays.asList(serviceEntitySearchFields)));
    }

    @Before
    public void init() throws DaoException {
        clearDb();
    }

    private void clearDb() {
        getEntityManager().getTransaction().begin();
        int disconnectDomains = getEntityManager().createQuery("UPDATE ServiceEntity set domain_id = null").executeUpdate();
        getEntityManager().getTransaction().commit();

        getEntityManager().getTransaction().begin();
        int domain = getEntityManager().createQuery("DELETE from DomainEntity").executeUpdate();
        getEntityManager().getTransaction().commit();

        getEntityManager().getTransaction().begin();
        int user = getEntityManager().createQuery("DELETE from UserEntity").executeUpdate();
        getEntityManager().getTransaction().commit();

        getEntityManager().getTransaction().begin();
        int service = getEntityManager().createQuery("DELETE from ServiceEntity").executeUpdate();
        getEntityManager().getTransaction().commit();
    }

    public void addNewValuesToDb(String[] names) throws DaoException {
        for (String name : names) {
            ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
            entity.setName(name);
            getEntityManager().getTransaction().begin();
            Optional<ServiceEntity> persistedEntity = persist(entity);
            getEntityManager().getTransaction().commit();
        }
    }

    @Test
    public void testSearchServiceEntityWildcard() throws DaoException {
        log.debug("Test Search Service Entity.");

        addNewValuesToDb(userEntityNames);

        Optional<List<ServiceEntity>> all = getAll();
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
        Optional<List<ServiceEntity>> search = search(filter);
        assert search.isPresent();
        List<ServiceEntity> searchRerult = search.get();
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

}
