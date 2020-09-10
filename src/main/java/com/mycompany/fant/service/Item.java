/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.fant.service;

import com.mycompany.fant.auth.User;
import static com.mycompany.fant.service.Item.FIND_ALL_ITEMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author sigur
 */
@Table(name = "Item")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedQuery(name = FIND_ALL_ITEMS,
        query = "select i from Item i")
public class Item implements Serializable {
    public static final String FIND_ALL_ITEMS = "Item.findAllItems";
    
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long itemid;
    
    private String item;
    private String description;
    private int price;
    
    /*
    Mikael photo
    */
    @JsonbTypeAdapter(MediaObjectAdapter.class)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<MediaObject> photos;
    
    @ManyToOne
    @JoinColumn(nullable = false)
    private User itemOwner;
    
    @ManyToOne
    private User itemBuyer;
    
       /*
    Mikael photo
    */
    public void addPhoto(MediaObject photo) {
        if(this.photos == null) {
            this.photos = new ArrayList<>();
        }
        
        this.photos.add(photo);
    }

    
            
}
