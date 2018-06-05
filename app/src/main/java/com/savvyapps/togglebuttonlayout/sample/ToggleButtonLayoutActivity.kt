package com.savvyapps.togglebuttonlayout.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_toggle_button_layout.*

@SuppressLint("ShowToast")
class ToggleButtonLayoutActivity : AppCompatActivity() {

    private val toast by lazy { Toast.makeText(this, "", Toast.LENGTH_SHORT) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toggle_button_layout)

        tblNormal.toggle(R.id.toggle_left, true)
        tblNormal.onToggledListener = { toggle, selected ->
            toast.setText("Toggle ${toggle.id} selected state $selected")
            toast.show()
        }

        tblColorful.toggleAll(true)

        tblCustom.toggle(R.id.toggle_3, true)
        tblCustom.onToggledListener = { toggle, _ ->
            when (toggle.id) {
                R.id.toggle_2 -> tblColorful.inflateMenu(R.menu.toggles_colorful_2)
                R.id.toggle_3 -> tblColorful.inflateMenu(R.menu.toggles_colorful_3)
                R.id.toggle_4 -> tblColorful.inflateMenu(R.menu.toggles_colorful_4)
            }
        }
    }
}
