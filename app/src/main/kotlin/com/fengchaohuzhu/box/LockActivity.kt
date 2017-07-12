package com.fengchaohuzhu.box

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import org.jetbrains.anko.*

class LockActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LockActivityUi().setContentView(this)
  }
}

class LockActivityUi : AnkoComponent<LockActivity> {
  override fun createView(ui: AnkoContext<LockActivity>) = with(ui) {
    relativeLayout {
      textView {
        text = "锁检测"
        textSize = 16f
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
