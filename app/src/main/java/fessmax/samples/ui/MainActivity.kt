package fessmax.samples.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import fessmax.samples.textinputlayoutcustom.TextInputLayoutCustom
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        check_email.setOnClickListener { tilc_email.setError("Error email") }

        check_pass.setOnClickListener { tilc_pass.setError("Error pass") }
    }
}
