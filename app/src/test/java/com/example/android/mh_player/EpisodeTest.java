package com.example.android.mh_player;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class EpisodeTest {

    @Test
    public void testEpisodeMethods() throws Exception {
        Episode episode = new Episode("Mock Title",
                                    "Mock Description",
                                    "http://www.mock_url.mp3",
                                    "01:02:03");

        assertThat(episode.getEpisodeTitle(),is("Mock Title"));
        assertThat(episode.getEpisodeDescription(),is("Mock Description"));
        assertThat(episode.getMp3URL(),is("http://www.mock_url.mp3"));
        assertThat(episode.getDuration(),is(3723000));
        assertThat(episode.isDownloaded(),is(false));
        assertThat(episode.getFilename().length(),is(32));

        episode.setDownloaded();
        assertThat(episode.isDownloaded(),is(true));
    }

}
