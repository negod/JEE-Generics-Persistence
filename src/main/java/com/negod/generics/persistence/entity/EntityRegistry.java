/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence.entity;

import com.negod.generics.persistence.exception.DaoException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SetAttribute;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.hibernate.annotations.Cache;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Data
@Slf4j
public abstract class EntityRegistry {

    abstract public EntityManager getEntityManager();

    protected void registerEnties() {

        Map<Class, Map<String, Class>> entitiesToRegister = new HashMap<>();
        Set<EntityType<?>> entities = getEntityManager().getMetamodel().getEntities();
        for (EntityType<?> entity : entities) {

            Map<String, Class> entityFields = new HashMap<>();
            Set<? extends Attribute<?, ?>> declaredAttributes = entity.getDeclaredAttributes();

            for (Attribute<?, ?> declaredAttribute : declaredAttributes) {

                if (declaredAttribute.isCollection()) {
                    SetAttribute<?, ?> set = entity.getSet(declaredAttribute.getName());
                    entityFields.put(declaredAttribute.getName(), set.getElementType().getJavaType());
                } else {
                    entityFields.put(declaredAttribute.getName(), declaredAttribute.getJavaType());
                }
            }
            entitiesToRegister.put(entity.getJavaType(), entityFields);
        }

        net.sf.ehcache.Cache entityCache = getEntityNameCache();

        if (!entityCache.isDisabled()) {
            for (Entry<Class, Map<String, Class>> entry : entitiesToRegister.entrySet()) {
                entityCache.put(new Element(entry.getKey(), entry.getValue()));
            }
        }

    }

    private net.sf.ehcache.Cache getEntityNameCache() {
        CacheManager cache = CacheManager.getInstance();
        if (!cache.cacheExists(DefaultCacheNames.ENTITY_REGISTRY_CACHE)) {
            cache.addCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);
            net.sf.ehcache.Cache ehCache = cache.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);
            ehCache.getCacheConfiguration().setEternal(true);
        }
        return cache.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);
    }

}
