package com.fengchaohuzhu.box

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import org.jetbrains.anko.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityUi().setContentView(this)
    }
}

class MainActivityUi : AnkoComponent<MainActivity> {

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        relativeLayout {
            textView {
                text = "Hello World"
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
