package com.fengchaohuzhu.box

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import org.jetbrains.anko.*

class CardActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CardActivityUi().setContentView(this)
  }
}

class CardActivityUi : AnkoComponent<CardActivity> {
  override fun createView(ui: AnkoContext<CardActivity>) = with(ui) {
    relativeLayout {
      textView {
        text = "ID/IC卡检测"
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
