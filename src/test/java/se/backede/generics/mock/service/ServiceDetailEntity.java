/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock.service;

import se.backede.generics.mock.service.constants.EntityConstants;
import se.backede.generics.persistence.entity.GenericEntity;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Table(name = EntityConstants.SERVICE_DETAIL)
@Entity
@Getter
@Setter
@Indexed
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = EntityConstants.SERVICE_DETAIL)
public class ServiceDetailEntity extends GenericEntity {

    @FullTextField
    @Column(name = "name", insertable = true, unique = true)
    private String name;

    @OneToOne(cascade = CascadeType.PERSIST, orphanRemoval = false)
    @PrimaryKeyJoinColumn
    private ServiceEntity service;

    @PrePersist
    @Override
    protected void onCreate() {
        if (service != null) {
            super.setId(service.getId());
        }
    }

}
