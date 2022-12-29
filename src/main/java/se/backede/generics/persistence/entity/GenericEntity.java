/*

 * To change this license header, choose License Headers in Project Properties.

 * To change this template file, choose Tools | Templates

 * and open the template in the editor.

 */
package se.backede.generics.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
//@MappedSuperclass
@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)
@ToString
@Getter
@Setter
@EqualsAndHashCode
@Cacheable(value = true)
@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class GenericEntity implements Serializable {

    @Id
    @NotNull(message = "External id cannot be null and should be set to UUID")
    @Column(unique = true, updatable = false, insertable = true, name = "id")
    @Pattern(regexp = "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}")
    @XmlElement
    @KeywordField
    private String id;

    @NotNull(message = "Updated date cannot be null and all CRUD operations must have a date")
    @Column(name = "updatedDate")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @XmlElement
    private Date updatedDate;

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = new Date();
    }

    @PrePersist
    protected void onCreate() {
        this.updatedDate = new Date();
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

}
