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
                                        "01:02:03",
                                        "mock duration",
                                        "mock link");

        assertThat(episode.title,is("Mock Title"));
        assertThat(episode.description,is("Mock Description"));
        assertThat(episode.mp3URL,is("http://www.mock_url.mp3"));
        assertThat(episode.duration,is(3723000));
        assertThat(episode.isDownloaded(),is(false));
        assertThat(episode.file_name.length(),is(32));

        episode.setDownloadedTrue();
        assertThat(episode.isDownloaded(),is(true));
    }

}
