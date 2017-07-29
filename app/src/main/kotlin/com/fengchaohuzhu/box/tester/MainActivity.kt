package com.fengchaohuzhu.box.tester

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
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
      padding = dip(10)
      gridView {
        id = LockViewID.BOXES.value
        numColumns = 1
        stretchMode = GridView.STRETCH_COLUMN_WIDTH
        verticalSpacing = dip(30)
        horizontalSpacing = dip(10)
        adapter = MenuAdapter(listOf(
            MenuItem(title = "锁检测", action = {
              startActivity<LockActivity>()
            }),
            MenuItem(title = "IC卡检测", action = {
              startActivity<CardActivity>()
            }),
            MenuItem(title = "扬声器", action = {
              startActivity<SpeakerActivity>()
            }),
            MenuItem(title = "退出", action = {
              System.exit(0)
            })
        ))
        onItemClickListener = object : AdapterView.OnItemClickListener {
          override fun onItemClick(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
            val item : MenuItem = adapter.getItem(position) as MenuItem
            item.action()
          }
        }
      }.lparams(height = matchParent)
    }
  }
}

data class MenuItem (val title: String = "", val action: () -> Unit)

class MenuAdapter(val menus: List<MenuItem>) : BaseAdapter() {
  val items: List<MenuItem> = menus

  override fun getView(i : Int, v : View?, parent : ViewGroup?) : View {
    val item = getItem(i)
    return with(parent!!.context) {
      verticalLayout {
        backgroundColor = 0x07FFFFFF
        background = resources.getDrawable(R.drawable.grid_item_background)
        padding = dip(20)
        textView("${item.title}") {
          textSize = 28f
          gravity = Gravity.CENTER
        }
      }
    }
  }

  override fun getItem(position: Int): MenuItem {
    return items.get(position)
  }

  override fun getCount(): Int {
    return items.size
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun areAllItemsEnabled(): Boolean {
    return true
  }
}
