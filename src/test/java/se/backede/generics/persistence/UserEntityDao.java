/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence;

import jakarta.persistence.EntityManager;
import se.backede.generics.mock.service.UserEntity;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class UserEntityDao extends GenericDao<UserEntity> {

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    @Override
    public EntityManager getEntityManager(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
