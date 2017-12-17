/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence;

import com.negod.generics.mock.service.UserEntity;
import javax.persistence.EntityManager;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class UserEntityDao extends GenericDao<UserEntity> {

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

}
