package pt.restricoes

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import pt.restricoes.model.*
import pt.restricoes.services.Preferences.widgetPreferences
import pt.restricoes.services.Time
import pt.restricoes.ui.Bitmap
import pt.restricoes.ui.VectorDrawable
import java.util.*

class RestrictionWidget : AppWidgetProvider() {

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
            val preferences = context.widgetPreferences(appWidgetId)
            val restrictionId = preferences.getString("restriction", null) ?: ""
            val municipality = preferences.getString("municipality", null)

            val riskClient = RiskClient()
            val risk = riskClient.get(context, municipality ?: "")?.risk ?: "-"
            val region = MunicipalityData.get(municipality ?: "")?.region ?: ""

            val restrictionClient = RestrictionClient()
            val restriction = restrictionClient.getRestriction(context, restrictionId)
            val value = restrictionClient.getValue(context, region, risk, Date(), restrictionId)

            val views = RemoteViews(context.packageName, R.layout.restriction_widget)
            views.setTextViewText(
                R.id.restrictionwidget_title,
                restriction?.widget?.title ?: "-"
            )
            views.setTextViewText(
                R.id.restrictionwidget_status,
                this.getStatus(value, municipality) ?: "-"
            )

            if (restriction != null) {
                val color = context.resources.getColor(android.R.color.white)
                val icon = VectorDrawable.fromXmlText(context, restriction.icon, color)
                val bitmap = Bitmap.fromDrawable(icon)
                views.setImageViewBitmap(R.id.restrictionwidget_icon, bitmap)
            }

            restrictionClient.refresh(context, live = false) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
            riskClient.refresh(context, live = false) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getStatus(value: RestrictionValue?, municipalityStr: String?): String? {
            val now = Calendar.getInstance()
            val startOfToday = Time.startOfDay(now)

            val dayOfWeek = Time.dayOfWeek(now)
            val isWeekend = Time.isWeekend(now)

            val municipality = MunicipalityData.get(municipalityStr ?: "")
            val dayPeriods = (value?.periods ?: listOf())
                .filter { p ->
                    p.period == "all" ||
                    (p.period == "week" && !isWeekend) ||
                    (p.period == "weekend" && isWeekend) ||
                    (p.period == dayOfWeek) ||
                    (
                        p.period == "holiday" && (
                        (
                            municipality?.holiday != null &&
                            now.time >= Time.startOfDay(municipality.holiday) &&
                            now.time <= Time.endOfDay(municipality.holiday)
                        ) ||
                            Holiday.all.any { h ->
                                h != null &&
                                now.time >= Time.startOfDay(h) &&
                                now.time <= Time.endOfDay(h)
                            }
                        )
                    )
                }

            val restricted = dayPeriods.any { p -> p.startTime == null && p.endTime == null }

            if (restricted) {
                return value?.restriction?.widget?.restrictedMessage
            }

            var endTime: Date? = null
            for (period in dayPeriods) {
                if (period.endTime == null) {
                    continue
                }

                val todayEndTime = Date(
                    startOfToday.time +
                    period.endTime.hours * Time.HOUR +
                    period.endTime.minutes * Time.MINUTE
                )

                if (endTime == null || todayEndTime > endTime) {
                    endTime = todayEndTime
                }
            }

            if (endTime != null && endTime > now.time) {
                return "Após " + Time.formatHour(endTime)
            }

            var startTime: Date? = null
            for (period in dayPeriods) {
                if (period.startTime == null) {
                    continue
                }

                val todayStartTime = Date(
                    startOfToday.time +
                    period.startTime.hours * Time.HOUR +
                    period.startTime.minutes * Time.MINUTE
                )

                if (startTime == null || todayStartTime < startTime) {
                    startTime = todayStartTime
                }
            }

            if (startTime != null) {
                if (startTime > now.time) {
                    return "Até às " + Time.formatHour(startTime)
                } else {
                    return value?.restriction?.widget?.restrictedMessage
                }
            }

            return value?.restriction?.widget?.permittedMessage
        }
    }
}