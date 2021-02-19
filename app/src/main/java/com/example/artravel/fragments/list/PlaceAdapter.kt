package com.example.artravel.fragments.list


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.artravel.AttractionsRC.OnPlaceItemClickListener
import com.example.artravel.model.database.ARTravelDatabase
import com.example.artravel.R
import com.example.artravel.model.entity.DBAttraction
import com.example.artravel.model.entity.DBPlace
import kotlinx.android.synthetic.main.attraction_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PlaceAdapter(
    var context: Context,
    var items: List<DBAttraction>,
    var clickListener: OnPlaceItemClickListener
) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    /*
    *  Favourites Database
    * */

    private val favouritesDatabase by lazy { ARTravelDatabase.getDatabase(context) }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.attraction_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
//        holder.initialize(items[position], clickListener)

        holder.itemView.tv_place_desc.text = items[position].desc
        holder.itemView.tv_place_name.text = items[position].name
        holder.itemView.place_image.load(items[position].image)

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(items[position], position)
        }

        holder.itemView.add_to_favorites.setOnClickListener {


            AlertDialog.Builder(context)
                .setPositiveButton("Yes") { _, _ ->

                    GlobalScope.launch {

                        favouritesDatabase.favouriteDao().addFavourite(
                            DBPlace(
                                items[position].id,
                                items[position].name,
                                items[position].image,
                                items[position].desc,
                                items[position].lat,
                                items[position].lng,
                            )
                        )
                    }
                    Toast.makeText(
                        context,
                        "Successfully added ${items[position].name} to favourites.",
                        Toast.LENGTH_SHORT
                    ).show()
                }.setNegativeButton("No") { _, _ -> }
                .setTitle("Add ${items[position].name} to favourites?")
                .setMessage("Are you sure you want to add ${items[position].name} to favourites?")
                .create()
                .show()

//                val favId = favouritesDatabase.favouriteDao()
//                    .addFavourite(
//                        DBPlace(
//                            0,
//                            items[position].name,
//                            null,
//                            items[position].desc,
//                            items[position].lat,
//                            items[position].lng,
//                        )
//                    )

//                Log.d("WAZAAP", "placeAdapter favId $favId")

        }
//        holder.itemView.tv_place_desc.text = items[position].desc
//        holder.itemView.tv_place_desc.text = items[position].desc

    }


    override fun getItemCount(): Int {
        return items.size
    }
}
//
//class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    val placeName = itemView.tv_place_name
//    val placeImage = itemView.place_image
//    val placeDesc = itemView.tv_place_desc
//
//
//    fun initialize(item: DBPlace, action: OnPlaceItemClickListener) {
//        placeName.text = item.name
//        placeImage.setImageBitmap(item.image)
//        placeDesc.text = item.desc
//
//
//        itemView.setOnClickListener {
//            action.onItemClick(item, adapterPosition)
//        }
//
//        //adding to favorites
//        val addToFavBtn = itemView.findViewById<ImageView>(R.id.add_to_favorites)
//        addToFavBtn.setOnClickListener {
//            Toast.makeText(itemView.context, item.name, Toast.LENGTH_SHORT).show()
//        }
//
//    }
//}
