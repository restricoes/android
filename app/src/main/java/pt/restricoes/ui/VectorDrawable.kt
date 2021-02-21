package pt.restricoes.ui

import android.content.Context
import android.graphics.drawable.Drawable

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

object VectorDrawable {

    fun fromXmlText(context: Context,
                    xml: String,
                    color: Int): Drawable {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(xml.byteInputStream(), null)
        parser.nextTag()

        parser.require(XmlPullParser.START_TAG, null, "vector")
        val width = parser.getAttributeValue(null, "android:width").takeWhile { c -> c.isDigit() }.toInt()
        val height = parser.getAttributeValue(null, "android:height").takeWhile { c -> c.isDigit() }.toInt()
        val viewportWidth = parser.getAttributeValue(null, "android:viewportWidth").toFloat()
        val viewportHeight = parser.getAttributeValue(null, "android:viewportHeight").toFloat()

        val paths = mutableListOf<VectorDrawableCreator.PathData>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            parser.require(XmlPullParser.START_TAG, null, "path")
            val path = parser.getAttributeValue(null, "android:pathData")
            parser.nextTag()

            paths.add(VectorDrawableCreator.PathData(path, color))
        }

        return VectorDrawableCreator.getVectorDrawable(context, width, height, viewportWidth, viewportHeight, paths)
    }
}