package com.fengchaohuzhu.box.tester

import java.io.File
import java.io.FilenameFilter
import java.util.Comparator
import java.util.PriorityQueue
import java.util.Queue
import java.util.Timer

import android.app.ActionBar
import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
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
import kotlin.concurrent.timer
import org.jetbrains.anko.*

import LockerSDK

enum class LockViewID (val value: Int) {
  DEVICES(0x00010001),
  BOXES(0x00010002),
  ERROR_CONTAINER(0x00010003),
  ERROR(0x00010004),
}

class LockActivity : Activity(), AnkoLogger {
  var board: Byte = 0
  var worker: Worker? = null
  var locktimer: Timer? = null
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
        val spinner = find<Spinner>(LockViewID.DEVICES.value)
        val adapter: PortAdapter = spinner.adapter as PortAdapter
        adapter.addAll(ports)
        spinner.setSelection(1)
        adapter.notifyDataSetChanged()
      }
    }
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

  fun scan(port: SerialPort) {
    doAsync() {
      uiThread {
        find<Spinner>(LockViewID.DEVICES.value).setEnabled(false)
        find<TextView>(LockViewID.ERROR.value).text = ""
        find<View>(LockViewID.ERROR_CONTAINER.value).visibility = View.INVISIBLE
        find<GridView>(LockViewID.BOXES.value).visibility = View.INVISIBLE
      }
      worker?.running = false
      locktimer?.cancel()
      port.setBaudRate(9600)
      port.setNumDataBits(8)
      port.setNumStopBits(1)
      port.setParity(0)
      if (!port.openPort()) {
        uiThread {
          find<Spinner>(LockViewID.DEVICES.value).setEnabled(true)
          find<TextView>(LockViewID.ERROR.value).text = "无法打开串口设备 ${port.systemPortName}"
          find<View>(LockViewID.ERROR_CONTAINER.value).visibility = View.VISIBLE
          find<GridView>(LockViewID.BOXES.value).visibility = View.INVISIBLE
        }
      } else {
        var dialog: ProgressDialog? = null
        uiThread {
          dialog = progressDialog(title = "扫描锁控板", message = "")
          dialog?.max = 16
          dialog?.show()
        }
        worker = Worker(port)
        worker?.start()
        worker?.queue?.add(LockCommand(type = LockCommandType.SCAN) { dat: Any? ->
          val pair: Pair<Byte, Boolean> = dat as Pair<Byte, Boolean>
          if (pair.second) {
            board = pair.first
            uiThread {
              dialog?.cancel()
              toast("发现${pair.first}号锁控板")
            }
            refreshLockGridUI(IntArray(0))
            locktimer = timer(initialDelay = 0L, period = 1000L) {
              worker?.queue?.add(LockCommand(type = LockCommandType.QUERY, board = board) { dat1: Any? ->
                refreshLockGridUI((dat1 ?: IntArray(0)) as IntArray)
              })
            }
          } else {
           if (pair.first == 0.toByte()) {
              port.closePort()
              uiThread {
                dialog?.cancel()
                find<Spinner>(LockViewID.DEVICES.value).setEnabled(true)
                find<TextView>(LockViewID.ERROR.value).text = "在 ${port.systemPortName} 上未发现可用的锁控板"
                find<View>(LockViewID.ERROR_CONTAINER.value).visibility = View.VISIBLE
                find<GridView>(LockViewID.BOXES.value).visibility = View.INVISIBLE
              }
            } else {
              uiThread {
                dialog?.setMessage("检测${pair.first}号锁控板")
                dialog?.progress = pair.first.toInt()
              }
            }
          }
        })
      }
    }
  }

  fun open(lock: Byte) {
    worker?.queue?.add(LockCommand(type = LockCommandType.OPEN, board = board, lock = lock) { _: Any? ->
      worker?.queue?.add(LockCommand(type = LockCommandType.QUERY, board = board) { dat: Any? ->
        refreshLockGridUI((dat ?: IntArray(0)) as IntArray)
      })
    })
  }

  fun refreshLockGridUI(boxes: IntArray) {
    doAsync () {
      val adapter = find<GridView>(LockViewID.BOXES.value).adapter as BoxAdapter
      for (i in 1..boxes.size) {
        if (i > adapter.getCount()) {
          break;
        }
        val item = adapter.getItem(i - 1)
        item.locked = boxes[i] == 1
      }
      uiThread {
        adapter.notifyDataSetChanged()
        find<Spinner>(LockViewID.DEVICES.value).setEnabled(true)
        find<View>(LockViewID.ERROR_CONTAINER.value).visibility = View.INVISIBLE
        find<GridView>(LockViewID.BOXES.value).visibility = View.VISIBLE
      }
    }
  }
}

