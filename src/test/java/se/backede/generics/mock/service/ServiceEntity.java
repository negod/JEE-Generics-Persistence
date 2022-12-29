package se.backede.generics.mock.service;

import se.backede.generics.mock.service.constants.EntityConstants;
import se.backede.generics.persistence.entity.GenericEntity;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

/**
 *
 * @author Joakim Backede joakim.backede@outlook.com
 */
@Table(name = EntityConstants.SERVICE)
@Entity
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, exclude = "users")
@Indexed
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = EntityConstants.SERVICE)
public class ServiceEntity extends GenericEntity {

    @FullTextField
    @Column(name = "name", insertable = true, unique = true)
    private String name;

    @OneToOne(mappedBy = "service", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private ServiceDetailEntity detail;

    @IndexedEmbedded(includeDepth = 3)
    @JoinColumn(name = "domain_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private DomainEntity domain;

    @IndexedEmbedded(includeDepth = 3)
    @JoinTable(name = "service_user",
            joinColumns = {
                @JoinColumn(name = "service_id", referencedColumnName = "id")},
            inverseJoinColumns = {
                @JoinColumn(name = "user_id", referencedColumnName = "id")})
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "service.users")
    private Set<UserEntity> users = new HashSet<>();

    //For OneToOne relation
    @PrePersist
    @Override
    protected void onCreate() {
        super.setUpdatedDate(new Date());
        super.setId(UUID.randomUUID().toString());
        if (detail != null) {
            detail.setId(super.getId());
            detail.setUpdatedDate(super.getUpdatedDate());
            detail.setService(this);
        }
    }

}
