package com.example.android.mh_player;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class EpisodesActivity extends AppCompatActivity {
    //The screen that shows the podcasts episodes. It has a ToolBar on top.

    //On creation of the activity we call a FeedParser with the url of the RSS Feed received
    //from the intent.
    //The parser returns a list of items, which we convert to a list of Episode objects.
    //We then create the ListView and ListView adapter to display the Episode objects
    //on the screen and handle clicks on them.

    private EpisodeListViewAdapter  episode_adapter;
    private ArrayList<Episode>      episodes_list;


    //Gets RSS URL and calls the parser.
    //Creates the ToolBar.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodes_list);

        Intent intent = getIntent();
        String rssURL = intent.getExtras().getString("RSS_URL");

        new ParseRSS(rssURL).execute();

        //Create the ToolBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Episodes");
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("EpisodeActivity","onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("EpisodeActivity","onResume()");
    }

    //Parse the RSS Feed of the selected podcast in the background.
    //Creates the list of Episodes for the ListView.
    private class ParseRSS extends AsyncTask<Void, Void, ArrayList<Episode>> {
        private String rssURL;

        //Constructor
        private ParseRSS(String rssURL){
            this.rssURL = rssURL;
        }

        //The network call to read the RSS is done in the background.
        @Override
        protected ArrayList<Episode> doInBackground(Void... voids) {

            InputStream in = null;

            try {
                in = new URL(rssURL).openStream();
            } catch (IOException e) {
                Log.e("EpisodeActivity", "URL OpenStream error.");
                e.printStackTrace();
            }

            FeedParser feedParser = new FeedParser();
            return feedParser.parse(in);
        }

        //Takes a list of RSS Items, creates a list of Episodes for the ListView.
        //Handles clicks on the episodes.
        @Override
        protected void onPostExecute(ArrayList<Episode> ep_list){

            episodes_list = ep_list;

            episode_adapter = new EpisodeListViewAdapter(getApplicationContext(), episodes_list);

            ListView episodes_listview = (ListView) findViewById(R.id.episodes_list_view);
            episodes_listview.setAdapter(episode_adapter);

            episodes_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> episode_adapter, View view, int i, long l) {

                    Episode episode = (Episode) episode_adapter.getItemAtPosition(i);

                    Intent intent = new Intent(EpisodesActivity.this, PlayerActivity.class);
                    intent.putExtra("Episode", episode);
                    startActivity(intent);
                }
            });
        }
    }

    //Create the ListView Adapter.
    private class EpisodeListViewAdapter extends ArrayAdapter<Episode> {

        EpisodeListViewAdapter(Context context, ArrayList<Episode> episodeList){
            //Constructor
            super(context,0,episodeList);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {


            Episode episode = getItem(position);

            if (convertView == null){
                //If an old view is available (such as a one scrolled to the bottom) - we use it.
                convertView =
                        LayoutInflater.from(getContext()).inflate(R.layout.episodes_list_row_layout, parent, false);
            }

            //Grab the TextViews from the podcasts_list_row_layout
            TextView tvTitle       = (TextView) convertView.findViewById(R.id.episode_title);
            TextView tvDescription = (TextView) convertView.findViewById(R.id.episode_description);
            final Button tvBtn = (Button) convertView.findViewById(R.id.dl_button);

            //Set the Row views.
            if (episode != null){

                tvTitle.setText(episode.title);
                tvDescription.setText(episode.description);

                if (episode.isDownloaded()){
                    tvBtn.setText("Downloaded!");
                } else {
                    tvBtn.setText("Press To DL");
                }
            }


            tvBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvBtn.setText("Downloading");
                    new DownloadFileFromInternet().execute(new Integer(position));
                }
            });

            return convertView;
        }
    }

    //Downloads the mp3 in the background.
    private class DownloadFileFromInternet extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... position) {
            int pos = position[0];
            Episode episode = episodes_list.get(pos);

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), episode.file_name);

            int count;
            String url = episode.mp3URL;

            URL mp3url;
            try {
                mp3url = new URL(url);
                URLConnection connection =  mp3url.openConnection();
                connection.connect();
                //int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(mp3url.openStream());
                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];
                int total = 0;

                while ((count = input.read(data)) != -1) {
                    total = total + data.length;
                    output.write(data, 0, count);
                    //Log.e("FILE DL", "total: " + total + " out of " + lengthOfFile);
                }
                //Log.e("FILE DL", "done");
                // Flush output
                output.flush();
                output.close();
                input.close();


            } catch (MalformedURLException e) {
                Log.e("EpisodesActivity", "Malformed url");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("EpisodesActivity", "IOException");
                e.printStackTrace();
            }

            episode.setDownloadedTrue();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Toast.makeText(getApplicationContext(), "async done", Toast.LENGTH_SHORT).show();
            episode_adapter.notifyDataSetChanged();
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
                Toast.makeText(this, "Podcasts Pressed", Toast.LENGTH_SHORT).show();
                break;

            case R.id.episodes_activity:
                //Do Nothing
                break;
            //Intent second_activity_intent = new Intent(this, EpisodesList.class);
            //startActivity(second_activity_intent);

            case R.id.player_activity:
                Toast.makeText(this, "Player Pressed", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

}
