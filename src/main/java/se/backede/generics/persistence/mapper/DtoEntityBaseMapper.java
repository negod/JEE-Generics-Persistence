/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.mapper;

import se.backede.generics.persistence.dto.GenericDto;
import se.backede.generics.persistence.entity.GenericEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class DtoEntityBaseMapper<D extends GenericDto, E extends GenericEntity> {

    Class<E> entityClass;
    Class<D> dtoClass;

    public DtoEntityBaseMapper(Class<D> dtoClass, Class<E> entityClass) {
        this.dtoClass = dtoClass;
        this.entityClass = entityClass;
    }

    public Optional<List<E>> mapToEntityList(List<D> dtoList) {
        List<E> entityList = new ArrayList<>();
        for (D dto : dtoList) {
            Optional<E> entity = mapFromDtoToEntity(dto);
            if (entity.isPresent()) {
                entityList.add(entity.get());
            }
        }
        return Optional.ofNullable(entityList);
    }

    public Optional<List<D>> mapToDtoList(List<E> entityList) {
        List<D> dtoList = new ArrayList<>();
        for (E entity : entityList) {
            Optional<D> dto = mapFromEntityToDto(entity);
            if (dto.isPresent()) {
                dtoList.add(dto.get());
            }

        }
        return Optional.ofNullable(dtoList);
    }

    public Optional<E> mapFromDtoToEntity(D dto) {
        try {
            E entity = Mapper.getInstance().getMapper().map(dto, entityClass);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("[ Failed to map from dto {} to entity {} [ DTO EXT_ID: {} ] Error : {}", dto.getClass().getName(), entityClass.getName(), dto.getId(), e);
            return Optional.empty();
        }
    }

    public Optional<D> mapFromEntityToDto(E entity) {
        try {
            D dto = Mapper.getInstance().getMapper().map(entity, dtoClass);
            return Optional.ofNullable(dto);
        } catch (Exception e) {
            log.error("[ Failed to map from entity {} to dto {} [ ENTITY_ID: {} ] Error : {}", entity.getClass().getName(), dtoClass.getName(), entity.getId(), e);
            return Optional.empty();
        }
    }

    public Optional<E> updateEntityFromDto(E entity, D dto) {
        try {
            Mapper.getInstance().getMapper().map(dto, entity);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("[ Failed to update entity {} from dto {} [ ENTITY_ID: {} ] Error : {}", entity.getClass().getName(), dto.getClass().getName(), entity.getId(), e);
            return Optional.empty();
        }
    }

}
