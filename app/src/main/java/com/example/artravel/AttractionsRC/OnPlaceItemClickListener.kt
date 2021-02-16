package com.example.artravel.AttractionsRC

import com.example.artravel.database.DBPlace

interface OnPlaceItemClickListener {
    fun onItemClick(item: DBPlace, position: Int)
}