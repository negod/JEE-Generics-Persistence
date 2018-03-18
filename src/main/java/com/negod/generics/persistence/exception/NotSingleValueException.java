/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics.persistence.exception;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
public class NotSingleValueException extends Exception {

    public NotSingleValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSingleValueException(String message) {
        super(message);
    }

}
