package com.example.urgetruckkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout

import android.widget.RelativeLayout
import android.widget.ImageView
import android.widget.TextView
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.model.trackVehical.JobMilestone
import com.example.urgetruckkotlin.model.trackVehical.MilestoneActionsTracking

class TrackVehicleRecyclerView(
    private val context: Context,
    private val milestones: List<JobMilestone>
) :
    RecyclerView.Adapter<TrackVehicleRecyclerView.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var list: MutableList<MilestoneActionsTracking> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.track_vehicle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = milestones[position]
        holder.tvMilestone.text = "${model.milestone}-${model.milestioneEvent}"
        holder.tvLocationName.text = model.locationName

        when (model.status) {
            "Pending" -> holder.icStatus.visibility = View.GONE
            "Completed" -> {
                holder.icStatus.visibility = View.VISIBLE
                holder.icStatus.setImageResource(R.drawable.ic_tick_new_green)
            }

            "Open" -> {
                holder.icStatus.visibility = View.VISIBLE
                holder.icStatus.setImageResource(R.drawable.ic_tick_new_orange)
            }
        }

        val isExpandable = model.isExpandable
        holder.expandableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE
        holder.mArrowImage.setImageResource(if (isExpandable) R.drawable.arrow_up else R.drawable.arrow_down)

        val adapter = model.milestoneActionsTracking?.let { TrackVehicleChildAdapter(it, context) }
        holder.nestedRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.nestedRecyclerView.setHasFixedSize(true)
        holder.nestedRecyclerView.adapter = adapter

        holder.linearLayout.setOnClickListener {
            model.isExpandable = (!model.isExpandable)
            list = model.milestoneActionsTracking as MutableList<MilestoneActionsTracking>
            notifyItemChanged(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return milestones.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMilestone: TextView = itemView.findViewById(R.id.tvMilestone)
        val tvLocationName: TextView = itemView.findViewById(R.id.tvLocationName)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linear_layout)
        val expandableLayout: RelativeLayout = itemView.findViewById(R.id.expandable_layout)
        val mArrowImage: ImageView = itemView.findViewById(R.id.arro_imageview)
        val nestedRecyclerView: RecyclerView = itemView.findViewById(R.id.child_rv)
        val icStatus: ImageView = itemView.findViewById(R.id.icStatus)
    }
}