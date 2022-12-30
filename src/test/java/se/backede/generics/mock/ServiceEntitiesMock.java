/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock;

import se.backede.generics.mock.service.DomainEntity;
import se.backede.generics.mock.service.ServiceEntity;
import se.backede.generics.mock.service.UserEntity;
import se.backede.generics.persistence.search.GenericFilter;
import se.backede.generics.persistence.search.Pagination;
import se.backede.generics.persistence.update.ObjectUpdate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import se.backede.generics.persistence.search.SearchMatch;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class ServiceEntitiesMock {

    public static ServiceEntity getServiceEntity() {
        ServiceEntity entity = new ServiceEntity();
        entity.setName(UUID.randomUUID().toString());
        return entity;
    }

    public static DomainEntity getDomainEntity() {
        DomainEntity entity = new DomainEntity();
        entity.setName(UUID.randomUUID().toString());
        return entity;
    }

    public static UserEntity getUserEntity() {
        UserEntity entity = new UserEntity();
        entity.setName(UUID.randomUUID().toString());
        return entity;
    }

    public static ObjectUpdate getObjectUpdate() {
        ObjectUpdate objUpd = new ObjectUpdate();
        return objUpd;
    }

    public static Set<ObjectUpdate> getObjectUpdateSet() {
        Set<ObjectUpdate> objUpd = new HashSet<>();
        return objUpd;
    }

    public static GenericFilter getGenericFilter(Set<String> searchFields, String globalSearchWord, Integer listsize, Integer page) {
        GenericFilter filter = new GenericFilter();
        filter.setGlobalSearchWord(globalSearchWord);
        filter.setSearchFields(searchFields);
        Pagination pagination = new Pagination();
        pagination.setListSize(listsize);
        pagination.setPage(page);
        filter.setSearchMatch(SearchMatch.WILDCARD);
        filter.setPagination(pagination);
        return filter;
    }

}
