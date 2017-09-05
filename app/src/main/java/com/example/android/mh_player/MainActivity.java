package com.example.android.mh_player;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/*
TODO:
Manange life cycle for all activities
Change theme colors
Change toolbar title
*/

public class MainActivity extends AppCompatActivity {
    //This is the main screen of the application.
    //It has a ToolBar on top, and shows a list of podcasts.

    //We create a ListOfPodcasts object, which is a list of Podcast objects.
    //Initialization of the Podcast objects is hard-coded.
    //We then create the ListView and ListView adapter that shows the podcast objects,
    //and handles clicks on them.

    //Create the ToolBar.
    //Create the ListView and its onItemClick()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Podcasts");
        setSupportActionBar(toolbar);

        ArrayList<Podcast> listOfPodcasts = new ListOfPodcasts().getList();

        PodcastListViewAdapter podcast_adapter =
                new PodcastListViewAdapter(this, listOfPodcasts);

        final ListView listview = (ListView) findViewById(R.id.list_view);
        listview.setAdapter(podcast_adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //When a podcast has been clicked, we call the EpisodesActivity
                //and pass it the RSS Feed URL of the podcast.
                Podcast podcast = (Podcast) adapterView.getItemAtPosition(i);

                Intent intent = new Intent(MainActivity.this, EpisodesActivity.class);
                intent.putExtra("RSS_URL", podcast.rssFeedURL);
                startActivity(intent);
            }
        });
    }

    //Create the ListView Adapter.
    class PodcastListViewAdapter extends ArrayAdapter<Podcast> {

        PodcastListViewAdapter(Context context, ArrayList<Podcast> podcastsList){
            //Constructor
            super(context,0,podcastsList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            //ArrayAdapter Method.
            Podcast podcast = getItem(position);

            if (convertView == null){
                //If an old view is available (such as a one scrolled to the bottom) - we use it.
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.podcasts_list_row_layout, parent, false);
            }

            //Grab the TextViews from the podcasts_list_row_layout
            TextView tvName        = (TextView) convertView.findViewById(R.id.name);
            TextView tvDescription = (TextView) convertView.findViewById(R.id.description);
            ImageView ibLogo       = (ImageView) convertView.findViewById(R.id.logo);

            //Set the Row views.
            if (podcast !=null){
                tvName.setText(podcast.name);
                tvDescription.setText(podcast.description);

                //path_to_logo is a string. We concert to ID first, then set the image.
                Drawable logo = getDrawable(getResources()
                        .getIdentifier(podcast.path_to_logo, "drawable", getPackageName()));
                ibLogo.setImageDrawable(logo);
            }

            return convertView;
        }
    }

    //Create the ToolBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Get the menu for this activity.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    //What happens when a ToolBar item is clicked.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.podcasts_activity:
                //do nothing
                break;

            case R.id.episodes_activity:
                //Toast.makeText(this, "Episodes Pressed", Toast.LENGTH_SHORT).show();

                //Intent intent = new Intent(MainActivity.this, EpisodesActivity.class);
                //intent.putExtra("RSS_URL", podcast.rssFeedURL);
                //startActivity(intent);
                break;

            case R.id.player_activity:
                Toast.makeText(this, "Player Pressed", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }


}
