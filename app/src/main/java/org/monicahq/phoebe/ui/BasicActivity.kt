package org.monicahq.phoebe.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.monicahq.phoebe.R

class BasicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)
        /*
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

         */
    }
}
