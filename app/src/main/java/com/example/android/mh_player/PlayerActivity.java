package com.example.android.mh_player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {
    //Displays the audio player control. Each press delegates the action
    //to the PlayerService.

    private PlayerService   musicSrv;
    private String          episode_title;
    private String          episode_description;
    private int             episode_duration;
    private String          episode_url;
    private String          episode_id;
    private int             episode_playing_position;
    Episode                 episode;

    //Create the Service connection.
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //casting the service to the MusicBinder type
            PlayerService.MusicBinder binder = (PlayerService.MusicBinder) service;
            musicSrv = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //null
        }
    };

    /// Life Cycle Methods
    /////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i("MH_PLAYER_APP", "PlayerActivity, onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_screen);

        //Create the ToolBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Player");
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle == null){
            //Activity started from toolbar.
            if (episode == null) {
                //no prior episode already selected. We load the last episode played or default.
                SharedPreferences sharedPreferences;
                sharedPreferences = getSharedPreferences("MH_PLAYER_PREF", 0);

                episode_title = sharedPreferences.getString("LAST_USED_EPISODE_TITLE", "223: ניתוחי לב, חלק א");
                episode_description = sharedPreferences.getString("LAST_USED_EPISODE_DESC", "בפרק זה הצטרפתי לצוות מחלקת כירורגית לב של בית החולים רמב&quot;ם בחיפה, לניתוח מעקפים דחוף בחולה שעבר התקף לב. מהו אוטם שריר הלב, מהי הטעות שעשתה האבולוציה בלב האנושי, והאם אתעלף כשהמנתח יפתח את בית החזה?...קדימה, היכנסו איתי לחדר הניתוחים: הניתוח עומד להתחיל.");
                episode_duration = sharedPreferences.getInt("LAST_USED_EPISODE_DURATION", 100); //need to change
                episode_url = sharedPreferences.getString("LAST_USED_EPISODE_URL", "http://traffic.cast.plus/59425bb8b5637505f5a3d444/ranlevi.podbean.com/mf/play/e4rsiw/Osim_Historiya_223_HeartSurgery_Part_1_MST.mp3");
                episode_playing_position = sharedPreferences.getInt("LAST_USED_EPISODE_POSITION", 0);
                episode_id = sharedPreferences.getString("LAST_USED_EPISODE_ID", "dummyID");//need to change
            }
        } else {
            //Activity started from Episodes Activity
            episode = (Episode) bundle.getSerializable("SELECTED_EPISODE");

            episode_title = episode.title;
            episode_description = episode.description;
            episode_duration = episode.duration;
            episode_playing_position = 0;
            episode_id = episode.episode_id;

            if (episode.isDownloaded()){
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), episode.file_name);
                    episode_url = file.getAbsolutePath();
                } else {
                    episode_url = episode.mp3URL;
                }
        }

        Intent serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.putExtra("EPISODE_URL", episode_url);
        serviceIntent.putExtra("EPISODE_ID", episode_id);
        serviceIntent.putExtra("EPISODE_PLAYING_POSITION", episode_playing_position);
        bindService(serviceIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);

        // Create the Player and button logic
        ///////////////////////////////////////

        final ImageButton playBtn = (ImageButton) findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (musicSrv.isPlaying()){
                    musicSrv.pauseBtnPressed();
                    playBtn.setImageResource(R.drawable.ic_pause_white_24dp);
                } else if (musicSrv.isIdle()){
                    musicSrv.playBtnPressed();
                    playBtn.setImageResource(R.drawable.play);
                } else if (musicSrv.isPaused()){
                    musicSrv.playBrnPressedWhenPaused();
                    playBtn.setImageResource(R.drawable.play);
                }
            }
        });

        ImageButton stopBtn = (ImageButton) findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicSrv.isPlaying()){
                    musicSrv.stopBtnPressed();
                    playBtn.setImageResource(R.drawable.play);
                }
            }
        });

        ImageButton fwdBtn = (ImageButton) findViewById(R.id.fwd_btn);
        fwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.fwdBtnPressed();
            }
        });

        ImageButton replayBtn = (ImageButton) findViewById(R.id.replay_btn);
        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.replayBtnPressed();
            }
        });

        final SeekBar seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){

                    float f_prog = (float) progress;
                    float fpos =  episode_duration*(f_prog/100);
                    int pos = (int) fpos;
                    musicSrv.seekTo(pos);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });

        //Handle the seekBar progress during play.
        final Handler mHandler = new Handler();
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //if (musicSrv !=null && musicSrv.isPlaying()){
                if (musicSrv !=null) {
                    float mCurrentPositionInMS = (float) musicSrv.getPosn();
                    float temp_progress = 100 * (mCurrentPositionInMS / episode_duration);
                    int progress = (int) temp_progress;

                    seekBar.setProgress(progress);
                }
                mHandler.postDelayed(this, 1000);
            }

        });
    }

    /////-------------------------------------------
    @Override
    protected void onDestroy() {
        Log.i("MH_PLAYER_APP", "PlayerActivity, onDestroy()");

        //Save the last played episode
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("MH_PLAYER_PREF", 0);

        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();

        editor.putString("LAST_USED_EPISODE_TITLE", episode_title);
        editor.putString("LAST_USED_EPISODE_DESC", episode_description);
        editor.putInt("LAST_USED_EPISODE_DURATION", episode_duration);
        editor.putString("LAST_USED_EPISODE_URL", episode_url);
        editor.putInt("LAST_USED_EPISODE_POSITION", musicSrv.getPosn());
        editor.putString("LAST_USED_EPISODE_ID", episode_id);

        editor.commit();

        unbindService(musicConnection);
        Intent serviceIntent = new Intent(this, PlayerService.class);
        stopService(serviceIntent);
        musicSrv=null;
        super.onDestroy();
    }

    /////-------------------------------------------
    //If the service has not been started before (==playIntent is null),
    //We start it here.
    //Note that the connection to the service is already created
    //when the activity is created.
    @Override
    protected void onStart() {
        Log.i("MH_PLAYER_APP", "PlayerActivity, onStart()");

        super.onStart();
    }

    /////-------------------------------------------
    @Override
    protected void onPause(){
        Log.i("MH_PLAYER_APP", "PlayerActivity, onPause()");
        super.onPause();
    }

    /////-------------------------------------------
    @Override
    protected void onResume(){
        Log.i("MH_PLAYER_APP", "PlayerActivity, onResume()");
        super.onResume();
    }

    /////-------------------------------------------
    @Override
    protected void onStop() {
        Log.i("MH_PLAYER_APP", "PlayerActivity, onStop()");
        super.onStop();
    }

    /////-------------------------------------------
    //Create the ToolBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Get the menu for this activity.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /////-------------------------------------------
    //What happens when a ToolBar item is clicked.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.podcasts_activity:
                Toast.makeText(this, "Podcasts Pressed", Toast.LENGTH_SHORT).show();
                break;

            case R.id.episodes_activity:
                Toast.makeText(this, "Episodes Pressed", Toast.LENGTH_SHORT).show();
                break;
            //Intent second_activity_intent = new Intent(this, EpisodesList.class);
            //startActivity(second_activity_intent);

            case R.id.player_activity:
                //Do nothing
                break;
        }

        return true;
    }

}

