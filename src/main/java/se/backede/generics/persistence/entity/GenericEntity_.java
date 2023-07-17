/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package se.backede.generics.persistence.entity;

import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Date;

/**
 *
 * @author joaki
 */
public class GenericEntity_ {

    public static volatile SingularAttribute<GenericEntity, String> id;
    public static volatile SingularAttribute<GenericEntity, Date> updatedDate;

}
