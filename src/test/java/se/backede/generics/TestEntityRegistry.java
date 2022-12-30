/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics;

import se.backede.generics.mock.service.DomainEntity;
import se.backede.generics.mock.service.ServiceDetailEntity;
import se.backede.generics.mock.service.ServiceEntity;
import se.backede.generics.mock.service.UserEntity;
import se.backede.generics.persistence.PersistenceUnitTest;
import se.backede.generics.persistence.entity.EntityRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;

import org.junit.Test;
import se.backede.generics.persistence.CacheHelper;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class TestEntityRegistry extends EntityRegistry {

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    public TestEntityRegistry() {
        super.registerEnties();
        super.registerSearchFields();
        super.registerSearchFieldCaches();
    }

    @Test
    public void test() {
        Cache<Class, Set> searchFieldCache = CacheHelper.getInstance().getSearchFieldCache();

        assert searchFieldCache != null;

        long cacheSize = StreamSupport.stream(searchFieldCache.spliterator(), false).count();

        assert cacheSize == 4;
        assert searchFieldCache.containsKey(ServiceEntity.class);
        assert searchFieldCache.containsKey(DomainEntity.class);
        assert searchFieldCache.containsKey(ServiceDetailEntity.class);
        assert searchFieldCache.containsKey(UserEntity.class);
    }

    @Test
    public void testServiceEntity() {

        Cache<Class, Map> enrityRegistryCache = CacheHelper.getInstance().getEnrityRegistryCache();

        assert enrityRegistryCache != null;
        Map get = (Map) enrityRegistryCache.get(ServiceEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.size() == 4;

        assert cachedData.containsKey("domain");
        assert cachedData.containsKey("name");
        assert cachedData.containsKey("detail");
        assert cachedData.containsKey("users");

        assert cachedData.get("domain").equals(DomainEntity.class);
        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("detail").equals(ServiceDetailEntity.class);
        assert cachedData.get("users").equals(UserEntity.class);
    }

    @Test
    public void testDomainEntity() {
        Cache<Class, Map> enrityRegistryCache = CacheHelper.getInstance().getEnrityRegistryCache();

        assert enrityRegistryCache != null;
        Map get = (Map) enrityRegistryCache.get(DomainEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.entrySet().size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("services");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("services").equals(ServiceEntity.class);
    }

    @Test
    public void testUserEntity() {
        Cache<Class, Map> enrityRegistryCache = CacheHelper.getInstance().getEnrityRegistryCache();

        assert enrityRegistryCache != null;
        Map get = (Map) enrityRegistryCache.get(UserEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("services");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("services").equals(ServiceEntity.class);
    }

    @Test
    public void testServiceDetailEntity() {

        Cache<Class, Map> enrityRegistryCache = CacheHelper.getInstance().getEnrityRegistryCache();

        assert enrityRegistryCache != null;
        Map get = (Map) enrityRegistryCache.get(ServiceDetailEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("service");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("service").equals(ServiceEntity.class);
    }

}
