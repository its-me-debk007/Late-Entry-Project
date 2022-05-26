package `in`.silive.lateentryproject.adapters

import `in`.silive.lateentryproject.R
import `in`.silive.lateentryproject.models.MessageDataClass
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast


class VenueItemsAdapter (private val context:Activity,private val arraylist:ArrayList<String>):ArrayAdapter<String>(context,
    R.layout.layout_venue_items,arraylist)  {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater:LayoutInflater=LayoutInflater.from(context)
        val view:View=inflater.inflate(R.layout.layout_venue_items, null)
        val venue:TextView=view.findViewById(R.id.venueData)
        venue.text = arraylist[position]
        return view
    }
    }