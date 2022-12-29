/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock.service;

import se.backede.generics.mock.service.constants.EntityConstants;
import se.backede.generics.persistence.entity.GenericEntity;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Table(name = EntityConstants.USER)
@Entity
@Getter
@Setter
@Indexed
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = EntityConstants.USER)
@EqualsAndHashCode
public class UserEntity extends GenericEntity {

    @FullTextField
    @Column(name = "name", insertable = true, unique = true)
    private String name;

    @IndexedEmbedded(includeDepth = 1)
    @ManyToMany(mappedBy = "users", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "user.services")
    private Set<ServiceEntity> services;

}
