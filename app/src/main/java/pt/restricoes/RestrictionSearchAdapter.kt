package pt.restricoes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import pt.restricoes.model.Restriction
import pt.restricoes.ui.Color
import pt.restricoes.ui.VectorDrawable
import kotlinx.android.synthetic.main.restriction_list_item.view.*

class RestrictionSearchAdapter(private val context: Context,
                               private val restrictions: List<Restriction>) : BaseAdapter() {

    private val inflater =
        this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return this.restrictions.size
    }

    override fun getItem(position: Int): Any {
        return this.restrictions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = this.inflater.inflate(R.layout.restriction_search_item, parent, false)

        val titleView = rowView.restriction_title
        val subtitleView = rowView.restriction_subtitle
        val iconView = rowView.restriction_icon

        val restriction = getItem(position) as Restriction

        titleView.text = restriction.title

        if (restriction.subtitle == null) {
            subtitleView.visibility = View.GONE
        } else {
            subtitleView.text = restriction.subtitle
        }

        val iconColor = Color.fromAttr(context, R.attr.colorControlNormal)
        val icon = VectorDrawable.fromXmlText(context, restriction.icon, iconColor)
        iconView.setImageDrawable(icon)

        return rowView
    }
}