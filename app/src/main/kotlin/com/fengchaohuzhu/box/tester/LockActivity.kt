package com.fengchaohuzhu.box.tester

import java.io.File
import java.io.FilenameFilter

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.Spinner
import android.widget.TextView
import com.fazecast.jSerialComm.SerialPort
import org.jetbrains.anko.*
import LockerSDK

enum class ViewID (val value: Int) {
  DEVICES(0x00010001),
  BOXES(0x00010002),
  ERROR_CONTAINER(0x00010003),
  ERROR(0x00010004),
}

class LockActivity : Activity(), AnkoLogger {
  var board: Byte = 0
  var sdk: LockerSDK? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LockActivityUi().setContentView(this)
    doAsync() {
      val ports = File("/dev/").list {
        _, filename -> !filename.equals("ttyS0") && filename.startsWith("ttyS") || filename.startsWith("ttyGS") || filename.startsWith("ttyUSB")
      }.map {
        SerialPort.getCommPort(it)
      }.reversed()
      uiThread {
        val spinner = find<Spinner>(ViewID.DEVICES.value)
        val adapter: PortAdapter = spinner.adapter as PortAdapter
        adapter.addAll(ports)
        spinner.setSelection(1)
        adapter.notifyDataSetChanged()
      }
    }
  }
  fun scan(port: SerialPort) {
    doAsync() {
      port.setBaudRate(9600)
      port.setNumDataBits(8)
      port.setNumStopBits(1)
      port.setParity(0)
      if (!port.openPort()) {
        uiThread {
          find<Spinner>(ViewID.DEVICES.value).setEnabled(true)
          find<TextView>(ViewID.ERROR.value).text = "无法打开串口设备 ${port.systemPortName}"
          find<View>(ViewID.ERROR_CONTAINER.value).visibility = View.VISIBLE
          find<GridView>(ViewID.BOXES.value).visibility = View.INVISIBLE
        }
      } else {
        var dialog: ProgressDialog? = null
        uiThread {
          dialog = progressDialog(title = "扫描锁控板")
          dialog?.max = 16
          dialog?.show()
        }
        var found = false
        sdk = LockerSDK(port.getInputStream(), port.getOutputStream())
        for (i in 1..16) {
          dialog?.setMessage("检测${i}号锁控板")
          dialog?.progress = i
          if (sdk?.is_online(i.toByte()) ?: false) {
            uiThread {
              dialog?.cancel()
              toast("发现${i}号锁控板")
            }
            found = true
            board = i.toByte()
            break
          }
        }
        if (!found) {
          sdk = null
          port.closePort()
          uiThread {
            dialog?.cancel()
            find<Spinner>(ViewID.DEVICES.value).setEnabled(true)
            find<TextView>(ViewID.ERROR.value).text = "在 ${port.systemPortName} 上未发现可用的锁控板"
            find<View>(ViewID.ERROR_CONTAINER.value).visibility = View.VISIBLE
            find<GridView>(ViewID.BOXES.value).visibility = View.INVISIBLE
          }
        } else {
          val boxes: IntArray = sdk?.query(board) ?: IntArray(0)
          val adapter = find<GridView>(ViewID.BOXES.value).adapter as BoxAdapter
          for (i in 1..boxes.size) {
            if (i > adapter.getCount()) {
              break;
            }
            val item = adapter.getItem(i - 1)
            item.locked = boxes[i] == 1
          }
          uiThread {
            adapter.notifyDataSetChanged()
            find<Spinner>(ViewID.DEVICES.value).setEnabled(true)
            find<View>(ViewID.ERROR_CONTAINER.value).visibility = View.INVISIBLE
            find<GridView>(ViewID.BOXES.value).visibility = View.VISIBLE
          }
        }
      }
    }
  }
  fun open(lock: Byte) {
    doAsync () {
      sdk?.open(board, lock)
      val boxes: IntArray = sdk?.query(board) ?: IntArray(0)
      val adapter = find<GridView>(ViewID.BOXES.value).adapter as BoxAdapter
      for (i in 1..boxes.size) {
        if (i > adapter.getCount()) {
          break;
        }
        val item = adapter.getItem(i - 1)
        item.locked = boxes[i] == 1
      }
      uiThread {
        adapter.notifyDataSetChanged()
      }
    }
  }
}

class LockActivityUi : AnkoComponent<LockActivity> {
  override fun createView(ui: AnkoContext<LockActivity>) = with(ui) {
    verticalLayout {
      val spinner = spinner {
        id = ViewID.DEVICES.value
        adapter = PortAdapter(mutableListOf<SerialPort>())
      }
      spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
          ui.owner.scan(spinner.adapter.getItem(pos) as SerialPort)
          spinner.setEnabled(false)
        }

        override fun onNothingSelected(parent: AdapterView<out Adapter>?) {
        }
      }
      frameLayout {
        lparams(weight = 1.0f, height = 0)
        relativeLayout {
          id = ViewID.ERROR_CONTAINER.value
          textView("") {
            id = ViewID.ERROR.value
            textSize = 28f
          }.lparams {
            centerInParent()
          }
        }
        val grid = gridView {
          id = ViewID.BOXES.value
          visibility = View.INVISIBLE
          numColumns = 3
          stretchMode = GridView.STRETCH_COLUMN_WIDTH
          adapter = BoxAdapter(listOf(Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box(), Box()))
        }
        grid.onItemClickListener = object: AdapterView.OnItemClickListener {
          override fun onItemClick(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
            val box = grid.adapter.getItem(pos) as Box
            if (box.locked) {
              ui.owner.open((pos + 1).toByte())
            }
          }
        }
        grid.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
          override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
          }

          override fun onNothingSelected(parent: AdapterView<out Adapter>?) {
          }
        }
      }
    }
  }
}

class PortAdapter(val ports: MutableList<SerialPort>) : BaseAdapter() {
  val items: MutableList<SerialPort> = ports
  fun add(port: SerialPort) {
    items.add(port)
  }
  fun addAll(ports: List<SerialPort>) {
    for (port in ports) {
      items.add(port)
    }
  }
  override fun getView(i : Int, v : View?, parent : ViewGroup?) : View {
    val item = getItem(i)
    return with(parent!!.context) {
      verticalLayout {
        padding = dip(10)
        textView(item.systemPortName) {
          textSize = 18f
        }
      }
    }
  }

  override fun getItem(position: Int): SerialPort {
    return items.get(position)
  }

  override fun getCount(): Int {
    return items.size
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }
}

data class Box (var locked: Boolean = false, var empty: Boolean = true)

class BoxAdapter(val boxes: List<Box>) : BaseAdapter() {
  val items: List<Box> = boxes

  override fun getView(i : Int, v : View?, parent : ViewGroup?) : View {
    val item = getItem(i)
    return with(parent!!.context) {
      relativeLayout {
        padding = dip(10)
        imageView(imageResource = if (item.locked) R.drawable.lock_icon else R.drawable.unlock_icon).lparams {
          centerInParent()
        }
        textView("${i + 1}") {
          textSize = 18f
        }.lparams {
          centerInParent()
        }
      }
    }
  }

  override fun getItem(position: Int): Box {
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
