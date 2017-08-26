package com.example.android.mh_player;

import android.graphics.drawable.Drawable;

//An object that holds information on a single podcast
public class Podcast {

    private String name;
    private String description;
    private Drawable logo;
    private String rssFeedURL;

    public Podcast (String name, String description, Drawable logo, String rssFeedURL) {
        //Constructor
        this.name        = name;
        this.description = description;
        this.logo        = logo;
        this.rssFeedURL  = rssFeedURL;
    }

    public String getPodcastName(){
        return this.name;
    }

    public String getPodcastDescription(){
        return this.description;
    }

    public Drawable getLogo(){
        return this.logo;
    }

    public String getRssFeedURL(){
        return this.rssFeedURL;
    }
}
