package com.fengchaohuzhu.box

import java.io.File
import java.io.FilenameFilter

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.fazecast.jSerialComm.SerialPort
import org.jetbrains.anko.*
import LockerSDK

enum class ViewID (val value: Int) {
  DEVICES(0x00010001),
}

class LockActivity : Activity(), AnkoLogger {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LockActivityUi().setContentView(this)
    doAsync() {
      val devices: Array<String> = File("/dev/").list { _: File, filename: String -> filename.startsWith("ttyS") || filename.startsWith("ttyGS") || filename.startsWith("ttyUSB") }
      if (devices.size > 0) {
        uiThread {
          val adapter: ArrayAdapter<String> = find<Spinner>(ViewID.DEVICES.value).getAdapter() as ArrayAdapter<String>
          adapter.addAll(devices.toList())
          adapter.notifyDataSetChanged()
        }
      }
    }
  }
  fun scan(device: String) {
    doAsync() {
      val port = SerialPort.getCommPort(device)
      port.setBaudRate(9600)
      port.setNumDataBits(8)
      port.setNumStopBits(1)
      port.setParity(0)
      if (!port.openPort()) {
        uiThread {
          alert("无法打开串口设备 $device", "警告").show()
        }
      } else {
        val dialog = progressDialog(title = "扫描锁控板")
        dialog.max = 16
        uiThread {
          dialog.show()
        }
        val sdk = LockerSDK(port.getInputStream(), port.getOutputStream())
        var found = false
        for (i in 1..16) {
          dialog.setMessage("检测${i}号锁控板")
          dialog.progress = i
          if (sdk.is_online(i.toByte())) {
            uiThread {
              toast("发现${i}号锁控板")
            }
            found = true
            break
          }
        }
        if (!found) {
          port.closePort()
          uiThread {
            alert("在 ${device} 上未发现可用的锁控板", "警告").show()
          }
        }
      }
    }
  }
}

class LockActivityUi : AnkoComponent<LockActivity> {
  override fun createView(ui: AnkoContext<LockActivity>) = with(ui) {
    verticalLayout {
      val spinner = spinner {
        id = ViewID.DEVICES.value
        adapter = ArrayAdapter<String>(ui.owner, android.R.layout.simple_list_item_1, mutableListOf<String>())
      }
      spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
          ui.owner.scan("/dev/${spinner.getAdapter().getItem(pos)}")
        }

        override fun onNothingSelected(parent: AdapterView<out Adapter>?) {
        }
      }
    }
  }
}
