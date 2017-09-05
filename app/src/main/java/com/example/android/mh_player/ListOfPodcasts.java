package com.example.android.mh_player;

import java.util.ArrayList;
import java.util.Arrays;

class ListOfPodcasts {
    //Creates the list of podcasts displayed in the main screen.
    private ArrayList<Podcast> arrayOfPodcasts;

    //Constructor, Create the List of Podcasts
    ListOfPodcasts(){

        Podcast makingHistory = new Podcast("עושים היסטוריה",
                                            "פודקאסט על מדע, טכנולוגיה והיסטוריה",
                                            "making_history_logo_60px",
                                            "http://www.ranlevi.com/feed/podcast/" );

        Podcast osimAsakim = new Podcast("עושים עסקים",
                                        "מאחורי הקלעים עם המנהלים המובילים במשק",
                                        "osim_asakim_logo_60px",
                                        "http://www.ranlevi.com/feed/bizpod/" );

        arrayOfPodcasts = new ArrayList<Podcast>(
                    Arrays.asList(makingHistory, osimAsakim)
        );
    }

    ArrayList<Podcast> getList(){
        return arrayOfPodcasts;
    }


}
