package com.bootcamp;

import com.bootcamp.jpa.entities.Projet;
import com.bootcamp.jpa.repositories.ProjetRepository;
import java.sql.SQLException;
import java.util.List;


public class App 
{    
    public static void main(String[] args) throws SQLException {
        ProjetRepository pr = new ProjetRepository("tpRest-mysql");
        //Projet projet = pr.findById(1);
        //System.out.println("le find By  "+projet.toString());
        
        List<Projet> liste = pr.search("id", "1");
        
        
    }
}
