package `in`.silive.lateentryproject.adapters

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.databinding.LayoutVenueItemsBinding
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class VenueRecyclerAdapter(private val venueMap: Map<Int,String>, private val selectedVenue:
String, private val listener: VenueClickListenerInterface) :
	RecyclerView.Adapter<VenueRecyclerAdapter.ViewHolder>() {

	private lateinit var selectedVenueBtn: MaterialButton

	inner class ViewHolder(val binding: LayoutVenueItemsBinding) :
		RecyclerView.ViewHolder(binding.root)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
		(LayoutVenueItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)))

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.binding.apply {
			venue.text = venueMap.values.toTypedArray()[position]
			if (venue.text == selectedVenue) {
				selectedVenueBtn = venue
				highlight(selectedVenueBtn, true)
			}

			venue.setOnClickListener {
				listener.venueClickListener(venue.text.toString(),venueMap.keys.toTypedArray()[position])
				highlight(selectedVenueBtn, false)
				selectedVenueBtn = venue
				highlight(venue, true)

			}
		}
	}

	override fun getItemCount(): Int {
		return venueMap.size
	}

	private fun highlight(btn: MaterialButton, bool: Boolean) {
		if (bool) {
			btn.setBackgroundColor(Color.parseColor("#1A73E8"))
			btn.strokeWidth = 0
			btn.setTextColor(Color.parseColor("#FFFFFF"))
		} else {
			btn.setBackgroundColor(Color.parseColor("#FFFFFF"))
			btn.strokeWidth = 1
			btn.setTextColor(Color.parseColor("#242E42"))
		}
	}
}

interface VenueClickListenerInterface{
	fun venueClickListener(venue: String,id:Int)
}