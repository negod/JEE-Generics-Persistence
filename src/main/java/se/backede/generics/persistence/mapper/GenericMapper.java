/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package se.backede.generics.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import se.backede.generics.persistence.dto.GenericDto;
import se.backede.generics.persistence.entity.GenericEntity;

/**
 *
 * @author joaki
 */
@Mapper
public interface GenericMapper {

    GenericMapper INSTANCE = Mappers.getMapper(GenericMapper.class);

    GenericDto entityToDto(GenericEntity entity);
    GenericEntity dtoToEntity(GenericDto dto);

}
