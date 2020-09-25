/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fant.service;

import com.mycompany.fant.auth.AuthenticationService;
import com.mycompany.fant.auth.Group;
import com.mycompany.fant.auth.User;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import net.coobird.thumbnailator.Thumbnails;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;


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
    
 
    @Inject
    @ConfigProperty(name = "photo.storage.path", defaultValue = "fantphotos")
    String photoPath;

    
    
    
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
    
    private String getPhotoPath(){
        return photoPath;
    }
    
    @POST
    @Path("image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({Group.USER})
    public Response addImage(@FormDataParam("itemid") Long itemid,
            FormDataMultiPart multiPart) {
        Item item = em.find(Item.class, itemid);
         try {
            if(item !=null) {
                User user = this.getCurrentUser();
                List<FormDataBodyPart> images = multiPart.getFields("images");
                if(images != null) {
                    for(FormDataBodyPart part : images) {
                        InputStream is = part.getEntityAs(InputStream.class);
                        ContentDisposition meta = part.getContentDisposition();
                        
                        String pid = UUID.randomUUID().toString();
                        Files.copy(is, Paths.get(getPhotoPath(),pid));
                        
                        MediaObject photo = new MediaObject(pid, user, meta.getFileName(), meta.getSize(), meta.getType());
                        em.persist(photo);
                        item.addPhoto(photo);                                      
                    }
                }
            }
         } catch (IOException ex) {
                            Logger.getLogger(ItemService.class.getName()).log(Level.SEVERE, null, ex);
                            return Response.serverError().build();
         }
         return Response.ok().build();
    }
    
    @GET
    @Path("image/{name}")
    @Produces("image/jpeg")
    public Response getImage(@PathParam("name") String name,
                             @QueryParam("width") int width){
        if(em.find(MediaObject.class, name) != null) {
            StreamingOutput result = (OutputStream os) ->{
                java.nio.file.Path image = Paths.get(getPhotoPath(),name);
                if(width == 0){
                    Files.copy(image, os);
                    os.flush();
                }else{
                    Thumbnails.of(image.toFile())
                            .size(width, width)
                            .outputFormat("jpeg")
                            .toOutputStream(os);
                }
            };
            //ask the browser to cache the image for 24 hours
            CacheControl cc = new CacheControl();
            cc.setMaxAge(86400);
            cc.setPrivate(true);
            
            return Response.ok(result).cacheControl(cc).build();
        }else{
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
    

