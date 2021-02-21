package pt.restricoes

import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import pt.restricoes.model.RestrictionSection
import pt.restricoes.model.RestrictionValue
import pt.restricoes.ui.Color
import pt.restricoes.ui.VectorDrawable
import kotlinx.android.synthetic.main.restriction_list_item.view.*
import kotlinx.android.synthetic.main.restriction_list_item_section.view.*
import kotlinx.android.synthetic.main.restriction_list_item_section_item.view.*

class RestrictionAdapter(private val context: Context,
                         private val values: List<RestrictionValue>) : BaseAdapter() {

    private val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return if (values.isEmpty()) 0 else (values.size + 1)
    }

    override fun getItem(position: Int): Any {
        return if (position == 0) "" else this.values[position - 1]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (position == 0) {
            return inflater.inflate(R.layout.restriction_list_title, parent, false)
        }

        val rowView = inflater.inflate(R.layout.restriction_list_item, parent, false)

        val titleView = rowView.restriction_title
        val subtitleView = rowView.restriction_subtitle
        val iconView = rowView.restriction_icon

        val value = getItem(position) as RestrictionValue

        if (value.restriction == null) {
            return rowView
        }

        titleView.text = value.restriction.title

        if (value.restriction.subtitle == null) {
            subtitleView.visibility = View.GONE
        } else {
            subtitleView.text = value.restriction.subtitle
        }

        val iconColor = Color.fromAttr(context, R.attr.colorControlNormal)
        val icon = VectorDrawable.fromXmlText(context, value.restriction.icon, iconColor)
        iconView.setImageDrawable(icon)

        for (i in 0..1) {
            if (i >= value.sections.size) {
                break
            }

            this.addSection(value.sections[i], rowView.restriction_sections)
        }

        for (i in 2 until value.sections.size) {
            this.addSection(value.sections[i], rowView.restriction_detail_sections)
        }

        rowView.restriction_detail_bar.setOnClickListener {
            rowView.restriction_detail_checkbox.toggle()

            if (rowView.restriction_detail_checkbox.isChecked) {
                rowView.restriction_detail_sections.visibility = View.VISIBLE
            } else {
                rowView.restriction_detail_sections.visibility = View.GONE
            }
        }

        rowView.restriction_detail_checkbox.setOnClickListener {
            if (rowView.restriction_detail_checkbox.isChecked) {
                rowView.restriction_detail_sections.visibility = View.VISIBLE
            } else {
                rowView.restriction_detail_sections.visibility = View.GONE
            }
        }

        return rowView
    }

    private fun addSection(section: RestrictionSection, parent: ViewGroup) {
        val sectionView = inflater.inflate(R.layout.restriction_list_item_section, parent, false)
        sectionView.restriction_section_name.text = section.name

        val itemsView = sectionView.restriction_section_items
        for (item in section.items) {
            val itemView = inflater.inflate(R.layout.restriction_list_item_section_item, itemsView, false)

            if (item.link == null) {
                itemView.restriction_section_item_value.text = item.value
            } else {
                itemView.restriction_section_item_value.text = Html.fromHtml("""<a href="${item.link}">${item.value}</a>""")
                itemView.restriction_section_item_value.movementMethod = LinkMovementMethod.getInstance()
            }

            itemsView.addView(itemView)
        }

        parent.addView(sectionView)
    }
}