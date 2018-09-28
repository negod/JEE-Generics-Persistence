/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.entity;

import se.backede.generics.persistence.exception.DaoException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SetAttribute;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
@Getter
public abstract class EntityRegistry {

    abstract public EntityManager getEntityManager();

    private Set<Class> registeredEntities = new HashSet<>();
    private Set<String> registeredSearchFields = new HashSet<>();

    public EntityRegistry() {
    }

    public void registerEnties() {
        log.debug("Registering Entities in Cache {} [ DatabaseLayer ] method:extractSearchFields");
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
            registeredEntities.add(entity.getJavaType());
            entitiesToRegister.put(entity.getJavaType(), entityFields);
        }

        net.sf.ehcache.Cache entityCache = getOrCreateCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);

        if (!entityCache.isDisabled()) {
            for (Entry<Class, Map<String, Class>> entry : entitiesToRegister.entrySet()) {
                entityCache.put(new Element(entry.getKey(), entry.getValue()));
                log.debug("EntityClass {} with corresponding fields registered [ DatabaseLayer ] method:extractSearchFields", entry.getKey().getSimpleName());
            }
        } else {
            log.error("Entity Cache disabled or not loaded!");
        }
    }

    public void registerSearchFields() {

        if (registeredEntities.isEmpty()) {
            registerEnties();
        }

        net.sf.ehcache.Cache entityNameCache = getOrCreateCache(DefaultCacheNames.SEARCH_FIELD_CACHE);

        if (!entityNameCache.isDisabled()) {
            for (Class registeredEntity : registeredEntities) {
                try {
                    Set<String> searchFields = extractSearchFields(registeredEntity, null);
                    registeredSearchFields = searchFields;
                    entityNameCache.put(new Element(registeredEntity, searchFields));
                } catch (CacheException | DaoException ex) {
                    log.error("Error when registering searchFields {} [ DatabaseLayer ]", ex);
                }
            }
        } else {
            log.error("Entity Name Cache disabled or not loaded!");
        }
    }

    public void registerSearchFieldCaches() {

        if (registeredSearchFields.isEmpty()) {
            registerSearchFields();
        }

        for (String registeredSearchField : registeredSearchFields) {
            getOrCreateCache(registeredSearchField);
        }

    }

    private Set<String> extractSearchFields(Class<?> entityClass, Set<String> alreadyExtractedClasses) throws DaoException {
        log.debug("Extracting searchfields for entity class {} [ DatabaseLayer ] method:extractSearchFields", entityClass.getSimpleName());
        // Used to avoid StackOverflow One class can only be extracted once
        if (alreadyExtractedClasses == null) {
            alreadyExtractedClasses = new HashSet<>(Arrays.asList(new String[]{entityClass.getName()}));
        }

        try {
            String fieldAnnotation = org.hibernate.search.annotations.Field.class.getName();
            String indexedEmbeddedAnnotation = org.hibernate.search.annotations.IndexedEmbedded.class.getName();
            Set<String> fields = new HashSet<>();
            Field[] declaredFields = entityClass.getDeclaredFields();
            for (Field field : declaredFields) {
                Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {

                    if (annotation.annotationType().getName().equals(fieldAnnotation)) {
                        fields.add(field.getName());
                    }
                    if (annotation.annotationType().getName().equals(indexedEmbeddedAnnotation)) {

                        Class<?> clazz = field.getType();

                        if (clazz.equals(Set.class) || clazz.equals(List.class)) {
                            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                            clazz = (Class<?>) stringListType.getActualTypeArguments()[0];
                        }

                        // To avoid StackOverFlow
                        if (alreadyExtractedClasses.contains(clazz.getName())) {
                            continue;
                        } else {
                            alreadyExtractedClasses.add(clazz.getName());
                        }

                        Object entity = clazz.newInstance();
                        Set<String> extractSearchFields = extractSearchFields(entity.getClass(), alreadyExtractedClasses);

                        for (String extractSearchField : extractSearchFields) {
                            fields.add(field.getName().concat(".").concat(extractSearchField));
                        }
                    }
                }
            }
            return fields;

        } catch (IllegalAccessException | InstantiationException | IllegalArgumentException ex) {
            log.error("Error when extracting searchFields {} [ DatabaseLayer ]", ex);
            throw new DaoException("Error whgen extracting serachFields {}", ex);
        }
    }

    private net.sf.ehcache.Cache getOrCreateCache(String cacheName) {
        CacheManager existingCache = CacheManager.getInstance();
        if (!existingCache.cacheExists(cacheName)) {
            CacheConfiguration config = new CacheConfiguration(cacheName, 10000);
            config.setEternal(true);
            Cache newCache = new Cache(config);
            existingCache.addCache(newCache);
            log.info("New cache created with config  MAX ENTRIES LOCAL HEAP: {} CACHE NAME {} ", config.getMaxEntriesLocalHeap(), cacheName);
        } else {
            log.info("Cache {} already exists cache not added", cacheName);
        }
        return existingCache.getCache(cacheName);
    }

}
