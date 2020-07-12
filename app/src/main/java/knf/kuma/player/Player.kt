package knf.kuma.player

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.RemoteException
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import android.webkit.URLUtil
import androidx.media.AudioAttributesCompat
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import knf.kuma.commons.BypassUtil
import knf.kuma.commons.PrefsUtil
import knf.kuma.commons.noCrash
import okhttp3.OkHttpClient
import xdroid.toaster.Toaster

data class PlayerState(var window: Int = 0,
                       var position: Long = 0,
                       var whenReady: Boolean = true)

class PlayerHolder(private val context: Context,
                   private val playerState: PlayerState,
                   private val playerView: PlayerView,
                   intent: Intent) {
    val audioFocusPlayer: ExoPlayer
    val playerCallback: PlayerCallback
    private var listPosition = 0
    var retriever = MediaMetadataRetriever()
    private val mediaCatalog: MediaCatalog

    // Create the exoPlayer instance.
    init {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioAttributes = AudioAttributesCompat.Builder()
                .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                .build()
        playerCallback = context as PlayerCallback
        mediaCatalog = MediaCatalog(mutableListOf(), intent)
        playerCallback.onChangeTitle((mediaCatalog[listPosition].title ?: "").toString())
        setUpRetriever(mediaCatalog[listPosition])
        if (mediaCatalog.size == 1) playerCallback.onChangeTitle(mediaCatalog[0].title.toString())
        audioFocusPlayer = AudioFocusWrapper(
                audioAttributes,
                audioManager,
                ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
                        .also { playerView.player = it }
        )
    }

    private fun setUpRetriever(description: MediaDescriptionCompat) {
        description.mediaUri?.let {
            noCrash {
                if (URLUtil.isFileUrl(it.toString()))
                    retriever.setDataSource(context, it)
                else
                    retriever.setDataSource(it.toString(), mapOf())
            }
        }
    }

    private fun buildMediaSource(): MediaSource {
        val uriList = mutableListOf<MediaSource>()
        mediaCatalog.forEach {
            uriList.add(createExtractorMediaSource(it.mediaUri ?: Uri.EMPTY))
        }
        return ConcatenatingMediaSource(*uriList.toTypedArray())
    }

    private fun createExtractorMediaSource(uri: Uri): MediaSource {
        return ProgressiveMediaSource.Factory(
                if (PrefsUtil.useExperimentalOkHttp)
                    DefaultDataSourceFactory(context, null, OkHttpDataSourceFactory(OkHttpClient(), BypassUtil.userAgent))
                else
                    DefaultDataSourceFactory(context, BypassUtil.userAgent, null)
        ).createMediaSource(uri)
    }

    // Prepare playback.
    fun start() {
        // Load media.
        audioFocusPlayer.prepare(buildMediaSource())
        // Restore state (after onResume()/onStart())
        with(playerState) {
            // Start playback when media has buffered enough
            // (whenReady is true by default).
            audioFocusPlayer.seekTo(window, position)
            audioFocusPlayer.playWhenReady = whenReady
            // Add logging.
            attachLogging(audioFocusPlayer)
        }
    }

    // Stop playback and release resources, but re-use the exoPlayer instance.
    fun stop() {
        with(audioFocusPlayer) {
            // Save state
            saveState()
            // Stop the exoPlayer (and release it's resources). The exoPlayer instance can be reused.
            stop(true)
        }
    }

    fun skip() {
        with(audioFocusPlayer) {
            seekTo(currentWindowIndex, currentPosition + 85000)
        }
    }

    fun saveState() {
        with(audioFocusPlayer) {
            with(playerState) {
                position = currentPosition
                window = currentWindowIndex
                whenReady = playWhenReady
            }
        }
    }

    // Destroy the exoPlayer instance.
    fun release() {
        audioFocusPlayer.release() // exoPlayer instance can't be used again.
    }

    /**
     * For more info on ExoPlayer logging, please review this
     * [codelab](https://codelabs.developers.google.com/codelabs/exoplayer-intro/#5).
     */
    private fun attachLogging(exoPlayer: ExoPlayer) {
        // Show toasts on state changes.
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        playerCallback.onFinish()
                    }
                }
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                var exception: Throwable? = null
                when (error?.type) {
                    ExoPlaybackException.TYPE_RENDERER -> exception = error.rendererException
                    ExoPlaybackException.TYPE_UNEXPECTED -> exception = error.unexpectedException
                    ExoPlaybackException.TYPE_SOURCE -> exception = error.sourceException
                    ExoPlaybackException.TYPE_OUT_OF_MEMORY -> exception = error.outOfMemoryError
                    ExoPlaybackException.TYPE_REMOTE -> exception = RemoteException(error.message)
                }
                if (exception != null) {
                    Toaster.toast("Error al reproducir: " + exception.message?.replace("%", "%%"))
                    FirebaseCrashlytics.getInstance().recordException(exception)
                } else {
                    Toaster.toast("Error desconocido al reproducir")
                }
                playerCallback.onFinish()
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                playerCallback.onLoadingChange(isLoading)
            }

            override fun onPositionDiscontinuity(reason: Int) {
                try {
                    val latestPosition = playerView.player.currentWindowIndex
                    if (latestPosition != listPosition) {
                        listPosition = latestPosition
                        playerCallback.onChangeTitle((mediaCatalog[listPosition].title
                                ?: "").toString())
                        setUpRetriever(mediaCatalog[listPosition])
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        // Write to log on state changes.
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Log.i("Player", "playerStateChanged: ${getStateString(playbackState)}, $playWhenReady")
                playerCallback.onPlayerStateChanged(playbackState, playWhenReady)
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Log.i("Player", "playerError: $error")
            }

            fun getStateString(state: Int): String {
                return when (state) {
                    Player.STATE_BUFFERING -> "STATE_BUFFERING"
                    Player.STATE_ENDED -> "STATE_ENDED"
                    Player.STATE_IDLE -> "STATE_IDLE"
                    Player.STATE_READY -> "STATE_READY"
                    else -> "?"
                }
            }


        })

    }

    interface PlayerCallback {
        fun onChangeTitle(title: String)
        fun onLoadingChange(loading: Boolean)
        fun onPlayerStateChanged(state: Int, playWhenReady: Boolean)
        fun onFinish()
    }

}