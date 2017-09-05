package com.example.android.mh_player;

//An object that holds information on a single podcast
class Podcast {

    String      name;
    String      description;
    String      path_to_logo;
    String      rssFeedURL;

    Podcast (String name, String description, String path_to_logo, String rssFeedURL) {
        //Constructor
        this.name         = name;
        this.description  = description;
        this.path_to_logo = path_to_logo;
        this.rssFeedURL   = rssFeedURL;
    }
}
