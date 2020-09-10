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
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
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
    
    @Inject
    MailService mailService;
    
    /** path to store photos 
    @Inject
    @ConfigProperty(name = "photo.storage.path", defaultValue = "fantphotos")
    String photoPath;
    * 
    * 
    * 
    */
    
    
    
    @DELETE
    @Path("remove")
    @RolesAllowed({Group.USER})
    public Response delete(@QueryParam("itemid") Long itemid) {
        Item item = em.find(Item.class, itemid);
        if(item != null){
            User user = this.getCurrentUser();
            if(item.getItemOwner().getUserid().equals(user.getUserid()))
                em.remove(item);
            return Response.ok().build();
        }
        return Response.notModified().build();
    }
    
    
    /**
     * lists all items
     * @return 
     */
    @GET
    @Path("allitems")
    public List<Item> getAllItems() {
        return em.createNamedQuery(Item.FIND_ALL_ITEMS, Item.class).getResultList();
    }
    

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
    @PUT
    @Path("email")
    @RolesAllowed({Group.USER})
    public Response setEmail(
            @QueryParam("uid") String uid,
            @FormParam("email") String email){
        User user = this.getCurrentUser();
        if(user.getEmail()==null){
            user.setEmail(email);
        }
        return Response.ok().build();
    }
    
    @PUT
    @Path("purchase")
    @RolesAllowed({Group.USER})
    public Response purchaseItem(@QueryParam("itemid") Long itemid){
       
        Item item = em.find(Item.class, itemid);
        if(item !=null){
             if(item.getItemBuyer()== null){
                 User user = this.getCurrentUser();
                 item.setItemBuyer(user);
                 mailService.sendEmail(item.getItemOwner().getEmail(), "Following item has been sold: ", item.getItem());
                 return Response.ok().build();
             }
        }return Response.notModified().build();
    }
    
    private User getCurrentUser(){
        return em.find(User.class, sc.getUserPrincipal().getName());
    }
}
    

