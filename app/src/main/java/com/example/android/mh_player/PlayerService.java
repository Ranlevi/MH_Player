package com.example.android.mh_player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{

    private MediaPlayer         player;
    private int                 mediaPlayerState = 0;
    private NotificationManager notificationManager;
    private int                 current_position = 0;
    private DatabaseReference   mDatabase;
    private String              episode_url;
    private String              episode_id;

    //private int MP_PREPARED          = 2;
    private int MP_IDLE              = 0;
    private int MP_STARTED           = 3;
    private int MP_PAUSED            = 4;


    /// Life Cycle Methods
    /////////////////////////////////

    public void onCreate(){
        //This method is called only once, so here we create & config the MediaPlayer.
        Log.i("MH_PLAYER_APP", "PlayerService, onCreate()");
        super.onCreate();

        //create player, configure and register listeners.
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

    }

    /////-------------------------------------------
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("MH_PLAYER_APP", "PlayerService, onStartCommand()");

        if (player.isPlaying()){
            player.stop();
            player.reset();
        }

        episode_url = intent.getStringExtra("EPISODE_URL");
        current_position = intent.getIntExtra("EPISODE_PLAYING_POSITION",0);
        episode_id = intent.getStringExtra("EPISODE_ID");

        return super.onStartCommand(intent, flags, startId);
    }

    /////-------------------------------------------
    class MusicBinder extends Binder {
        //Returns a Binder object to the calling activity. The object
        //contains a reference to this service.

        PlayerService getService() {
            return PlayerService.this;
        }
    }
    //Create an IBinder object to send to the bound activity.
    private final IBinder musicBind = new MusicBinder();

    @Override
    public IBinder onBind(Intent intent) {
        //Return an IBinder object to the bound activity.
        return musicBind;
    }

    /////-------------------------------------------
    @Override
    public void onDestroy() {
        //Stop and Release the MediaPlayer.
        Log.i("MH_PLAYER_APP", "PlayerService, onDestroy()");

        notificationManager.cancel(0);

        player.stop();
        player.release();

        mDatabase = null;
    }

    ////////////////////////////////////////
    ////////////////////////////////////////

    @Override
    public boolean onUnbind(Intent intent){
        Log.i("MH_PLAYER_APP", "PlayerService, onUnbind()");
        return false;
    }

    /// MediaPlayer Methods
    /////////////////////////////

    @Override
    public void onPrepared(MediaPlayer mp) {

//        mediaPlayerState = MP_PREPARED;
        player.start();
        if (current_position != 0){
            mp.seekTo(current_position);
        }
        mediaPlayerState = MP_STARTED;

        //Creating the notification
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification n = new Notification.Builder(this)
                .setContentTitle("test title")
                .setContentText("test text")
                .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(0, n);

        //file_duration = mp.getAudioSessionId();
    }

    /////-------------------------------------------
    //OnError, bring the player to Idle state.
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    /////-------------------------------------------
    @Override
    public void onCompletion(MediaPlayer mp) {
        //called when track is done. We do nothing.
    }

    //AUDIO MANAGER METHODS
    ////////////////////////////////////////

    @Override
    public void onAudioFocusChange(int i) {

    }

    //METHODS CALLED FROM THE PlayerActivity
    //////////////////////////////////////////

//    public void setURL(String episode_url){
//        mp3URL = episode_url;
//    }

    /////-------------------------------------------
    public void playBtnPressed(){

        if (mediaPlayerState == MP_IDLE){
            try {
                player.setDataSource(episode_url);
                player.prepareAsync();
            }
            catch(Exception e){
                Log.e("MH_PLAYER_APP", "Player Service: onStartCommand(), Error setting data source", e);

            }
        }

    }

    public void playBrnPressedWhenPaused(){
        player.start();
        mediaPlayerState = MP_STARTED;
    }

    public void pauseBtnPressed(){
        player.pause();
        mediaPlayerState = MP_PAUSED;
    }

    /////-------------------------------------------
    public void stopBtnPressed(){

        if (mediaPlayerState == MP_PAUSED || mediaPlayerState == MP_STARTED){

            current_position = player.getCurrentPosition();

            DatabaseReference mDatabase;
            mDatabase = FirebaseDatabase.getInstance().getReference();

            HashMap<String,Integer> data = new HashMap<String, Integer>();
            data.put(episode_id,Integer.valueOf(current_position));
            mDatabase.push().setValue(data);

            player.stop();
            player.reset();
            current_position = 0;
        }
    }

    /////-------------------------------------------
    public void fwdBtnPressed(){
        if (player.isPlaying()){
            int currentTime = player.getCurrentPosition(); //in MS.
            player.seekTo(currentTime + 30000);
        }
    }

    /////-------------------------------------------
    public void replayBtnPressed(){
        if (player.isPlaying()){
            int currentTime = player.getCurrentPosition(); //in MS.

            if (currentTime > 30000){
                player.seekTo(currentTime - 30000);
            } else {
                player.seekTo(0);
            }
        }
    }

    /////-------------------------------------------
    public boolean isPlaying(){
        return player.isPlaying();//Same as MP_STARTED
    }

    /////-------------------------------------------
    public int getPosn(){
        if (mediaPlayerState == MP_PAUSED || mediaPlayerState == MP_STARTED){
            return player.getCurrentPosition();
        } else {
            return 0;
        }
    }

    /////-------------------------------------------
    public boolean isPaused(){
        if (mediaPlayerState == MP_PAUSED){
            return true;
        } else {
            return false;
        }
    }

    /////-------------------------------------------
    public boolean isIdle(){
        if (mediaPlayerState == MP_IDLE){
            return true;
        } else {
            return false;
        }
    }


    /////-------------------------------------------
    public void seekTo(int posn){
        if (mediaPlayerState == MP_PAUSED || mediaPlayerState == MP_STARTED){
            player.seekTo(posn);
        }

    }

//    /////-------------------------------------------
//    public void playPrev(String song_url){
//        playSong(song_url);
//    }
//
//    /////-------------------------------------------
//    //skip to next
//    public void playNext(String song_url){
//        playSong(song_url);
//    }

//    /////-------------------------------------------
//    public void playSong(String song_url){
//        //play a song
//        player.reset();
//
//        try{
//            player.setDataSource(song_url);
//        }
//        catch(Exception e){
//            Log.e("MH_PLAYER_APP", "Player Service: PlaySong() Error setting data source", e);
//        }
//        player.prepareAsync();
//    }
}
