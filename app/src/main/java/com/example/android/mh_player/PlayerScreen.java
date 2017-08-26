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
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;

public class PlayerScreen extends AppCompatActivity {

    private PlayerService       musicSrv;
    String                      mp3URL;
    private Intent              playIntent;
    private boolean             musicBound=false;
    //private PlayerController    controller;
    private boolean             paused=false, playbackPaused=false;

    private ImageButton playBtn;
    private ImageButton stopBtn;
    private ImageButton fwdBtn;
    private ImageButton replayBtn;
    private Boolean playBtnIcon_Play = true;
    private SeekBar seekBar;
    private int mp3_duration;

    Episode episode;

    //Get the url of the mp3 to play. Calls the MediaController set.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_screen);

//        Bundle b = this.getIntent().getExtras();
//        String[] array = b.getStringArray("MP3_INFO");

        episode = (Episode) this.getIntent().getSerializableExtra("Episode");
        mp3URL = episode.getMp3URL();
        mp3_duration = episode.getDuration();

        playBtn = (ImageButton) findViewById(R.id.play_btn);
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

        stopBtn = (ImageButton) findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.stopBtnPressed();
                playBtn.setImageResource(R.drawable.play);
                playBtnIcon_Play = true;
            }
        });

        fwdBtn = (ImageButton) findViewById(R.id.fwd_btn);
        fwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.fwdBtnPressed();
            }
        });

        replayBtn = (ImageButton) findViewById(R.id.replay_btn);
        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicSrv.replayBtnPressed();
            }
        });

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){

                    float f_prog = (float) progress;
                    float fpos =  mp3_duration*(f_prog/100);
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

        final Handler mHandler = new Handler();
        PlayerScreen.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicSrv !=null && musicSrv.isPlaying()){
                    float mCurrentPositionInMS = (float) musicSrv.getPosn();
                    float temp_progress = 100*(mCurrentPositionInMS/mp3_duration);
                    int progress = (int) temp_progress;

                    seekBar.setProgress(progress);
                }
                mHandler.postDelayed(this, 1000);
            }

        });
    }

    //Create the Service connection.
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //casting the service to the MusicBinder type
            PlayerService.MusicBinder binder = (PlayerService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();

            if (episode.isDownloaded()){
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), episode.getFilename());
                musicSrv.setURL(file.getAbsolutePath());
            } else {
                musicSrv.setURL(mp3URL);
            }

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    //Activity Life Cycle Methods
    /////////////////////////////

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    //After onCreate(), we connect to the service.
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
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    //Media Controller Methods
    //////////////////////////////

//    private void setController(){
//        controller = new PlayerController(this);
//        controller.setPrevNextListeners(
//
//         new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playNext();
//            }
//
//        }, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playPrev();
//            }
//        });
//
//        controller.setMediaPlayer(this);
//        controller.setAnchorView(findViewById(R.id.player_screen_view));
//        controller.setEnabled(true);
//    }

//    @Override
//    public int getDuration() {//Media Controller
//        if(musicSrv!=null && musicBound && musicSrv.isPng())
//            return musicSrv.getDur();
//        else return 0;
//    }
//
//    @Override
//    public int getCurrentPosition() {//Media Controller
//        if(musicSrv!=null && musicBound && musicSrv.isPng())
//            return musicSrv.getPosn();
//        else return 0;
//    }
//
//    @Override
//    public void pause() {//Media Controller
//        playbackPaused=true;
//        musicSrv.pausePlayer();
//    }
//
//    @Override
//    public void seekTo(int pos) {//Media Controller
//        musicSrv.seek(pos);
//    }
//
//    @Override
//    public void start() {//Media Controller
//        Toast.makeText(this, "Now Buffering...", Toast.LENGTH_LONG);
//        musicSrv.go();
//    }
//
//    @Override
//    public boolean isPlaying() {//Media Controller
//        if(musicSrv!=null && musicBound)
//            return musicSrv.isPng();
//        return false;
//    }
//
//    @Override
//    public int getBufferPercentage() {//Media Controller
//        return 0;
//    }
//
//    @Override
//    public boolean canPause() {//Media Controller
//        return true;
//    }
//
//    @Override
//    public boolean canSeekBackward() {//Media Controller
//        return true;
//    }
//
//    @Override
//    public boolean canSeekForward() {//Media Controller
//        return true;
//    }
//
//    @Override
//    public int getAudioSessionId() {//Media Controller
//        return 0;
//    }
//
//    private void playNext(){
//        //musicSrv.playNext(urls[1]);
//        if(playbackPaused){
//            setController();
//            playbackPaused=false;
//        }
//        controller.show(0);
//    }
//
//    private void playPrev(){
//        //musicSrv.playPrev(urls[0]);
//        if(playbackPaused){
//            setController();
//            playbackPaused=false;
//        }
//        controller.show(0);
//    }
//
//

}

