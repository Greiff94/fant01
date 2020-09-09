/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fant.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author sigur
 */
@Path("item")
public class itemservice {
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String allItems(){
        return "this will soon show all items";
    }
    
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "test";
    }
    
    @GET
    @Path("userid")
    @Produces(MediaType.TEXT_HTML)
    public String userItem(@PathParam("userid") String userid){
        //user.getUser(userid)
        return "this will show all items from one UserID, current param: "+userid;
    }
    
    @GET
    @Path("specific")
    @Produces(MediaType.TEXT_HTML)
    public String specificItem(){
    return "This path will show a specific item.";
    }
    /**
    @POST
    @Path("add")
    @Consumes(MediaType.
    */
}
    

