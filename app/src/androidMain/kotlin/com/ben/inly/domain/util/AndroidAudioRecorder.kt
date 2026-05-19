package com.ben.inly.domain.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

class AndroidAudioRecorder(private val context: Context) : AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentFile: File? = null
    private var recordingStartTime = 0L

    override fun startRecording() {
        val fileName = "voice_${UUID.randomUUID()}.m4a"
        currentFile = File(context.filesDir, fileName)

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(currentFile!!.absolutePath)
            prepare()
            start()
        }
        recordingStartTime = System.currentTimeMillis()
    }

    override fun stopRecording(cancel: Boolean): Pair<String, Int>? {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) { e.printStackTrace() }
        recorder = null

        if (cancel) {
            currentFile?.delete()
            return null
        }

        val durationSecs = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
        return currentFile?.name?.let { Pair(it, durationSecs) }
    }

    override fun play(fileName: String, onCompletion: () -> Unit) {
        player?.release()
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return

        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                onCompletion()
                release()
                player = null
            }
        }
    }

    override fun stopPlaying() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }
}