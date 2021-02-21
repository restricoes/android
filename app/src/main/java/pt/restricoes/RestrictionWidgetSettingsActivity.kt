package pt.restricoes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import pt.restricoes.services.Preferences.defaults
import pt.restricoes.services.Preferences.widgetPreferences
import pt.restricoes.ui.AppActivity
import kotlinx.android.synthetic.main.activity_restriction_widget_settings.*

class RestrictionWidgetSettingsActivity : AppActivity() {

    private var restriction: String? = null
    private var municipality: String? = null
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restriction_widget_settings)

        this.appWidgetId = this.intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        this.municipality = this.defaults().getString("municipality", null)

        restriction_widget_pref_municipality_input.setText(municipality)
        restriction_widget_pref_municipality_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                restriction_widget_pref_municipality_input.clearFocus()

                val intent = Intent(this, MunicipalitySearchActivity::class.java)
                this.startActivityForResult(intent, 1)
            }
        }

        restriction_widget_pref_restriction_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                restriction_widget_pref_restriction_input.clearFocus()

                val intent = Intent(this, RestrictionSearchActivity::class.java)
                this.startActivityForResult(intent, 2)
            }
        }

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        restriction_widget_pref_create_button.isEnabled = this.creationReady()
        restriction_widget_pref_create_button.setOnClickListener {
            with (this.widgetPreferences(appWidgetId).edit()) {
                putString("restriction", restriction)
                putString("municipality", municipality)
                apply()
            }

            RestrictionWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), appWidgetId)

            setResult(RESULT_OK, resultValue)
            finish()
        }

        setResult(RESULT_CANCELED, resultValue)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            1 -> {
                this.municipality = data?.getStringExtra("municipality") ?: return
                restriction_widget_pref_municipality_input.setText(this.municipality)
            }
            2 -> {
                this.restriction = data?.getStringExtra("restriction") ?: return
                val title = data?.getStringExtra("restrictionTitle")
                restriction_widget_pref_restriction_input.setText(title)
            }
            else -> {
                return
            }
        }

        restriction_widget_pref_create_button.isEnabled = this.creationReady()
    }

    private fun creationReady(): Boolean {
        return this.municipality != null && this.restriction != null
    }
}