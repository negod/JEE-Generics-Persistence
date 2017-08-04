/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence.mapper;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
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
                CacheManager manager = CacheManager.getInstance();
                Cache cache = manager.getCache("entity_registry");
                List keys = cache.getKeys();
                for (Object key : keys) {
                    Class<?> entityClass = (Class) cache.get(key).getValue();
                    mapping(entityClass, entityClass, mapNull(false), mapEmptyString(false));
                }
            }
        };
    }

    public DozerBeanMapper getMapper() {
        return MAPPER;
    }

}
