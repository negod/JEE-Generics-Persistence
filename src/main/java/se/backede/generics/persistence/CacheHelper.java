/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package se.backede.generics.persistence;

import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import se.backede.generics.persistence.entity.DefaultCacheNames;

/**
 *
 * @author joaki
 */
public class CacheHelper {

    private final CacheManager cacheManager;
    @Getter
    private final Cache<Class, Map> enrityRegistryCache;
    @Getter
    private static CacheHelper instance;

    private CacheHelper() {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();

        enrityRegistryCache = cacheManager
                .createCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE, CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(
                                Class.class, Map.class,
                                ResourcePoolsBuilder.heap(10)));
    }

    public CacheHelper createCacheIfNotExistent(String name, Object key, Object value) {

        Cache<? extends Object, ? extends Object> cache = cacheManager.getCache(name, key.getClass(), value.getClass());
        if (cache == null) {
            cacheManager.createCache(name, CacheConfigurationBuilder
                    .newCacheConfigurationBuilder(key.getClass(), value.getClass(),
                            ResourcePoolsBuilder.heap(10)));
        }

        return instance;
    }

    public static synchronized CacheHelper getInstance() {
        if (instance == null) {
            instance = new CacheHelper();
        }
        return instance;
    }

}