class LockActivityUi : AnkoComponent<LockActivity> {
  override fun createView(ui: AnkoContext<LockActivity>) = with(ui) {
    verticalLayout {
      val spinner = spinner {
        id = LockViewID.DEVICES.value
        adapter = PortAdapter(mutableListOf<SerialPort>())
      }
      spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
          ui.owner.scan(spinner.adapter.getItem(pos) as SerialPort)
        }

        override fun onNothingSelected(parent: AdapterView<out Adapter>?) {
        }
      }
      frameLayout {
        lparams(weight = 1.0f, height = 0)
        relativeLayout {
          id = LockViewID.ERROR_CONTAINER.value
          textView("") {
            id = LockViewID.ERROR.value
            textSize = 28f
          }.lparams {
            centerInParent()
          }
        }
        val grid = gridView {
          id = LockViewID.BOXES.value
          visibility = View.INVISIBLE
          numColumns = 3
          stretchMode = GridView.STRETCH_COLUMN_WIDTH
          verticalSpacing = dip(10)
          horizontalSpacing = dip(10)
          adapter = BoxAdapter(listOf(Box(id = 1), Box(id = 2), Box(id = 3), Box(id = 4), Box(id = 5), Box(id = 6), Box(id = 7), Box(id = 8), Box(id = 9), Box(id = 10), Box(id = 11), Box(id = 12), Box(id = 13), Box(id = 14), Box(id = 15), Box(id = 16), Box(id = 17), Box(id = 18)))
        }
        grid.onItemClickListener = object: AdapterView.OnItemClickListener {
          override fun onItemClick(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
            val box = grid.adapter.getItem(pos) as Box
            if (box.locked) {
              ui.owner.open((box.id).toByte())
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

data class Box (var id: Int, var locked: Boolean = false, var empty: Boolean = true)

class BoxAdapter(val boxes: List<Box>) : BaseAdapter() {
  val items: List<Box> = boxes

  override fun getView(i : Int, v : View?, parent : ViewGroup?) : View {
    val item = getItem(i)
    return with(parent!!.context) {
      verticalLayout {
        padding = dip(10)
        textView("${item.id}") {
          textSize = 28f
        }
        imageView(imageResource = if (item.locked) R.drawable.lock_icon else R.drawable.unlock_icon)
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

enum class LockCommandType(val value: Int) {
  CHECK(3),
  OPEN(1),
  QUERY(3),
  SCAN(7)
}

data class LockCommand (val type: LockCommandType = LockCommandType.QUERY, val board: Byte = 1, val lock: Byte = 1, val callback: (result: Any?) -> Unit)

class Worker(val port: SerialPort): Thread() {
  val queue: Queue<LockCommand> = PriorityQueue<LockCommand>(11, object: Comparator<LockCommand> {
    override fun compare(a: LockCommand, b: LockCommand): Int {
      if (a.type.value < b.type.value) {
        return -1
      } else if (a.type.value > b.type.value) {
        return 1
      } else {
        return 0
      }
    }
  })
  var running: Boolean = true

  override fun run() {
    val sdk: LockerSDK = LockerSDK(port.getInputStream(), port.getOutputStream())
    while (running && port.isOpen()) {
      var cmd: LockCommand? = queue.poll()
      when (cmd?.type) {
        LockCommandType.QUERY -> cmd.callback(sdk.query(cmd.board))
        LockCommandType.CHECK -> cmd.callback(sdk.check(cmd.board))
        LockCommandType.OPEN -> cmd.callback(sdk.open(cmd.board, cmd.lock))
        LockCommandType.SCAN -> {
          var found = false
          for (i in 1..16) {
            cmd.callback(Pair<Byte, Boolean>(i.toByte(), false)) // just publish progress
            if (sdk.is_online(i.toByte())) {
              cmd.callback(Pair<Byte, Boolean>(i.toByte(), true))
              found = true
              break
            }
          }
          if (!found) {
            cmd.callback(Pair<Byte, Boolean>(0.toByte(), false))
          }
        }
        null -> {
          try {
            Thread.sleep(500)
          } finally {
          }
        }
      }
    }
    if (port.isOpen()) {
      port.closePort()
    }
  }
}
