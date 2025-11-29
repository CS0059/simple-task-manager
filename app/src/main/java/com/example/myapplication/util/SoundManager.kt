package com.example.myapplication.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.myapplication.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private var taskCompletedSound: Int
    private var taskDeletedSound: Int
    private var taskCreatedSound: Int
    private var isSoundEnabled = true

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attributes)
            .build()

        // Load sound effects
        taskCompletedSound = soundPool.load(context, R.raw.task_completed, 1)
        taskDeletedSound = soundPool.load(context, R.raw.task_deleted, 1)
        taskCreatedSound = soundPool.load(context, R.raw.task_created, 1)
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun playTaskCompletedSound() {
        if (isSoundEnabled) {
            soundPool.play(taskCompletedSound, 0.8f, 0.8f, 2, 0, 1f)
        }
    }

    fun playTaskDeletedSound() {
        if (isSoundEnabled) {
            soundPool.play(taskDeletedSound, 1.5f, 1.5f, 3, 0, 1f)
        }
    }

    fun playTaskCreatedSound() {
        if (isSoundEnabled) {
            soundPool.play(taskCreatedSound, 1.5f, 1.5f, 3, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
} 