package pt.restricoes

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import pt.restricoes.services.Preferences.defaults
import pt.restricoes.ui.AppActivity
import kotlinx.android.synthetic.main.activity_consent.*

class ConsentActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (this.defaults().getBoolean("consented", false)) {
            this.mainActivity()
            return
        }

        setContentView(R.layout.activity_consent)

        val policy = "https://restricoes.pt/privacy/v1";
        val html = """<a href="$policy">saber mais</a>"""
        consent_more.text = Html.fromHtml(html)
        consent_more.movementMethod = LinkMovementMethod.getInstance()

        consent_accept.setOnClickListener {
            with (this.defaults().edit()) {
                putBoolean("consented", true)
                apply()
            }

            this.mainActivity()
        }
    }

    private fun mainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
        this.finish()
    }
}