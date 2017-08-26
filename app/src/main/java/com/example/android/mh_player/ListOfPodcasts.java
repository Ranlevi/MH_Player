package com.example.android.mh_player;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import java.util.Arrays;

public class ListOfPodcasts {
    //Creates the list of podcasts displayed in the main screen.
    private ArrayList<Podcast> arrayOfPodcasts;

    //Create the List of Podcasts
    public ListOfPodcasts (Context context){
        //Constructor

        Drawable logo = ContextCompat.getDrawable(context, R.drawable.making_history_logo_60px);
        Podcast makingHistory = new Podcast("עושים היסטוריה",
                                            "פודקאסט על מדע, טכנולוגיה והיסטוריה",
                                            logo,
                                            "http://www.ranlevi.com/feed/podcast/" );

        logo = ContextCompat.getDrawable(context, R.drawable.osim_asakim_logo_60px);
        Podcast osimAsakim = new Podcast("עושים עסקים",
                                        "מאחורי הקלעים עם המנהלים המובילים במשק",
                                        logo,
                                        "http://www.ranlevi.com/feed/bizpod/" );

        arrayOfPodcasts = new ArrayList<Podcast>(
                Arrays.asList(makingHistory, osimAsakim)
        );
    }

    public ArrayList<Podcast> getList(){
        //Getter
        return arrayOfPodcasts;
    }


}
