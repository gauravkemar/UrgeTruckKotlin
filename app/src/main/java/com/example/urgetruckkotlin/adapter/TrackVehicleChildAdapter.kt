package com.example.urgetruckkotlin.adapter

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.model.trackVehical.MilestoneActionsTracking

import java.text.SimpleDateFormat
import java.util.*

class TrackVehicleChildAdapter(private val mList: List<MilestoneActionsTracking>, private val context: Context) :
    RecyclerView.Adapter<TrackVehicleChildAdapter.NestedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_child, parent, false)
        return NestedViewHolder(view)
    }

    override fun onBindViewHolder(holder: NestedViewHolder, position: Int) {
        val item = mList[position]
        when (item.status) {
            "Completed" -> holder.ivStatus.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_tick_small_green, context.theme))
            "Open", "ReOpen" -> holder.ivStatus.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_open_small_orange, context.theme))
            "Cancelled" -> holder.ivStatus.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_close_small_red, context.theme))
            "Pending" -> holder.ivStatus.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_pending_small_blue, context.theme))
            "Failed" -> holder.ivStatus.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_failed_small_blue, context.theme))
        }
        holder.tvlabel.text = item.milestoneAction
        holder.tvStatus.text = item.completionTime?.let { formattedDate(it) } ?: "NA"
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class NestedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvlabel: TextView = itemView.findViewById(R.id.label)
        val tvStatus: TextView = itemView.findViewById(R.id.status)
        val ivStatus: ImageView = itemView.findViewById(R.id.iv_status)
    }

    private fun formattedDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        val date: Date = inputFormat.parse(inputDate) ?: Date()

        return outputFormat.format(date)
    }
}
