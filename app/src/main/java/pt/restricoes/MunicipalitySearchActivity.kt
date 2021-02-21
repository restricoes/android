package pt.restricoes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import pt.restricoes.model.MunicipalityData
import pt.restricoes.services.Search.search
import pt.restricoes.ui.AppActivity
import kotlinx.android.synthetic.main.activity_municipality_search.*

class MunicipalitySearchActivity : AppActivity() {

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_municipality_search)

        setSupportActionBar(municipality_search_toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val municipalities = MunicipalityData.all.map { m -> m.name }

        this.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            municipalities
        )

        municipality_search_list.adapter = this.adapter

        municipality_search_input.requestFocus()
        municipality_search_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(edit: Editable?) {
                val search = edit?.toString() ?: ""

                val municipalities = MunicipalityData
                    .all
                    .search(search) { m -> m.name }
                    .map { m -> m.name }

                adapter.clear()
                adapter.addAll(municipalities)
                adapter.notifyDataSetChanged()
            }
        })

        municipality_search_input_clear.setOnClickListener {
            municipality_search_input.text.clear()
        }

        municipality_search_list.setOnItemClickListener { _, _, position, _ ->
            val municipality = adapter.getItem(position)

            val intent = Intent()
            intent.putExtra("municipality", municipality)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}