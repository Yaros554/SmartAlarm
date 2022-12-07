package com.skyyaros.smartalarm.datasource

import android.content.SharedPreferences
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.SoundPool
import android.util.Log
import com.skyyaros.smartalarm.entity.Sound
import java.io.IOException

private const val SOUNDS_FOLDER = "sample_sounds"
private const val MAX_SOUNDS = 5
private const val SAVED_SOUND = "saved_sound"

class AlarmSound private constructor(private val assets: AssetManager, private val sharedPreferences: SharedPreferences) {
    var currentSoundIndex = 0
    private val sounds: List<Sound>
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_SOUNDS)
        .build()
    private var currentPlayId: Int? = null

    init {
        sounds = loadSounds()
        currentSoundIndex = sharedPreferences.getInt(SAVED_SOUND, 0)
    }

    fun play(index: Int = currentSoundIndex) {
        stop()
        val sound = sounds[index]
        Log.d("testAlarmSound", "Выбираю нужный трек")
        sound.soundId?.let {
            Log.d("testAlarmSound", "Готов к воспроизведению: $it")
            currentPlayId = soundPool.play(it, 1.0f, 1.0f, 1, 19, 1.0f)
            Log.d("testAlarmSound", "Воспроизводится: $currentPlayId")
        }
    }

    fun stop() {
        if (currentPlayId != null)
            soundPool.stop(currentPlayId!!)
    }

    fun release() {
        soundPool.release()
    }

    fun getSounds(): Array<String> {
        return sounds.map {
            it.name
        }.toTypedArray()
    }

    fun saveSettings() {
        sharedPreferences.edit().putInt(SAVED_SOUND, currentSoundIndex).apply()
    }

    private fun loadSounds(): List<Sound> {
        val soundNames: Array<String>
        try {
            soundNames = assets.list(SOUNDS_FOLDER)!!
            Log.d("testAlarmSound", "Треки распознаны")
        } catch (e: Exception) {
            Log.d("testAlarmSound", "Ошибка распознавания треков")
            return emptyList()
        }
        val sounds = mutableListOf<Sound>()
        soundNames.forEach { filename ->
            val assetPath = "$SOUNDS_FOLDER/$filename"
            val sound = Sound(assetPath)
            try {
                load(sound)
                sounds.add(sound)
            } catch (ioe: IOException) {
                Log.d("testAlarmSound", "Не могу добавить трек")
            }
        }
        return sounds
    }

    private fun load(sound: Sound) {
        val afd: AssetFileDescriptor = assets.openFd(sound.assetPath)
        val soundId = soundPool.load(afd, 1)
        Log.d("testAlarmSound", "id: $soundId")
        sound.soundId = soundId
    }

    companion object {
        private var INSTANCE: AlarmSound? = null
        fun initialize(assets: AssetManager, sharedPreferences: SharedPreferences) {
            if (INSTANCE == null) {
                Log.d("testAlarmSound", "Начало инициализации")
                INSTANCE = AlarmSound(assets, sharedPreferences)
            }
        }
        fun get(): AlarmSound {
            Log.d("testAlarmSound", "Возвращение готового объекта")
            return INSTANCE ?: throw IllegalStateException("AlarmSound must be initialized")
        }
    }
}