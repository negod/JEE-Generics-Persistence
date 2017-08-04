/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence.mapper;

import com.negod.generics.persistence.entity.GenericEntity;
import lombok.extern.slf4j.Slf4j;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import static org.dozer.loader.api.TypeMappingOptions.mapEmptyString;
import static org.dozer.loader.api.TypeMappingOptions.mapNull;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class Mapper {

    private static final DozerBeanMapper mapper = new DozerBeanMapper();
    private static final Mapper INSTANCE = new Mapper();

    public static Mapper getInstance() {
        return INSTANCE;
    }

    public DozerBeanMapper getMapper() {
        return mapper;
    }

}
