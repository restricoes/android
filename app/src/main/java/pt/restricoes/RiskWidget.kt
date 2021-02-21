package pt.restricoes

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import pt.restricoes.services.Preferences.widgetPreferences

class RiskWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}

    companion object {

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val municipality = context.widgetPreferences(appWidgetId).getString("municipality", null)
            val risk = RiskClient().get(context, municipality ?: "")?.risk ?: "-"

            val textColor =
                when {
                    risk.contains("Moderado") -> android.R.color.white
                    risk.contains("Muito") -> android.R.color.white
                    risk.contains("Extremamente") -> android.R.color.white
                    risk.contains("Elevado") -> android.R.color.white
                    else -> android.R.color.black
                }

            val background =
                when {
                    risk.contains("Moderado") -> R.drawable.rect_gray
                    risk.contains("Muito") -> R.drawable.rect_orange
                    risk.contains("Extremamente") -> R.drawable.rect_red
                    risk.contains("Elevado") -> R.drawable.rect_yellow
                    else -> R.drawable.rect_white
                }

            val views = RemoteViews(context.packageName, R.layout.risk_widget)

            views.setTextViewText(R.id.riskwidget_municipality, municipality ?: "-")
            views.setTextColor(R.id.riskwidget_municipality, context.resources.getColor(textColor))

            views.setTextViewText(R.id.riskwidget_risk, risk)
            views.setTextColor(R.id.riskwidget_risk, context.resources.getColor(textColor))

            views.setInt(R.id.riskwidget_background, "setBackgroundResource", background)

            RiskClient().refresh(context, live = false) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}