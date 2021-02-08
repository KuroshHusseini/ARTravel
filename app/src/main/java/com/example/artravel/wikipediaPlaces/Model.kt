package com.example.artravel.wikipediaPlaces

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Model {
    /**
     *
     * @return
     * The batchcomplete
     */
    /**
     *
     * @param batchcomplete
     * The batchcomplete
     */
    @SerializedName("batchcomplete")
    @Expose
    var batchcomplete: String? = null
    /**
     *
     * @return
     * The query
     */
    /**
     *
     * @param query
     * The query
     */
    @SerializedName("query")
    @Expose
    var query: Query? = null
}