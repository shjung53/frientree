package com.d101.presentation

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object BackgroundMusicPlayer {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var musicList: List<String>
    private const val prefix = "music_"
    private val prefixPattern = Regex("^$prefix")

    fun playMusic(context: Context, musicName: String) {
        mediaPlayer = MediaPlayer.create(
            context,
            R.raw::class.java.getField(prefix + musicName).getInt(null),
        )
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    fun resumeMusic() {
        if(mediaPlayer.isPlaying.not()){
            Log.d("확인", "resumeMusic: ")
            mediaPlayer.start()
        }
    }

    fun pauseMusic() {
        if(mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
    }

    fun releaseMusicPlayer() {
        mediaPlayer.release()
    }

    fun getMusicList(): List<String> {
        return musicList
    }

    fun initMusicList(context: Context) {
        val raw = R.raw::class.java
        val fields = raw.fields
        musicList = fields.mapNotNull { field ->
            try {
                val resourceId = field.getInt(null)
                val name = context.resources.getResourceEntryName(resourceId)
                if (name.startsWith("music_")) {
                    prefixPattern.replace(name, "")
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
