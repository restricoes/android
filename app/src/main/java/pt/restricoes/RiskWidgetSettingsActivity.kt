package pt.restricoes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import pt.restricoes.services.Preferences.defaults
import pt.restricoes.services.Preferences.widgetPreferences
import pt.restricoes.ui.AppActivity
import kotlinx.android.synthetic.main.activity_risk_widget_settings.*

class RiskWidgetSettingsActivity : AppActivity() {

    private var municipality: String? = null
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk_widget_settings)

        this.appWidgetId = this.intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        this.municipality = this.defaults().getString("municipality", null)

        risk_widget_pref_municipality_input.setText(municipality)
        risk_widget_pref_municipality_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                risk_widget_pref_municipality_input.clearFocus()

                val intent = Intent(this, MunicipalitySearchActivity::class.java)
                this.startActivityForResult(intent, 1)
            }
        }

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        risk_widget_pref_create_button.isEnabled = municipality != null
        risk_widget_pref_create_button.setOnClickListener {
            with (this.widgetPreferences(appWidgetId).edit()) {
                putString("municipality", municipality)
                apply()
            }

            RiskWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), appWidgetId)

            setResult(RESULT_OK, resultValue)
            finish()
        }

        setResult(RESULT_CANCELED, resultValue)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != 1) {
            return
        }

        this.municipality = data?.getStringExtra("municipality") ?: return

        risk_widget_pref_create_button.isEnabled = true
        risk_widget_pref_municipality_input.setText(this.municipality)
    }
}
