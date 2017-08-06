/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence.mapper;

import com.negod.generics.persistence.entity.DefaultCacheNames;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import static org.dozer.loader.api.TypeMappingOptions.mapEmptyString;
import static org.dozer.loader.api.TypeMappingOptions.mapNull;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class Mapper {

    private static final DozerBeanMapper MAPPER = new DozerBeanMapper();
    private static final Mapper INSTANCE = new Mapper();

    public static Mapper getInstance() {
        return INSTANCE;
    }

    protected Mapper() {
        MAPPER.addMapping(beanMappingBuilder());
    }

    private BeanMappingBuilder beanMappingBuilder() {
        return new BeanMappingBuilder() {
            @Override
            protected void configure() {
                log.debug("Configuring Mapper for Entityclasses");
                CacheManager manager = CacheManager.getInstance();
                Optional<Cache> cache = Optional.ofNullable(manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE));
                if (cache.isPresent()) {
                    List keys = cache.get().getKeys();
                    for (Object key : keys) {
                        Class<?> clazz = (Class) key;
                        mapping(clazz, clazz, mapNull(false), mapEmptyString(false)).exclude("id").exclude("updatedDate");
                    }
                } else {
                    log.error("GenericMapper: Cache [ entity_registry ] not initialized!, Continuing....");
                }
            }
        };
    }

    public DozerBeanMapper getMapper() {
        return MAPPER;
    }

}
