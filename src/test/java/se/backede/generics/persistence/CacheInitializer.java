/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence;

import jakarta.persistence.EntityManager;
import se.backede.generics.persistence.entity.EntityRegistry;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class CacheInitializer extends EntityRegistry {

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    public CacheInitializer() {
        super.registerEnties();
    }

}
