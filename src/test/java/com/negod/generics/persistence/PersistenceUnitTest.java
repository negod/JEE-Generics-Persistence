/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class PersistenceUnitTest {

    @PersistenceContext
    private static EntityManager em;

    protected static EntityManagerFactory emf;

    public static EntityManager getEntityManager() {
        if (em == null) {
            emf = Persistence.createEntityManagerFactory("TestPu");
            em = emf.createEntityManager();
        }
        if (!em.isOpen()) {
            em = emf.createEntityManager();
        }
        return em;
    }

}
