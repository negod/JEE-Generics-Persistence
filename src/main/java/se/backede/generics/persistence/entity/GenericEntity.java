/*

 * To change this license header, choose License Headers in Project Properties.

 * To change this template file, choose Tools | Templates

 * and open the template in the editor.

 */
package se.backede.generics.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import static jakarta.persistence.TemporalType.TIMESTAMP;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
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
@MappedSuperclass
public class GenericEntity implements Serializable {

    @Id
    @NotNull(message = "External id cannot be null and should be set to UUID")
    @Column(unique = true, updatable = false, insertable = true, name = "id")
    @Pattern(regexp = "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}")
    @XmlElement
    private String id;

    @NotNull(message = "Updated date cannot be null and all CRUD operations must have a date")
    @Column(name = "updatedDate")
    @Temporal(TIMESTAMP)
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
