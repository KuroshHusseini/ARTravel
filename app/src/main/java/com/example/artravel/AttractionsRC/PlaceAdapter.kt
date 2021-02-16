package com.example.artravel.AttractionsRC


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
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
        placeImage.setImageBitmap(item.image)
        placeDesc.text = item.desc


        itemView.setOnClickListener {
            action.onItemClick(item, adapterPosition)
        }

        //adding to favorites
        val addToFavBtn = itemView.findViewById<ImageView>(R.id.add_to_favorites)
        addToFavBtn.setOnClickListener {
            Toast.makeText(itemView.context, item.name, Toast.LENGTH_SHORT).show()
        }

    }
}
