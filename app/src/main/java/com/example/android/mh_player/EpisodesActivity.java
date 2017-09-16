package com.example.android.mh_player;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private String rssURL;

    /// Life Cycle Methods
    /////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Gets RSS URL and calls the parser.
        //Creates the ToolBar.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodes_list);

        Log.i("MH_PLAYER_APP", "EpisodesActivity, onCreate()");

        Intent intent = getIntent();
        rssURL = intent.getStringExtra("PODCAST_RSS_URL");

        if (rssURL == null){
            //The activity was started from the toolbar, and there is
            //no rssURL already loaded:
            //Get the last podcast used before the app was closed.
            SharedPreferences sharedPreferences;
            sharedPreferences = getSharedPreferences("MH_PLAYER_PREF", 0);
            rssURL = sharedPreferences.getString("LAST_USED_RSS_FEED", "http://www.ranlevi.com/feed/podcast/");
        }

        new ParseRSS(rssURL).execute();

        //Create the ToolBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Episodes");
        setSupportActionBar(toolbar);
    }

    /////-------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("MH_PLAYER_APP", "EpisodesActivity, onStart()");
    }

    /////-------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MH_PLAYER_APP", "EpisodesActivity, onCreate()");
    }

    /////-------------------------------------------
    @Override
    protected void onPause() {
        Log.i("MH_PLAYER_APP", "EpisodesActivity, onPause()");
        super.onPause();
    }

    /////-------------------------------------------
    @Override
    protected void onStop() {
        Log.i("MH_PLAYER_APP", "EpisodesActivity, onPause()");
        super.onStop();
    }

    /////-------------------------------------------
    @Override
    protected void onDestroy() {
        Log.i("MH_PLAYER_APP", "EpisodesActivity, onDestroy()");
        super.onDestroy();

        //Save the last used RSS feed url, for the next time the app opens.
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("MH_PLAYER_PREF", 0);

        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();
        editor.putString("LAST_USED_RSS_FEED", rssURL);
        editor.commit();
    }

    /////-------------------------------------------
    @Override
    protected void onRestart() {
        Log.i("MH_PLAYER_APP", "EpisodesActivity, onRestart()");
        super.onRestart();
    }

    /////-------------------------------------------
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("MH_PLAYER_APP", "EpisodesActivity, onNewIntent()");
    }

    ////////////////////////////////////////////////////
    //////////////////////////////////////////////////


    private class ParseRSS extends AsyncTask<Void, Void, ArrayList<Episode>> {
        //Parse the RSS Feed of the selected podcast in the background.
        //Creates the list of Episodes for the ListView.

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

                    // Start the PlayerActivity
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("SELECTED_EPISODE", episode);

                    Intent intent = new Intent(EpisodesActivity.this, PlayerActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
    }

    /////-------------------------------------------

    private class EpisodeListViewAdapter extends ArrayAdapter<Episode> {
        //Create the ListView Adapter.

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

    /////-------------------------------------------

    private class DownloadFileFromInternet extends AsyncTask<Integer, Void, Void>{
        //Downloads the mp3 in the background.

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
    /////-------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Get the menu for this activity.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /////-------------------------------------------
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(1).setIcon(R.drawable.ic_episodes_screen_white_24dp);
        menu.getItem(1).setEnabled(false);
        return super.onPrepareOptionsMenu(menu);
    }

    /////-------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.podcasts_activity:
                Intent intent = new Intent(EpisodesActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;

            case R.id.player_activity:
                //Toast.makeText(this, "Player Pressed", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

}
