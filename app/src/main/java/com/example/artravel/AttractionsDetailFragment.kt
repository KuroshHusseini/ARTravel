package com.example.artravel


import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_attractions_detail.view.*



class AttractionsDetailFragment : Fragment() {
    private var lat: String? = null
    private var lon: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_attractions_detail, container, false)
        view.showOnMap_button.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("lat", lat)
            bundle.putString("lon", lon)

            findNavController().navigate(
                R.id.action_attractionsDetailFragment_to_attractionsDrawRoute,
                bundle
            )
        }

        val name = arguments?.getString("name")
        val bytes = arguments?.getByteArray("bytes")
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
        val description = arguments?.getString("description")

        lat = arguments?.getString("lat")
        lon = arguments?.getString("lon")

        Log.d("DBEGGUS", name!!)
        if (description != null) {
            Log.d("DBEGGUS", description)
        }
        if (lat != null) {
            Log.d("DBEGGUS", lat!!)
        }
        if (lon != null) {
            Log.d("DBEGGUS", lon!!)
        }

        view.tv_detail_place_name.text = name
        if (image != null)
            view.iv_detail_place_image.setImageBitmap(image)
        view.tv_detail_place_desc.text = description
        return view
    }
}