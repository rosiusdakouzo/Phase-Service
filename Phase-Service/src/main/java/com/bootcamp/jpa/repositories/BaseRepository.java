package com.bootcamp.jpa.repositories;

import java.sql.SQLException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * @param <T>
 */
public class BaseRepository<T>{

  private final EntityManager em;
  private final String unitPersistence;
  private final EntityManagerFactory emf;
  private final Class entityClass;

   
    public BaseRepository(String unitPersistence, Class c) {
        this.unitPersistence = unitPersistence;
        emf = Persistence.createEntityManagerFactory(this.unitPersistence);
         this.em=emf.createEntityManager();
        this.entityClass = c;
    }
    
    public  EntityManager getEntityManager() {
            return this.em;
        }

   public Boolean create(T entity) throws SQLException {
       getEntityManager().getTransaction().begin();
       getEntityManager().persist(entity);
       getEntityManager().getTransaction().commit();
       return true;
    }

    public void delete(T type){
        getEntityManager().getTransaction().begin();
              getEntityManager().remove(type);
         getEntityManager().getTransaction().commit();
              
    }
    
    public List<T> findByProperty(String propertyName, Object value) throws SQLException {
           
            String className = entityClass.getSimpleName(); 
            String str = "select obj from "+ className+ " obj where obj."+propertyName +"=:param";
            Query query = getEntityManager().createQuery(str);
            query.setParameter("param", value);

            List<T> result = query.getResultList();
          
            return result;
        }
    
    public T findById(int value) throws SQLException {
           
            String className = entityClass.getSimpleName(); 
            String str = "select obj from "+ className+ " obj where obj.id=:param";
            Query query = getEntityManager().createQuery(str);
            query.setParameter("param", value);

             List<T> result = query.getResultList();
          
            return result.get(0);
        }
    
    public List<T> findAll(){    
            String className = entityClass.getSimpleName(); 
            String str = "select obj FROM "+className+" obj";
            Query query = getEntityManager().createQuery(str);
            List<T> result = query.getResultList();
            return result;
    }
    
     public List<T> findByCriterias(String attr, String sens,int offset, int limit){
         String className = entityClass.getSimpleName();
         String str ="";
         List<T> result = null;
         
        if( (attr != null && sens != null) && ( offset == 0 && limit == 0) ){
        str = "select obj FROM "+className+" obj ORDER BY obj."+attr+" "+sens.toUpperCase();
        Query query = getEntityManager().createQuery(str);
        result = query.getResultList();
         }
         
        if( (attr == null && sens == null) && ( offset != 0 && limit != 0) ){
        str = "select obj FROM "+className+" obj ";
        Query query = getEntityManager().createQuery(str);
        query.setFirstResult(offset).setMaxResults(limit);
        result = query.getResultList();
         }
         
        if( (attr != null && sens != null) && ( offset != 0 && limit != 0) ){
        str = "select obj FROM "+className+" obj ORDER BY obj."+attr+" "+sens.toUpperCase();
        Query query = getEntityManager().createQuery(str);
        query.setFirstResult(offset).setMaxResults(limit);
        result = query.getResultList();
         }
                       
            return result;
    }
     
     public List<T> search(Object o ,Object value){
        
            String className = entityClass.getSimpleName(); 
            String s = "SELECT obj FROM "+className+" obj WHERE obj."+o+" LIKE '%"+value+"%'";
            Query query = getEntityManager().createQuery(s);
             List<T> result = query.getResultList();
            return result;
    }

    public void update(T t){
        getEntityManager().getTransaction().begin();
       getEntityManager().merge(t);
       getEntityManager().getTransaction().commit();
    }
    
    

    }
