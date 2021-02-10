package com.example.artravel.AttractionsRC

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artravel.R
import kotlinx.android.synthetic.main.attraction_item.view.*

class PlaceAdapter(var items: ArrayList<Place>, var clickListener: OnPlaceItemClickListener) :
    RecyclerView.Adapter<PlaceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.attraction_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        /* holder.placeName.text = items[position].name
         holder.placeImage.setImageResource(items[position].image)
         holder.placeDesc.text = items[position].desc
     */
        holder.initialize(items[position], clickListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val placeName = itemView.tv_place_name
    val placeImage = itemView.place_image
    val placeDesc = itemView.tv_place_desc

    fun initialize(item: Place, action: OnPlaceItemClickListener) {
        placeName.text = item.name
        placeImage.setImageResource(item.image)
        placeDesc.text = item.desc

        itemView.setOnClickListener {
            action.onItemClick(item, adapterPosition)
        }
    }
}
