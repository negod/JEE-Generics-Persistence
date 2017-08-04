/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence.mapper;

import com.negod.generics.persistence.entity.GenericEntity;
import static java.lang.Math.log;
import java.util.Optional;
import static javafx.scene.input.KeyCode.D;
import lombok.extern.slf4j.Slf4j;
import org.dozer.loader.api.BeanMappingBuilder;
import static org.dozer.loader.api.TypeMappingOptions.mapEmptyString;
import static org.dozer.loader.api.TypeMappingOptions.mapNull;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public abstract class DaoMapper<T extends GenericEntity> {

    private final Class<T> entityClass;

    public DaoMapper(Class<T> entityClass) {
        this.entityClass = entityClass;
        Mapper.getInstance().getMapper().addMapping(beanMappingBuilder());
    }

    private BeanMappingBuilder beanMappingBuilder() {
        return new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(entityClass, entityClass, mapNull(false), mapEmptyString(false));
            }
        };
    }

    public Optional<T> map(T fromEntity, T toEntity) {
        try {
            Mapper.getInstance().getMapper().map(fromEntity, toEntity);
            return Optional.ofNullable(toEntity);
        } catch (Exception e) {
            log.error("[ Failed to update entity {} from dto {} [ ENTITY_ID: {} ] Error : {}", fromEntity.toString(), toEntity.toString(), toEntity.getId(), e);
            return Optional.empty();
        }
    }

}
