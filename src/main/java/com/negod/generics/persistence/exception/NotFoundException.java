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
public class NotFoundException extends DaoException {

    public NotFoundException(String msg) {
        super(msg);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
