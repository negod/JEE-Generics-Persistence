/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.mock;

import java.util.HashSet;
import java.util.Set;
import se.backede.generics.mock.service.UserEntity;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class UserEntityMock {

    public static UserEntity getUserEntity(String name) {

        UserEntity entity = new UserEntity();
        entity.setName(name);
        return entity;

    }

    public static Set<UserEntity> getUserEntitySet(Integer amount) {
        Set<UserEntity> entitites = new HashSet<>();
        for (int i = 0; i < amount; i++) {
            System.out.println("Entity".concat(String.valueOf(i)));
            entitites.add(getUserEntity("Entity".concat(String.valueOf(i))));
        }
        return entitites;
    }

}
