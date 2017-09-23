package com.example.android.mh_player;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.UUID;

public class Episode implements Serializable{

    //Episode object holds all the information received from the RSS feed about a podcast episode.
    //Extends Serialziable so it can be sent to PlayerScreen.

    String          title;
    public String   description;
    private boolean isDownloaded;
    String          file_name;
    String          mp3URL;
    int             durationMS;
    String          durationText;
    String          episode_id = "default_dummy_id";
    String          pubDate;
    String          episodeAge;

    private String  link;


    Episode(String title,
            String description,
            String mp3URL,
            String duration,
            String pubDate,
            String link){

        this.title       = title;
        this.description = description;
        this.mp3URL      = mp3URL;
        this.pubDate     = pubDate;
        this.link        = link;
        this.durationText = duration;

        //Fri, 22 Sep 2017 15:53:47 +0000
        SimpleDateFormat mySimpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        long pubDateInMS = 0;

        try {
            Date parsedDate = mySimpleDateFormat.parse(this.pubDate);
            pubDateInMS = parsedDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (pubDateInMS != 0){
            Date currentDate = new Date();
            long currentDateInMS = currentDate.getTime();

            long different = currentDateInMS - pubDateInMS;
            long daysInMilli = 1000 * 60 * 60 * 24;
            long yearsInMilli = daysInMilli*365;

            long elapsedYears = different / yearsInMilli;

            if (elapsedYears == 0){
                //show only days
                long elapsedDays = different / daysInMilli;

                if (elapsedDays == 0){
                    this.episodeAge = "Today";
                } else {
                    this.episodeAge = String.valueOf(elapsedDays) + "d";
                }
            } else {
                different = different % yearsInMilli;
                long elapsedDays = different / daysInMilli;

                if (elapsedDays == 0){
                    this.episodeAge = String.valueOf(elapsedYears) + "y";
                } else {
                    this.episodeAge = String.valueOf(elapsedYears) + "y " + String.valueOf(elapsedDays) + "d";
                }
            }
        } else {
            this.episodeAge = "Unknown";
        }

        //Convert the String duration of the form "01:02:03" to int number of millisecs.
        String[] temp_arr = duration.split(":");

        if (temp_arr.length == 2){ //e.g. "53:23"

            int minutes = Integer.parseInt(temp_arr[0]);
            int seconds = Integer.parseInt(temp_arr[1]);
            this.durationMS = minutes*60*1000 + seconds*1000;

        } else if (temp_arr.length == 3){ //e.g. "01:02:03"

            int hours = Integer.parseInt(temp_arr[0]);
            int minutes = Integer.parseInt(temp_arr[1]);
            int seconds = Integer.parseInt(temp_arr[2]);
           this.durationMS = hours*60*60*1000 + minutes*60*1000 + seconds*1000;

        }

        this.isDownloaded  = false;

        //Local file name, random string of 32 chars.
        this.file_name = UUID.randomUUID().toString().replaceAll("-","");

        //Create a unique & consistent episode id
        try {
            URL tempURL = new URL(mp3URL);
            episode_id = String.valueOf(tempURL.hashCode());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    void setDownloadedTrue(){
        this.isDownloaded = true;
    }

    boolean isDownloaded(){
        return this.isDownloaded;
    }
}
