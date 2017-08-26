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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class EpisodesScreen extends AppCompatActivity {
    //The screen that shows the podcasts episodes. It has a ToolBar on top.

    //On creation of the activity we call a FeedParser with the url of the RSS Feed recived
    //from the intent.
    //The parser returns a list of items, which we convert to a list of Episode objects.
    //We then create the ListView and ListView adapter to display the Episode objects
    //on the screen and handle clicks on them.

    private EpisodeListViewAdapter episode_adapter;
    private ListOfEpisodes listOfEpisodes;

    //Gets RSS URL and calls the parser.
    //Creates the ToolBar.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodes_list);

        //Get the RSS Feed URL of the selected podcast and parse it.
        Intent intent = getIntent();
        String rssURL = intent.getExtras().getString("RSS_URL");

        //Create the ToolBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new ParseRSS(rssURL).execute();
    }

    //Parse the RSS Feed of the selected podcast in the background.
    //Creates the list of Episodes for the ListView.
    private class ParseRSS extends AsyncTask<Void, Void, ArrayList<FeedParser.Item>> {
        private String rssURL;

        //Constructor
        private ParseRSS(String rssURL){
            this.rssURL = rssURL;
        }

        //The network call to read the RSS is done in the background.
        @Override
        protected ArrayList<FeedParser.Item> doInBackground(Void... voids) {

            InputStream in = null;

            try {
                in = new URL(rssURL).openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FeedParser feedParser = new FeedParser();
            return feedParser.parse(in);
        }

        //Takes a list of RSS Items, creates a list of Episodes for the ListView.
        //Handles clicks on the episodes.
        @Override
        protected void onPostExecute(ArrayList<FeedParser.Item> episodes_list){

            //ListOfEpisodes listOfEpisodes = new ListOfEpisodes(episodes_list);
            listOfEpisodes = new ListOfEpisodes(episodes_list);

            episode_adapter = new EpisodeListViewAdapter(getApplicationContext(), listOfEpisodes.getList());

            ListView episodes_listview = (ListView) findViewById(R.id.episodes_list_view);
            episodes_listview.setAdapter(episode_adapter);

            episodes_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> episode_adapter, View view, int i, long l) {

                    Episode episode = (Episode) episode_adapter.getItemAtPosition(i);
                    //String mp3URL = episode.getMp3URL();

                    Intent intent = new Intent(EpisodesScreen.this, PlayerScreen.class);

//                    Bundle b = new Bundle();
//                    b.putStringArray("MP3_INFO", new String[]{episode.getMp3URL(), episode.getDuration()});
//                    intent.putExtras(b);

                    intent.putExtra("Episode", episode);
                    startActivity(intent);
                }
            });
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
            case R.id.main_activity:
                Toast.makeText(this, "Main Activity Pressed", Toast.LENGTH_SHORT).show();
                break;

            case R.id.second_activity:
                Toast.makeText(this, "2nd Activity Pressed", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    //Create the ListView Adapter.
    public class EpisodeListViewAdapter extends ArrayAdapter<Episode> {

        public EpisodeListViewAdapter(Context context, ArrayList<Episode> episodeList){
            //Constructor
            super(context,0,episodeList);
        }

        @NonNull
        @Override
        //public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            //ArrayAdapter Method.
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
            tvTitle.setText(episode.getEpisodeTitle());
            tvDescription.setText(episode.getEpisodeDescription());

            if (episode.isDownloaded()){
                tvBtn.setText("Downloaded!");
            } else {
                tvBtn.setText("Press To DL");
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

    private class DownloadFileFromInternet extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... position) {
            int pos = position[0];
            Episode episode = listOfEpisodes.getList().get(pos);

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), episode.getFilename());

            int count;
            String url = episode.getMp3URL();

            URL mp3url;
            try {
                mp3url = new URL(url);
                URLConnection connection =  mp3url.openConnection();
                connection.connect();
                int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(mp3url.openStream());
                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];
                int total = 0;

                while ((count = input.read(data)) != -1) {
                    total = total + data.length;
                    output.write(data, 0, count);
                    Log.e("FILE DL", "total: " + total + " out of " + lengthOfFile);
                }
                Log.e("FILE DL", "done");
                // Flush output
                output.flush();
                output.close();
                input.close();


            } catch (MalformedURLException e) {
                Log.e("URL ERROR", "Malformed url");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("URL ERROR", "IOException");
                e.printStackTrace();
            }

            episode.setDownloaded();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "async done", Toast.LENGTH_SHORT).show();
            episode_adapter.notifyDataSetChanged();
        }
    }


}

//https://stackoverflow.com/questions/21161959/custom-arrayadapter-and-onclicklistener-for-a-button-in-a-row
//http://programmerguru.com/android-tutorial/android-asynctask-example/