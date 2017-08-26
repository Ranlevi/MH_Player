package com.example.android.mh_player;

import java.util.ArrayList;

public class ListOfEpisodes {

    private ArrayList<Episode> arrayOfEpisodes = new ArrayList<Episode>();

    public ListOfEpisodes(ArrayList<FeedParser.Item> episodes_list){

        for (FeedParser.Item item: episodes_list){
            Episode episode = new Episode(item.title,
                                          item.description,
                                          item.mp3URL,
                                          item.duration);
            arrayOfEpisodes.add(episode);
        }

    }

    public ArrayList<Episode> getList(){
        return this.arrayOfEpisodes;
    }


}
