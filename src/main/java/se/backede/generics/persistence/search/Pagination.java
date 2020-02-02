package se.backede.generics.persistence.search;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import lombok.Data;

/**
 *
 * @author Joakim Johansson ( joakimjohansson@outlook.com )
 */
@Data
public class Pagination {

    private Integer listSize;
    private Integer page;
    private OrderBy order;

}
