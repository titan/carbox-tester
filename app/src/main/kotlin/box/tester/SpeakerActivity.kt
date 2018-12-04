package box.tester

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.*

enum class SpeakerViewID (val value: Int) {
  PLAY(0x00030001),
  INCR(0x00030002),
  DECR(0x00030003),
}

class SpeakerActivity : Activity() {
  val buf: StringBuffer = StringBuffer()
  var player: MediaPlayer? = null
  var audioManager: AudioManager? = null
  var maxVolume = 15
  var currentVolume = 0
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    player = MediaPlayer.create(this, R.raw.sky_city)
    player?.setLooping(true)
    audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
    currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
    SpeakerActivityUi().setContentView(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val ab: ActionBar = getActionBar()
    ab.setHomeButtonEnabled(true)
    ab.setDisplayHomeAsUpEnabled(true)
    ab.show()
    return true
  }

  override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
    when (menuItem.getItemId()) {
      android.R.id.home -> {
        if (player?.isPlaying() ?: false) {
          player?.stop()
        }
        startActivity(intentFor<MainActivity>().clearTop())
      }
    }
    return (super.onOptionsItemSelected(menuItem))
  }
}

class SpeakerActivityUi : AnkoComponent<SpeakerActivity> {
  override fun createView(ui: AnkoContext<SpeakerActivity>) = with(ui) {
    relativeLayout {
      padding = dip(10)
      verticalLayout {
        linearLayout {
          val decr = button("-") {
            id = SpeakerViewID.DECR.value
          }
          val progress = horizontalProgressBar {
            max = ui.owner.maxVolume
            progress = ui.owner.currentVolume
          }.lparams {
            weight = 1.0f
            width = 0
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
          }
          val incr = button("+") {
            id = SpeakerViewID.INCR.value
          }
          decr.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View): Unit {
              if (progress.progress > 0) {
                progress.progress -= 1
                ui.owner.audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress.progress, 0)
              }
            }
          })
          incr.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View): Unit {
              if (progress.progress < progress.max) {
                progress.progress += 1
                ui.owner.audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress.progress, 0)
              }
            }
          })
        }.lparams {
          width = matchParent
        }
        val play = button(if (ui.owner.player?.isPlaying() ?: false) "暂停" else "播放") {
          id = SpeakerViewID.PLAY.value
          textSize = 36f
        }
        play.setOnClickListener(object: View.OnClickListener {
          override fun onClick(v: View): Unit {
            if (ui.owner.player?.isPlaying() ?: false) {
              ui.owner.player?.pause()
              play.text = "播放"
            } else {
              ui.owner.player?.start()
              play.text = "暂停"
            }
          }
        })
      }.lparams {
          width = matchParent
          height = wrapContent
          centerInParent()
        }
    }
  }
}
