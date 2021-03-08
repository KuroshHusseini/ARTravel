package com.example.artravel.fragments.list


import android.app.AlertDialog
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * AttractionsAdapter shows items displayed in AttractionsFragment
 *
 * @author Michael Lock & Kurosh Husseini
 * @date 08.03.2021
 */

class AttractionsAdapter(
    var context: Context,
    var items: List<DBAttraction>,
    var clickListener: OnPlaceItemClickListener
) :
    RecyclerView.Adapter<AttractionsAdapter.AttractionsViewHolder>() {

    private var favourites: List<DBPlace>? = null

    private val favouritesDatabase by lazy { ARTravelDatabase.getDatabase(context) }

    inner class AttractionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionsViewHolder {
        return AttractionsViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.attraction_item, parent, false)
        )
    }

    /**
     * Queries all items from favourites table and adds selected attraction item if it
     * hasn't been added.
     *
     * Method is in 'add_to_favorites' setOnClickListener
     *
     * @author Michael Lock & Kurosh Husseini
     * @date 25.02.2021
     */

    private suspend fun getFavourites(position: Int) {
        val isReady = GlobalScope.async {
            favourites = favouritesDatabase.favouriteDao().getAll()
            Log.d("Favs", favourites.toString())
        }

        if (isReady.await() === 1) {

            var selectedItem = favourites?.find { it.xid == items[position].xid }

            if (selectedItem == null) {
                AlertDialog.Builder(context)
                    .setPositiveButton("Yes") { _, _ ->

                        GlobalScope.launch {
                            favouritesDatabase.favouriteDao().addFavourite(
                                DBPlace(
                                    items[position].id,
                                    items[position].xid,
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
            } else {
                AlertDialog.Builder(context)
                    .setNegativeButton("No") { _, _ -> }
                    .setTitle("${items[position].name} is already added to favourites.")
                    .create()
                    .show()
            }
        }
    }

    override fun onBindViewHolder(holder: AttractionsViewHolder, position: Int) {
        holder.itemView.tv_place_desc.text = items[position].desc
        holder.itemView.tv_place_name.text = items[position].name
        holder.itemView.place_image.load(items[position].image)

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(items[position], position)
        }

        holder.itemView.add_to_favorites.setOnClickListener {

            GlobalScope.launch(Dispatchers.Main) {
                getFavourites(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}