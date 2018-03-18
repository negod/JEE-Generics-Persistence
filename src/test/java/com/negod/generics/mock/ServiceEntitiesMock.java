/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.mock;

import com.negod.generics.mock.service.DomainEntity;
import com.negod.generics.mock.service.ServiceEntity;
import com.negod.generics.mock.service.UserEntity;
import com.negod.generics.persistence.search.GenericFilter;
import com.negod.generics.persistence.search.Pagination;
import com.negod.generics.persistence.search.SearchMatch;
import com.negod.generics.persistence.update.ObjectUpdate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
