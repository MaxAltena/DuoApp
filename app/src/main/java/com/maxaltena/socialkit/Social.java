package com.maxaltena.socialkit;

public class Social {
    private String username;
    //private String othervar;

    public Social(){
        //Public no-ard constructor needed because java is horrible
    }

    public Social(String username){
        this.username = username;
        //this.othervar = var
    };

    public String getUsername(){
        return username;
    }

    //public String getOthervar(){
    //    return othervar;
    //}


}

