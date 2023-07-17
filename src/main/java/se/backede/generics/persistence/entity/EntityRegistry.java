/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SetAttribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.backede.generics.persistence.CacheHelper;

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

        entities.forEach((EntityType<?> entity) -> {
            Map<String, Class> entityFields = new HashMap<>();
            Set<? extends Attribute<?, ?>> declaredAttributes = entity.getDeclaredAttributes();

            declaredAttributes.forEach(declaredAttribute -> {
                if (declaredAttribute.isCollection()) {
                    SetAttribute<?, ?> set = entity.getSet(declaredAttribute.getName());
                    entityFields.put(declaredAttribute.getName(), set.getElementType().getJavaType());
                } else {
                    entityFields.put(declaredAttribute.getName(), declaredAttribute.getJavaType());
                }
            });
            registeredEntities.add(entity.getJavaType());
            entitiesToRegister.put(entity.getJavaType(), entityFields);
        });

        entitiesToRegister.entrySet().stream().map(entry -> {
            CacheHelper.getInstance().getEnrityRegistryCache().put(entry.getKey(), entry.getValue());
            return entry;
        }).forEachOrdered(entry -> {
            log.debug("EntityClass {} with corresponding fields registered [ DatabaseLayer ] method:extractSearchFields", entry.getKey().getSimpleName());
        });
    }

}
