package com.example.android.mh_player;

import org.junit.Test;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ListOfPodcastsTest {

    @Test
    public void testListOfEpisodes() throws Exception{
        ArrayList<Podcast> listOfPodcasts =
                new ListOfPodcasts().getList();

        Podcast makingHistory = listOfPodcasts.get(0);

        assertThat(makingHistory.name, is("עושים היסטוריה"));
        assertThat(makingHistory.description, is("פודקאסט על מדע, טכנולוגיה והיסטוריה"));
        assertThat(makingHistory.path_to_logo, is("making_history_logo_60px"));
        assertThat(makingHistory.rssFeedURL, is("http://www.ranlevi.com/feed/podcast/"));
    }
}