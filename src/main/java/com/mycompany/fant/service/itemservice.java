/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fant.service;

import com.mycompany.fant.auth.AuthenticationService;
import com.mycompany.fant.auth.Group;
import com.mycompany.fant.auth.User;
import java.util.List;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author sigur
 */
@Path("item")
@Stateless
@DeclareRoles({Group.USER})
public class ItemService {
    
     @Inject
    AuthenticationService authService;
    
    @Context
    SecurityContext sc;
        
    @PersistenceContext
    EntityManager em;
    
    /** path to store photos 
    @Inject
    @ConfigProperty(name = "photo.storage.path", defaultValue = "fantphotos")
    String photoPath;
    */
    
    /**
     * lists all items
     * @return 
     */
    @GET
    @Path("allitems")
    public List<Item> getAllItems() {
        return em.createNamedQuery(Item.FIND_ALL_ITEMS, Item.class).getResultList();
    }
    
/**
 * returns all items from a specific user
 * @param userid
 * @return 
 */
    
    @GET
    @Path("userid")
    @Produces(MediaType.TEXT_HTML)
    public String userItem(@PathParam("userid") String userid){
        //item.getItem
        return "this will show all items from one UserID, current param: "+userid;
    }
    
    /**
     * returns items from itemID
     * @return 
     */
    @GET
    @Path("specific")
    @Produces(MediaType.TEXT_HTML)
    public String specificItem(){
    return "This path will show a specific item.";
    }
    

 /**
  * Lets a logged in user add items
  * @param item
  * @param description
  * @param price
  * @return 
  */
    @POST
    @Path("add")
    @RolesAllowed({Group.USER})
    public Response addItem(
            @FormParam("item") String item,
            @FormParam("description") String description,
            @FormParam("price") int price){
        
        User user = this.getCurrentUser();
        Item newItem = new Item();
        
        newItem.setItemOwner(user);
        newItem.setItem(item);
        newItem.setDescription(description);
        newItem.setPrice(price);
        
        em.persist(newItem);
        
        return Response.ok().build();
   
}
    
        private User getCurrentUser(){
        return em.find(User.class, sc.getUserPrincipal().getName());
    }
}
    

