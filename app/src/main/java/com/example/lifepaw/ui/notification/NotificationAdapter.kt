package com.example.lifepaw.ui.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.lifepaw.R

class NotificationAdapter(private val context: Context, private val dataSource: List<NotificationItem>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: inflater.inflate(R.layout.notification_item, parent, false)

        val titleTextView = view.findViewById<TextView>(R.id.text_notification_title)
        val dateTextView = view.findViewById<TextView>(R.id.text_notification_date)
        val descriptionTextView = view.findViewById<TextView>(R.id.text_notification_description)

        val notification = getItem(position) as NotificationItem

        titleTextView.text = notification.title
        dateTextView.text = notification.date
        descriptionTextView.text = notification.description

        return view
    }
}
