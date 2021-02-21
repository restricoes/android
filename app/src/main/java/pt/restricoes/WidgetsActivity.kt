package pt.restricoes

import android.os.Bundle
import pt.restricoes.ui.AppActivity

class WidgetsActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widgets)

        setSupportActionBar(findViewById(R.id.widgets_toolbar))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Widgets"
    }
}