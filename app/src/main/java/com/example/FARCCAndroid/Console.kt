package com.example.FARCCAndroid

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class Console : ComponentActivity(), FARUIUpdater {
    public var parentActivity = null
    override var updatingFromData: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        currentUI = this
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemUI(this.window)
        setContentView(R.layout.console)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.harmonicEditMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ListView>(R.id.listviewConsole).adapter = logAdapter

        val command = findViewById<TextInputEditText>(R.id.texteditConsoleCommand)

        command.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean): Unit {
                if (!hasFocus) communicator!!.writeUSB(command.text.toString())
            }
        }

        command.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_ACTION_NEXT ||
                    event != null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                        sendCommandToUsb(command.text.toString())
                        command.setText("")
                        return true;  // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        })

    }

    override fun updateUI() {

    }

    override fun updateFAR() {

    }
}