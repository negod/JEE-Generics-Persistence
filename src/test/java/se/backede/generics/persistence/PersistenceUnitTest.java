/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;

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
