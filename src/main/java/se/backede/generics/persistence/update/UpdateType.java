/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics.persistence.update;

import java.io.Serializable;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public enum UpdateType implements Serializable{
    UPDATE, DELETE, ADD;
}
