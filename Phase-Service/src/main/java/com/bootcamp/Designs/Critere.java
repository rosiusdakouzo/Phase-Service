/**
 *
 * @Bignon
 */
package com.bootcamp.Designs;

import java.util.List;

public class Critere {
    //private String[] filtre;
    private Pagination pagination;
    private Sort sort;
/*
    public String[] getFiltre() {
        return filtre;
    }

    public void setFiltre(String[] filtre) {
        this.filtre = filtre;
    }
*/
    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }
    
    
}
