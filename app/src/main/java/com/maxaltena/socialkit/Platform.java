package com.maxaltena.socialkit;

public class Platform {
    private String name;
    private String link;
    private String image;

    public Platform(){
        //Public no-ard constructor needed because java is horrible
    }

    public Platform(String name, String link, String image){
        this.name = name;
        this.link = link;
        this.image = image;
    };

    public String getName(){
        return name;
    }
    public String getLink(){
        return link;
    }
    public String getImage(){
        return image;
    }



}

