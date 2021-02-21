package pt.restricoes

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import pt.restricoes.model.*
import pt.restricoes.model.RestrictionWidget
import pt.restricoes.services.Preferences.defaults
import pt.restricoes.services.Time
import pt.restricoes.services.U
import org.json.JSONObject
import java.util.Date

class RestrictionClient {

    private var restrictions: List<Restriction> = listOf()
    private var mappings: List<RestrictionMapping> = listOf()
    private var sets: List<RestrictionSet> = listOf()

    fun getValues(context: Context, region: String, risk: String, date: Date): List<RestrictionValue> {
        this.getRestrictions(context)

        val regionMapping = this.mappings.find { m -> m.risk == risk && region == m.region }
        val mapping = regionMapping ?: this.mappings.find { m -> m.risk == risk }

        val set = this.sets.find { s -> s.id == mapping?.restrictionSet }
        val potentialValues = set?.values ?: listOf()

        return potentialValues
            .filter { v -> date >= v.startDate && date <= v.endDate }
            .map { v -> v.copy(restriction = this.restrictions.find { r -> r.id == v.id }) }
    }

    fun getRestriction(context: Context, restrictionId : String): Restriction? {
        this.getRestrictions(context)

        return this.restrictions.find { r -> r.id == restrictionId }
    }

    fun getValue(context: Context,
                 region: String,
                 risk: String,
                 date: Date,
                 restrictionId: String): RestrictionValue? {
        return this
            .getValues(context, region, risk, date)
            .find { v -> v.id == restrictionId }
    }

    fun getRestrictions(context: Context): List<Restriction> {
        if (this.sets.isEmpty()) {
            val cache = context.defaults().getString("restriction_json", null)

            if (cache != null) this.update(JSONObject(cache))
        }

        return restrictions
    }

    fun refresh(context: Context, live: Boolean, callback: (Boolean) -> Unit) {
        val now = System.currentTimeMillis()

        val cache = context.defaults().getString("restriction_json", null)
        val last = context.defaults().getLong("restriction_json_date", 0)

        if (!live && cache != null && (now - last) < (12 * Time.HOUR)) {
            update(JSONObject(cache))
            return
        }

        val queue = Volley.newRequestQueue(context)
        val url = "https://restricoes.pt/backend/v1_mini.json"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null, { response ->
                with(context.defaults().edit()) {
                    putString("restriction_json", response.toString())
                    putLong("restriction_json_date", now)
                    apply()
                }

                update(response)
                callback(true)
            }, {
                callback(false)
            }
        )
        request.setShouldCache(!live)

