/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics;

import com.negod.generics.mock.service.DomainEntity;
import com.negod.generics.mock.service.ServiceDetailEntity;
import com.negod.generics.mock.service.ServiceEntity;
import com.negod.generics.mock.service.UserEntity;
import com.negod.generics.persistence.PersistenceUnitTest;
import com.negod.generics.persistence.entity.DefaultCacheNames;
import com.negod.generics.persistence.entity.EntityRegistry;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
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
    }

    @Test
    public void test() {
        CacheManager manager = CacheManager.getInstance();
        Cache cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);

        assert cache.getKeys().size() == 4;
        assert cache.isKeyInCache(ServiceEntity.class);
        assert cache.isKeyInCache(DomainEntity.class);
        assert cache.isKeyInCache(ServiceDetailEntity.class);
        assert cache.isKeyInCache(UserEntity.class);
    }

    @Test
    public void testServiceEntity() {
        CacheManager manager = CacheManager.getInstance();
        Cache cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);

        Element get = cache.get(ServiceEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get.getValue();

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
        CacheManager manager = CacheManager.getInstance();
        Cache cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);

        Element get = cache.get(DomainEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get.getValue();

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("services");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("services").equals(ServiceEntity.class);
    }

    @Test
    public void testUserEntity() {
        CacheManager manager = CacheManager.getInstance();
        Cache cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);

        Element get = cache.get(UserEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get.getValue();

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("services");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("services").equals(ServiceEntity.class);
    }

    @Test
    public void testServiceDetailEntity() {
        CacheManager manager = CacheManager.getInstance();
        Cache cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);

        Element get = cache.get(ServiceDetailEntity.class);
        HashMap<String, Class> cachedData = (HashMap<String, Class>) get.getValue();

        assert cachedData.size() == 2;

        assert cachedData.containsKey("name");
        assert cachedData.containsKey("service");

        assert cachedData.get("name").equals(String.class);
        assert cachedData.get("service").equals(ServiceEntity.class);
    }

}
