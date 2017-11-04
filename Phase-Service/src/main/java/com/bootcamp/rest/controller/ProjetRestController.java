
package com.bootcamp.rest.controller;

import static aQute.lib.osgi.Processor.split;
import com.bootcamp.Designs.Critere;
import com.bootcamp.jpa.entities.Projet;
import com.bootcamp.jpa.repositories.ProjetRepository;
import com.bootcamp.rest.exception.NotCreateException;
import com.bootcamp.rest.exception.SuccessMessage;
import com.bootcamp.rest.exception.ReturnResponse;
import com.bootcamp.rest.exception.UnknownException;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.scene.input.KeyCode.T;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.apache.felix.bundlerepository.impl.RepositoryParser.T;
import scala.tools.nsc.interactive.Global;

@Path("/projet")
public class ProjetRestController {
    
    ProjetRepository pr = new ProjetRepository("tpRest-mysql");
        
    List<Projet> liste = new ArrayList<Projet>();
        
    Response resp;
    
    Field[] fieldsProjet = returnProperties(Projet.class);

    // Services qui renvoie la liste des projets suivants les criteres de trie,
    //de filtre ou de pagination envoyé par l'user suivant un format donnee
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all")
    public Response getAllProjets(Critere c) { 
       // recuperation des elemnts de l'objet
       String[] filtreElts = c.getFiltre();
       
       String attr = c.getSort().getAttribut();
       String ordre = c.getSort().getOrdre();
       
       int offset = c.getPagination().getOffset();
       int limit = c.getPagination().getLimit();
       
        if(c == null) {
            try {
                liste = pr.findAll();  
                return Response.status(200).entity(liste).build();
            } catch (Exception e) {
                resp=UnknownException.unknownException(e);
            }  
        }else {
               
            if(filtreElts != null)       
            {
                try {
                liste = pr.findByCriterias(attr, ordre, offset, limit);
                Map<String, Object> responseMaps = filtre(filtreElts, liste.get(0));
                    for (int i = 0; i < liste.size(); i++) {
                    responseMaps.putAll(filtre(filtreElts, liste.get(i))) ;
                    }
                return Response.status(200).entity(responseMaps).build();
            } catch (Exception e) {
                resp=UnknownException.unknownException(e);
            } 
            }else{
                try {
                liste = pr.findByCriterias(attr, ordre, offset, limit);
                return Response.status(200).entity(liste).build();
            } catch (Exception e) {
                resp=UnknownException.unknownException(e);
            } 
            }
             
        }
              
        return resp;
    }
      
    // service pour creer un projet quand on en a le droit
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Projet projet) throws SQLException {      
                //
                 try {
                
                pr.create(projet);
                resp=SuccessMessage.message("Bien cree");
            } catch (Exception e) {
                resp=NotCreateException.notCreateException("Erreur lors de la creation", e);
            }
            
        return resp;
    }

    // Service pour modifier un projet suivant les droit qu'on a
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(Projet projet) throws SQLException {
       
            try {
                pr.update(projet);
                resp=SuccessMessage.message("Mise a jour bien faite");
            } catch (Exception e) {
                resp=NotCreateException.notCreateException("Erreur lors de la mise a jour", e);
            }
                   
        return resp;
    }
    
    // Service pour supprimer un projet quand on en a le droit
    @DELETE
    @Path("/delete/{id}")
    public Response delete(@PathParam("id") int valeur) throws SQLException {                  
                try {
                Projet projet = pr.findById(valeur);
                pr.delete(projet);
                resp=SuccessMessage.message("Le projet d'id "+valeur+" a bien ete supprime");
            } catch (Exception e) {
                resp=NotCreateException.notCreateException("Probleme lors de la suppression de projet d'id "+valeur,e);
            }
        
        return resp;    
    }
    
    //Servivce pour récuperer un projet par son id et le trier suivant un ou des attributs du choix de l'user
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response findProjetByFields(@PathParam("id") int id,@QueryParam("fields") String fields) throws SQLException, IntrospectionException, InvocationTargetException, IllegalArgumentException, IllegalAccessException {

            try {   
        String[] rempli = fields.split(",");
        Projet proj = pr.findById(id);
        
         Map<String, Object> responseMap = filtre(rempli,proj);
         return Response.status(200).entity(responseMap).build();
        //resp = ReturnResponse.object("", responseMap);
                  
            } catch (Exception e) {
         resp=UnknownException.unknownException(e);
            }  
        
       return resp;        
    }
    
    //Service pour la recherche
        @GET
        @Path("/search")
        @Produces("application/json")
        public Response searchInProjet(@QueryParam("attrib") String attr, @QueryParam("value") String value) throws IntrospectionException, SQLException {
            if(singleCheckAttribute(attr)){               
                liste = pr.search(attr, value);
            return Response.status(200).entity(liste).build();
             }
            return Response.status(200).entity("Probleme !!").build();
        }    
    
    // juste pour le debugage
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list")
    public Response getListProjets() {       
            try {
                liste = pr.findAll();     
                return Response.status(200).entity(liste).build();
               // resp =ReturnResponse.object("Liste de tous les projets", liste);
            } catch (Exception e) {
                resp=UnknownException.unknownException(e);
            }
            
        return resp;
    }
     /*  Q U E L Q U E S   M E T H O D E S    U T I L E S   */
    
    
        // verifie si chaque elt d'une liste est egal a un elt donnee
     private boolean check(String[] fields, String field) {
       for (String field1 : fields) {
           if (field.equals(field1)) {
               return true;
           }
       }
       return false;
   }
     
      // verifie si un elt est egal a un autre elt donnee
     private boolean singlecheck(String fields, String field) {
       
           if (field.equals(fields)) {
               return true;
       }
       return false;
   }
     
     //verifie si un attribut appartient a l'ensemble des attributs d'une classe
     private boolean checkAttribute(String[] attrs) throws IntrospectionException{
    PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Projet.class).getPropertyDescriptors();

             for (PropertyDescriptor propertyDescriptor: propertyDescriptors) {

               Method method = propertyDescriptor.getReadMethod();

               if (check(attrs, propertyDescriptor.getName())) {
                   return true;
               }
        }
             return false;
     }
     
     //verifie si un attribut appartient a l'ensemble des attributs d'une classe
     private boolean singleCheckAttribute(String attr) throws IntrospectionException{
    PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Projet.class).getPropertyDescriptors();

             for (PropertyDescriptor propertyDescriptor: propertyDescriptors) {

               Method method = propertyDescriptor.getReadMethod();

               if (singlecheck(attr, propertyDescriptor.getName())) {
                   return true;
               }
        }
             return false;
     }
      
     // retourne l'ensemble des champs d'une classe
    private Field[] returnProperties(Class c) {      
           return c.getDeclaredFields();
       }

    // filtre un filtre un résultat par selon plusieurs champs spécifies
    private Map<String, Object> filtre(String[] attrs, Projet projet) throws IllegalAccessException, IllegalArgumentException, IntrospectionException{

        Map<String, Object> responseMap = new HashMap<String, Object>();
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Projet.class).getPropertyDescriptors();

             for (PropertyDescriptor propertyDescriptor: propertyDescriptors) {

               Method method = propertyDescriptor.getReadMethod();

               if (checkAttribute(attrs)) {
                   try {
                       responseMap.put(propertyDescriptor.getName(), method.invoke(projet));
                   } catch (InvocationTargetException e) {
                       Logger.getLogger(ProjetRestController.class.getName()).log(Level.SEVERE, null, e);
                   }
               }
        }
             return responseMap;
       }
}
