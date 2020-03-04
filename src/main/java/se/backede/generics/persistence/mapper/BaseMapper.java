/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.mapper;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 * @param <D>
 * @param <E>
 */
public interface BaseMapper<D, E> {

    default Optional<Set<E>> mapToEntitySet(Set<D> dtoList) {
        Set<E> entityList = new HashSet<>();
        for (D dto : dtoList) {
            Optional<E> entity = mapFromDtoToEntity(dto);
            if (entity.isPresent()) {
                entityList.add(entity.get());
            }
        }
        return Optional.ofNullable(entityList);
    }

    default Optional<Set<D>> mapToDtoSet(Set<E> entityList) {
        Set<D> dtoList = new HashSet<>();
        for (E entity : entityList) {
            Optional<D> dto = mapFromEntityToDto(entity);
            if (dto.isPresent()) {
                dtoList.add(dto.get());
            }

        }
        return Optional.ofNullable(dtoList);
    }

    public Optional<E> mapFromDtoToEntity(D dto);

    public Optional<D> mapFromEntityToDto(E entity);

    public Optional<E> updateEntityFromDto(E entity, D dto);

}
