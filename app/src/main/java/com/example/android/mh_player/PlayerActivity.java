package com.example.android.mh_player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
    private Intent          playIntent;
    private Boolean         playBtnIcon_Play = true;
    Episode                 episode;

    //Create the Service connection.
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //casting the service to the MusicBinder type
            PlayerService.MusicBinder binder = (PlayerService.MusicBinder) service;
            musicSrv = binder.getService();

            if (episode.isDownloaded()){
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), episode.file_name);
                musicSrv.setURL(file.getAbsolutePath());
            } else {
                musicSrv.setURL(episode.mp3URL);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //null
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_screen);

        //Create the ToolBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Player");
        setSupportActionBar(toolbar);

        episode = (Episode) this.getIntent().getSerializableExtra("Episode");

        final ImageButton playBtn = (ImageButton) findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.playBtnPressed();

                if (playBtnIcon_Play){
                    playBtn.setImageResource(R.drawable.ic_pause_white_24dp);
                } else {
                    playBtn.setImageResource(R.drawable.play);
                }
                playBtnIcon_Play = !playBtnIcon_Play;
            }
        });

        ImageButton stopBtn = (ImageButton) findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.stopBtnPressed();
                playBtn.setImageResource(R.drawable.play);
                playBtnIcon_Play = true;
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
                    float fpos =  episode.duration*(f_prog/100);
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
                if (musicSrv !=null && musicSrv.isPlaying()){
                    float mCurrentPositionInMS = (float) musicSrv.getPosn();
                    float temp_progress = 100*(mCurrentPositionInMS/episode.duration);
                    int progress = (int) temp_progress;

                    seekBar.setProgress(progress);
                }
                mHandler.postDelayed(this, 1000);
            }

        });
    }



    //Activity Life Cycle Methods
    /////////////////////////////

    @Override
    protected void onDestroy() {
        //stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    //If the service has not been started before (==playIntent is null),
    //We start it here.
    //Note that the connection to the service is already created
    //when the activity is created.
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, PlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

