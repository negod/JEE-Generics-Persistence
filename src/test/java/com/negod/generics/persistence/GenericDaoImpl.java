/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence;

import com.negod.generics.mock.TestEntity;
import com.negod.generics.persistence.exception.DaoException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class GenericDaoImpl extends GenericDao<TestEntity> {

    @PersistenceContext(name = "testPU")
    EntityManager em;

    public GenericDaoImpl(Class entityClass) throws DaoException {
        super(TestEntity.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}
