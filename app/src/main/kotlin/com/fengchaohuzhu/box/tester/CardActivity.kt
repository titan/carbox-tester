package com.fengchaohuzhu.box.tester

import android.app.ActionBar
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import org.jetbrains.anko.*

enum class CardViewID (val value: Int) {
  NUMBER(0x00020001)
}

class CardActivity : Activity() {
  val buf: StringBuffer = StringBuffer()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CardActivityUi().setContentView(this)
  }
  override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
    if (keyCode == KeyEvent.KEYCODE_ENTER) {
      find<TextView>(CardViewID.NUMBER.value).text = buf.toString()
      buf.delete(0, buf.length)
    } else {
      when(keyCode) {
        KeyEvent.KEYCODE_0 -> buf.append("0")
        KeyEvent.KEYCODE_1 -> buf.append("1")
        KeyEvent.KEYCODE_2 -> buf.append("2")
        KeyEvent.KEYCODE_3 -> buf.append("3")
        KeyEvent.KEYCODE_4 -> buf.append("4")
        KeyEvent.KEYCODE_5 -> buf.append("5")
        KeyEvent.KEYCODE_6 -> buf.append("6")
        KeyEvent.KEYCODE_7 -> buf.append("7")
        KeyEvent.KEYCODE_8 -> buf.append("8")
        KeyEvent.KEYCODE_9 -> buf.append("9")
      }
    }
    return true
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
      android.R.id.home -> startActivity(intentFor<MainActivity>().clearTop())
    }
    return (super.onOptionsItemSelected(menuItem))
  }
}

class CardActivityUi : AnkoComponent<CardActivity> {
  override fun createView(ui: AnkoContext<CardActivity>) = with(ui) {
    relativeLayout {
      textView {
        id = CardViewID.NUMBER.value
        text = ""
        textSize = 36f
        textColor = Color.WHITE
        gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
      }.lparams {
        width = wrapContent
        height = wrapContent
        centerInParent()
      }
    }
  }
}
