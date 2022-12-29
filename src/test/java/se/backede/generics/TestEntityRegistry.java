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
import java.util.Set;
import javax.persistence.EntityManager;
import javax.xml.bind.Element;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

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
        CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().withCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, getConfiguration()).build(Boolean.TRUE);
        Cache<Class, Set> cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Set.class);

        Iterator<Cache.Entry<Class, Set>> iterator = cache.iterator();

        int cacheSize = ((Collection<?>) iterator).size();

        assert cacheSize == 4;
        assert cache.containsKey(ServiceEntity.class);
        assert cache.containsKey(DomainEntity.class);
        assert cache.containsKey(ServiceDetailEntity.class);
        assert cache.containsKey(UserEntity.class);
    }

    @Test
    public void testServiceEntity() {

        CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().withCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, getConfiguration()).build(Boolean.TRUE);
        Cache<Class, Set> cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Set.class);

        Set get = cache.get(ServiceEntity.class);
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
        CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().withCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, getConfiguration()).build(Boolean.TRUE);
        Cache<Class, Set> cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Set.class);

        Set get = cache.get(DomainEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.entrySet().size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("services");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("services").equals(ServiceEntity.class);
    }

    @Test
    public void testUserEntity() {
        CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().withCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, getConfiguration()).build(Boolean.TRUE);
        Cache<Class, Set> cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Set.class);

        Set get = cache.get(UserEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("services");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("services").equals(ServiceEntity.class);
    }

    @Test
    public void testServiceDetailEntity() {
        CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().withCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, getConfiguration()).build(Boolean.TRUE);
        Cache<Class, Set> cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, Class.class, Set.class);

        Set get = cache.get(ServiceDetailEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get;

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("service");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("service").equals(ServiceEntity.class);
    }

}
