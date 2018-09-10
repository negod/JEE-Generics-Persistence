/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence;

import com.negod.generics.mock.service.DomainEntity;
import javax.persistence.EntityManager;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class DomainEntityDao extends GenericDao<DomainEntity> {

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    @Override
    public EntityManager getEntityManager(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
