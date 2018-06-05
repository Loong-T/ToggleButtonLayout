package com.savvyapps.togglebuttonlayout.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_toggle_button.*

@SuppressLint("ShowToast")
class ToggleButtonLayoutActivity : AppCompatActivity() {

    private val toast by lazy { Toast.makeText(this, "", Toast.LENGTH_SHORT) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toggle_button)

        toggleButtonLayout.toggle(R.id.toggle_left, true)
        toggleButtonLayout.onToggledListener = { toggle, selected ->
            toast.setText("Toggle ${toggle.id} selected state $selected")
            toast.show()
        }

        toggleColorful.toggleAll(true)
    }
}