        queue.add(request)
    }

    private fun update(response: JSONObject) {
        this.restrictions = this.parseRestrictions(response)
        this.mappings = this.parseRestrictionMappings(response)
        this.sets = this.parseRestrictionSets(response)
    }

    private fun parseRestrictions(response: JSONObject): List<Restriction> {
        val restrictions = U.safe { response.getJSONArray("restrictions") }

        var i = 0
        val result = mutableListOf<Restriction>()

        while (i < restrictions?.length() ?: 0) {
            val obj = U.safe { restrictions?.getJSONObject(i) }

            val restriction = Restriction(
                U.safe { obj?.getString("id") } ?: "",
                U.safe { obj?.getString("title") } ?: "",
                U.safe { obj?.getString("subtitle") },
                U.safe { obj?.getString("icon") } ?: "",
                this.parseRestrictionWidget(obj ?: JSONObject())
            )

            result.add(restriction)

            i++
        }

        return result
    }

    private fun parseRestrictionWidget(response: JSONObject): RestrictionWidget? {
        val obj = U.safe { response.getJSONObject("widget") } ?: return null

        val restricted = U.safe { obj.getJSONObject("restricted") }
        val permitted = U.safe { obj.getJSONObject("permitted") }

        return RestrictionWidget(
            U.safe { obj.getString("title") } ?: "",
            U.safe { restricted?.getString("message") } ?: "",
            U.safe { permitted?.getString("message") } ?: ""
        )
    }

    private fun parseRestrictionMappings(response: JSONObject): List<RestrictionMapping> {
        val mappings = U.safe { response.getJSONArray("restriction_mappings") }

        var i = 0
        val result = mutableListOf<RestrictionMapping>()

        while (i < mappings?.length() ?: 0) {
            val obj = U.safe { mappings?.getJSONObject(i) }

            val mapping = RestrictionMapping(
                U.safe { obj?.getString("risk") } ?: "",
                U.safe { obj?.getString("region") },
                U.safe { obj?.getString("restriction_set") } ?: ""
            )

            result.add(mapping)

            i++
        }

        return result
    }

    private fun parseRestrictionSets(response: JSONObject): List<RestrictionSet> {
        val sets = U.safe { response.getJSONArray("restriction_sets") }

        var i = 0
        val result = mutableListOf<RestrictionSet>()

        while (i < sets?.length() ?: 0) {
            val obj = U.safe { sets?.getJSONObject(i) }

            val set = RestrictionSet(
                U.safe { obj?.getString("id") } ?: "",
                this.parseRestrictionValues(obj ?: JSONObject())
            )

            result.add(set)

            i++
        }

        return result
    }

    private fun parseRestrictionValues(response: JSONObject): List<RestrictionValue> {
        val values = U.safe { response.getJSONArray("restrictions") }

        var i = 0
        val result = mutableListOf<RestrictionValue>()

        while (i < values?.length() ?: 0) {
            val obj = U.safe { values?.getJSONObject(i) }

            val startDate = U.safe { Time.parseDate(obj?.getString("start_date")) }
            val endDate = U.safe { Time.parseDate(obj?.getString("end_date")) }

            if (startDate == null || endDate == null) {
                continue
            }

            val value = RestrictionValue(
                U.safe { obj?.getString("id") } ?: "",
                null,
                startDate,
                Date(endDate.time + 86399000L),
                this.parseRestrictionPeriods(obj ?: JSONObject()),
                this.parseRestrictionSections(obj ?: JSONObject())
            )

            result.add(value)

            i++
        }

        return result
    }

    private fun parseRestrictionPeriods(response: JSONObject): List<RestrictionPeriod> {
        val periods = U.safe { response.getJSONArray("periods") }

        var i = 0
        val result = mutableListOf<RestrictionPeriod>()

        while (i < periods?.length() ?: 0) {
            val obj = U.safe { periods?.getJSONObject(i) }

            val startTime = U.safe { Time.parseHour(obj?.getString("start_time")) }
            val endTime = U.safe { Time.parseHour(obj?.getString("end_time")) }

            val period = RestrictionPeriod(
                U.safe { obj?.getString("period") } ?: "",
                startTime,
                endTime
            )

            result.add(period)

            i++
        }

        return result
    }

    private fun parseRestrictionSections(response: JSONObject): List<RestrictionSection> {
        val sections = U.safe { response.getJSONArray("sections") }

        var i = 0
        val result = mutableListOf<RestrictionSection>()

        while (i < sections?.length() ?: 0) {
            val obj = U.safe { sections?.getJSONObject(i) }

            val section = RestrictionSection(
                U.safe { obj?.getString("id") } ?: "",
                U.safe { obj?.getString("name") } ?: "",
                this.parseRestrictionItems(obj ?: JSONObject())
            )

            result.add(section)

            i++
        }

        return result
    }

    private fun parseRestrictionItems(response: JSONObject): List<RestrictionItem> {
        val items = U.safe { response.getJSONArray("items") }

        var i = 0
        val result = mutableListOf<RestrictionItem>()

        while (i < items?.length() ?: 0) {
            val obj = U.safe { items?.getJSONObject(i) }

            val item = RestrictionItem(
                U.safe { obj?.getString("value") } ?: "",
                U.safe { obj?.getString("link") }
            )

            result.add(item)

            i++
        }

        return result
    }
}