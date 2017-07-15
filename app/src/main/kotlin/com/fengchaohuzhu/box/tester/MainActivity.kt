package com.fengchaohuzhu.box.tester

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.jetbrains.anko.*

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    MainActivityUi().setContentView(this)
  }
}

class MainActivityUi : AnkoComponent<MainActivity> {
  override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
    verticalLayout {
      listView {
        adapter = ArrayAdapter<String>(ui.owner, android.R.layout.simple_list_item_1, arrayOf<String>("锁检测", "ID/IC卡检测"))
        onItemClickListener = object : AdapterView.OnItemClickListener {
          override fun onItemClick(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
            when (position) {
              0 -> {
                startActivity<LockActivity>()
              }
              1 -> {
                startActivity<CardActivity>()
              }
            }
          }
        }
      }
    }
  }
}
