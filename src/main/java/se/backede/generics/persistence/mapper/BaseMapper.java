/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.mapper;

import java.util.HashSet;
import java.util.Set;
import org.mapstruct.Mapping;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 * @param <D>
 * @param <E>
 */
public interface BaseMapper<D, E> {

    default Set<E> mapToEntitySet(Set<D> dtoList) {
        Set<E> entityList = new HashSet<>();
        for (D dto : dtoList) {
            entityList.add(mapFromDtoToEntity(dto));
        }
        return entityList;
    }

    default Set<D> mapToDtoSet(Set<E> entityList) {
        Set<D> dtoList = new HashSet<>();
        for (E entity : entityList) {
            dtoList.add(mapFromEntityToDto(entity));
        }
        return dtoList;
    }

    @Mapping(source = "id", target = "id")
    public E mapFromDtoToEntity(D dto);

    public D mapFromEntityToDto(E entity);

    public E updateEntityFromDto(E entity, D dto);

}
