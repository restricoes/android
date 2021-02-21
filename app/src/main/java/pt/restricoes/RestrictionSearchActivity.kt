package pt.restricoes

import android.content.Intent
import android.os.Bundle
import pt.restricoes.model.Restriction
import pt.restricoes.ui.AppActivity
import kotlinx.android.synthetic.main.activity_restriction_search.*

class RestrictionSearchActivity : AppActivity() {

    private val restrictionClient = RestrictionClient()
    private lateinit var adapter: RestrictionSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restriction_search)

        setSupportActionBar(restriction_search_toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Selecionar restrição"

        val restrictions = this.restrictionClient.getRestrictions(this).filter { r -> r.widget != null }
        this.adapter = RestrictionSearchAdapter(this, restrictions)

        restriction_search_list.adapter = this.adapter
        restriction_search_list.setOnItemClickListener { _, _, position, _ ->
            val restriction = adapter.getItem(position) as Restriction

            val intent = Intent()
            intent.putExtra("restriction", restriction.id)
            intent.putExtra("restrictionTitle", restriction.title)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}