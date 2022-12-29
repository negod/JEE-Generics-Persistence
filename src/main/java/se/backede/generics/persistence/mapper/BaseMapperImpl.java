/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.backede.generics.persistence.dto.GenericDto;
import se.backede.generics.persistence.entity.GenericEntity;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class BaseMapperImpl<D extends GenericDto, E extends GenericEntity> {

    Class<E> entityClass;
    Class<D> dtoClass;

    public BaseMapperImpl(Class<D> dtoClass, Class<E> entityClass) {
        this.dtoClass = dtoClass;
        this.entityClass = entityClass;
    }

    public Optional<List<GenericEntity>> mapToEntityList(List<D> dtoList) {
        List<GenericEntity> entityList = new ArrayList<>();
        for (D dto : dtoList) {
            Optional<GenericEntity> entity = mapFromDtoToEntity(dto);
            if (entity.isPresent()) {
                entityList.add(entity.get());
            }
        }
        return Optional.ofNullable(entityList);
    }

    public Optional<List<GenericDto>> mapToDtoList(List<E> entityList) {
        List<GenericDto> dtoList = new ArrayList<>();
        for (E entity : entityList) {
            Optional<GenericDto> dto = mapFromEntityToDto(entity);
            if (dto.isPresent()) {
                dtoList.add(dto.get());
            }

        }
        return Optional.ofNullable(dtoList);
    }

    public Optional<GenericEntity> mapFromDtoToEntity(D dto) {
        try {
            GenericEntity entity = Mapper.getInstance().map(dto);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("[ Failed to map from dto {} to entity {} [ DTO EXT_ID: {} ] Error : {}", dto.getClass().getName(), entityClass.getName(), dto.toString(), e);
            return Optional.empty();
        }
    }

    public Optional<GenericDto> mapFromEntityToDto(E entity) {
        try {
            GenericDto dto = Mapper.getInstance().map(entity);
            return Optional.ofNullable(dto);
        } catch (Exception e) {
            log.error("[ Failed to map from entity {} to dto {} [ ENTITY_ID: {} ] Error : {}", entity.getClass().getName(), dtoClass.getName(), entity.toString(), e);
            return Optional.empty();
        }
    }

    public Optional<GenericEntity> updateEntityFromDto(GenericEntity entity, GenericDto dto) {
        try {
            Mapper.getInstance().map(dto);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("[ Failed to update entity {} from dto {} [ ENTITY_ID: {} ] Error : {}", entity.getClass().getName(), dto.getClass().getName(), entity.toString(), e);
            return Optional.empty();
        }
    }

}
