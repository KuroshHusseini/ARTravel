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
import com.example.artravel.R
import com.example.artravel.model.entity.DBPlace
import com.example.artravel.model.database.ARTravelDatabase
import kotlinx.android.synthetic.main.attraction_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FavouritesAdapter(
    var context: Context,
    private val favouritesList: List<DBPlace>,
    private var clickListener: OnPlaceItemClickListener
) :
    RecyclerView.Adapter<FavouritesAdapter.MyViewHolder>() {

    private val favouritesDatabase by lazy { ARTravelDatabase.getDatabase(context) }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.attraction_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = favouritesList[position]
        if (currentItem != null) {
            holder.itemView.place_image.load(currentItem.image)
            holder.itemView.tv_place_name.text = currentItem.name
            holder.itemView.tv_place_desc.text = currentItem.desc
        }
        holder.itemView.setOnClickListener {
            clickListener.onItemClick(currentItem, position)
        }
        holder.itemView.add_to_favorites.setOnClickListener {
            AlertDialog.Builder(context)
                .setPositiveButton("Yes") { _, _ ->

                    GlobalScope.launch {
                        favouritesDatabase.favouriteDao().deleteFavourite(
                            currentItem
                        )
                    }
                    Toast.makeText(
                        context,
                        "Successfully Deleted ${currentItem.name} to favourites.",
                        Toast.LENGTH_SHORT
                    ).show()
                }.setNegativeButton("No") { _, _ -> }
                .setTitle("Delete ${currentItem.name} from favourites?")
                .setMessage("Are you sure you want to delete ${currentItem.name} from favourites?")
                .create()
                .show()
        }
    }
    override fun getItemCount() = favouritesList.size
}