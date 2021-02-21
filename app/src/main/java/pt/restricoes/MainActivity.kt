package pt.restricoes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import pt.restricoes.model.Municipality
import pt.restricoes.model.MunicipalityData
import pt.restricoes.services.Preferences.defaults
import pt.restricoes.services.U
import pt.restricoes.ui.AppActivity
import kotlinx.android.synthetic.main.activity_main.*
import pt.restricoes.ui.Color
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppActivity() {

    private var municipality: Municipality? = null

    private val riskClient = RiskClient()
    private val restrictionClient = RestrictionClient()

    private val refreshCount = AtomicInteger(0)
    private val refreshSuccess = AtomicBoolean(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.main_toolbar))

        this.setMunicipality(this.defaults().getString("municipality", null))

        municipality_selection_input.setText(this.municipality?.name)
        municipality_selection_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                municipality_selection_input.clearFocus()

                val intent = Intent(this, MunicipalitySearchActivity::class.java)
                this.startActivityForResult(intent, 1)
            }
        }

        more_button.setOnClickListener {
            this.showMenu(restrictions_top)
        }

        this.askUpdate(live = false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            val selection = data?.getStringExtra("municipality")

            if (selection != null) {
                this.setMunicipality(selection)

                with (this.defaults().edit()) {
                    putString("municipality", selection)
                    apply()
                }

                municipality_selection_input.setText(selection)
                municipality_selection_input.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                update()
            }
        }
    }

    private fun setMunicipality(municipality: String?) {
        this.municipality = MunicipalityData.get(municipality ?: "")
    }

    private fun askUpdate(live: Boolean) {
        this.update()
        this.riskClient.refresh(this, live) { success ->
            this.onRefreshResult(live, success)
        }
        this.restrictionClient.refresh(this, live) { success ->
            this.onRefreshResult(live, success)
        }
    }

    private fun onRefreshResult(live: Boolean, success: Boolean) {
        val index = this.refreshCount.getAndIncrement()
        val previousSuccess = this.refreshSuccess.getAndSet(success)

        if (live) {
            if (index == 0) {
                return
            }

            if (!success || !previousSuccess) {
                val dialog = AlertDialog
                    .Builder(this)
                    .setMessage("Verifique a sua ligação de Internet. Informação não foi actualizada.")
                    .setPositiveButton(android.R.string.ok, { _, _ -> })
                    .setCancelable(false)
                    .create()

                dialog.show()
                dialog
                    .getButton(AlertDialog.BUTTON_POSITIVE)
                    ?.setTextColor(Color.fromAttr(this, R.attr.colorOnSurface))
            }
        }

        this.update()

        this.refreshCount.set(0)
        this.refreshSuccess.set(true)
    }

    private fun update() {
        val risk = this.riskClient.get(this, this.municipality?.name ?: "")?.risk ?: "Indisponível"

        municipality_risk.text = "Risco $risk"
        val icon =
            when {
                risk.contains("Moderado") -> R.drawable.ic_virus_gray
                risk.contains("Muito") -> R.drawable.ic_virus_orange
                risk.contains("Extremamente") -> R.drawable.ic_virus_red
                risk.contains("Elevado") -> R.drawable.ic_virus_yellow
                else -> R.drawable.ic_virus_gray
            }

        municipality_risk_icon.background = resources.getDrawable(icon)

        val region = this.municipality?.region ?: ""
        val restrictionValues = this.restrictionClient.getValues(this, region, risk, Date())

        restriction_list.adapter = RestrictionAdapter(this, restrictionValues)
        restriction_list.emptyView = restriction_list_empty

        if (this.municipality == null) {
            restriction_list_initial.visibility = View.VISIBLE
            restriction_list_none.visibility = View.GONE
        } else {
            restriction_list_initial.visibility = View.GONE
            restriction_list_none.visibility = View.VISIBLE
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.app_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.restriction_refresh -> this.askUpdate(live = true)
                R.id.restriction_widgets -> this.openWidgets()
                R.id.restriction_contact -> this.sendEmail()
            }
            true
        }

        val menuPopupHelper = MenuPopupHelper(this, popup.menu as MenuBuilder, view)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            menuPopupHelper.setForceShowIcon(true)
        }

        menuPopupHelper.show()
    }

    private fun openWidgets() {
        val intent = Intent(this, WidgetsActivity::class.java)
        this.startActivity(intent)
    }

    private fun sendEmail() {
        val version = U.safe {
            this.packageManager.getPackageInfo("pt.restricoes", 0).versionName
        }
        val title = "Restrições " + ("v$version - " ?: "")

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("apoio@frroliveira.com"))
            putExtra(Intent.EXTRA_SUBJECT, title)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
