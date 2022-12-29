/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.mapper;

import lombok.extern.slf4j.Slf4j;
import se.backede.generics.persistence.dto.GenericDto;
import se.backede.generics.persistence.entity.GenericEntity;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 * @param <D>
 * @param <E>
 */
@Slf4j
public class Mapper<D extends GenericDto, E extends GenericEntity> {

    private static final Mapper INSTANCE = new Mapper();

    public static Mapper getInstance() {
        return INSTANCE;
    }

    protected GenericDto map(GenericEntity entity) {
        return GenericMapper.INSTANCE.entityToDto(entity);
    }

    protected GenericEntity map(GenericDto dto) {
        return GenericMapper.INSTANCE.dtoToEntity(dto);
    }

}
