package com.maxaltena.socialkit;

public class Social {
    private String username;

    public Social(){
        // Public no-ard constructor needed because java is horrible
    }

    public Social(String username){
        this.username = username;
    };

    public String getUsername(){
        return username;
    }
}

