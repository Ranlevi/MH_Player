package com.example.android.mh_player;

import java.io.Serializable;
import java.util.UUID;

public class Episode implements Serializable{

    //Episode object holds all the information received from the RSS feed about a podcast episode.
    //Extends Serialziable so it can be sent to PlayerScreen.

    private String  title;
    private String  description;
    private String  mp3URL;
    private int     duration;
    private Boolean downloaded;
    private String  file_name;


    public Episode(String title,
                   String description,
                   String mp3URL,
                   String duration){

        this.title       = title;
        this.description = description;
        this.mp3URL      = mp3URL;

        //Convert the String duration of the form "01:02:03" to int number of millisecs.
        String[] temp_arr = duration.split(":");

        if (temp_arr.length == 2){ //e.g. "53:23"

            int minutes = Integer.parseInt(temp_arr[0]);
            int seconds = Integer.parseInt(temp_arr[1]);
            this.duration = minutes*60*1000 + seconds*1000;

        } else if (temp_arr.length == 3){ //e.g. "01:02:03"

            int hours = Integer.parseInt(temp_arr[0]);
            int minutes = Integer.parseInt(temp_arr[1]);
            int seconds = Integer.parseInt(temp_arr[2]);
           this.duration = hours*60*60*1000 + minutes*60*1000 + seconds*1000;

        }

        this.downloaded  = false;

        //Local file name, random string of 32 chars.
        this.file_name = UUID.randomUUID().toString().replaceAll("-","");
    }

    public String getEpisodeTitle(){
        return this.title;
    }

    public String getEpisodeDescription(){
        return this.description;
    }

    public String getMp3URL(){
        return this.mp3URL;
    }

    public int getDuration(){
        return this.duration;
    }

    public void setDownloaded(){
        this.downloaded = true;
    }

    public boolean isDownloaded(){
        return this.downloaded;
    }

    public String getFilename(){
        return this.file_name;
    }
}
