/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics;

import java.util.Collection;
import se.backede.generics.mock.service.DomainEntity;
import se.backede.generics.mock.service.ServiceDetailEntity;
import se.backede.generics.mock.service.ServiceEntity;
import se.backede.generics.mock.service.UserEntity;
import se.backede.generics.persistence.PersistenceUnitTest;
import se.backede.generics.persistence.entity.DefaultCacheNames;
import se.backede.generics.persistence.entity.EntityRegistry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;

import org.junit.Test;

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
        Optional<Cache> cache = super.getOrCreateCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Map.class);

        assert cache.isPresent();
        Iterator<Cache.Entry<Class, Set>> iterator = cache.get().iterator();

        int cacheSize = ((Collection<?>) iterator).size();

        assert cacheSize == 4;
        assert cache.get().containsKey(ServiceEntity.class);
        assert cache.get().containsKey(DomainEntity.class);
        assert cache.get().containsKey(ServiceDetailEntity.class);
        assert cache.get().containsKey(UserEntity.class);
    }

    @Test
    public void testServiceEntity() {

        Optional<Cache> cache = super.getOrCreateCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Map.class);

        assert cache.isPresent();
        Map get = (Map) cache.get().get(ServiceEntity.class);
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
        Optional<Cache> cache = super.getOrCreateCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Map.class);

        Map get = (Map) cache.get().get(DomainEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.entrySet().size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("services");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("services").equals(ServiceEntity.class);
    }

    @Test
    public void testUserEntity() {
        Optional<Cache> cache = super.getOrCreateCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Map.class);

        Map get = (Map) cache.get().get(UserEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("services");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("services").equals(ServiceEntity.class);
    }

    @Test
    public void testServiceDetailEntity() {

        Optional<Cache> cache = super.getOrCreateCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Map.class);

        Map get = (Map) cache.get().get(ServiceDetailEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("service");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("service").equals(ServiceEntity.class);
    }

}
