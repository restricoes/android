package pt.restricoes

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import pt.restricoes.services.Preferences.defaults
import pt.restricoes.model.Risk
import pt.restricoes.services.Time
import pt.restricoes.services.U
import org.json.JSONObject

class RiskClient {

    private var values: Map<String, Risk> = mapOf()

    fun get(context: Context, municipality: String): Risk? {
        if (values.isEmpty()) {
            val cache = context.defaults().getString("risk_json", null)

            if (cache != null) this.update(JSONObject(cache))
        }

        val match = municipality.toLowerCase()
        val risk = values[match]

        if (risk != null) {
            return risk
        }

        if (match.contains("lagoa")) {
            return values["lagoa [r.a. açores]"]
        }
        if (match.contains("madeira")) {
            return values["calheta [r.a. madeira]"]
        }
        if (match.contains("calheta")) {
            return values["calheta [r.a. açores]"]
        }

        return null
    }

    fun refresh(context: Context, live: Boolean,  callback: (Boolean) -> Unit) {
        val now = System.currentTimeMillis()

        val cache = context.defaults().getString("risk_json", null)
        val last = context.defaults().getLong("risk_json_date", 0)

        if (!live && cache != null && (now - last) < (12 * Time.HOUR)) {
            if (values.isNotEmpty()) {
                return
            }

            update(JSONObject(cache))
            return
        }

        val queue = Volley.newRequestQueue(context)
        val url = "https://services.arcgis.com/CCZiGSEQbAxxFVh3/arcgis/rest/services/IncidenciaCOVIDporConc100k_view/FeatureServer/0/query?f=json&where=1%3D1&returnGeometry=false&spatialRel=esriSpatialRelIntersects&outFields=*&orderByFields=Valorincid%20desc&outSR=102100&resultOffset=0&resultRecordCount=310&resultType=standard&cacheHint=true"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null, { response ->
                with(context.defaults().edit()) {
                    putString("risk_json", response.toString())
                    putLong("risk_json_date", now)
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
        val riskList = parseRisk(response)

        values = riskList
            .map { risk ->
                risk.municipality.toLowerCase() to risk
            }
            .toMap()
    }

    private fun parseRisk(response: JSONObject): List<Risk> {
        val features = U.safe { response.getJSONArray("features") }

        var i = 0
        val result = mutableListOf<Risk>()

        while (i < features?.length() ?: 0) {
            val feature = U.safe { features?.getJSONObject(i)?.getJSONObject("attributes") }

            val risk = Risk(
                U.safe { feature?.getString("Concelho") } ?: "",
                U.safe { feature?.getInt("Total") } ?: -1,
                U.safe { feature?.getInt("Incidência") } ?: -1,
                U.safe { feature?.getString("Incidência_") } ?: ""
            )

            if (risk.municipality.isNotEmpty() && risk.risk.isNotEmpty()) {
                result.add(risk)
            }

            i++
        }

        return result
    }
}