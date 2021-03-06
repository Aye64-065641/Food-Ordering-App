package com.example.foodorderingapp.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookreader.utils.ConnectionManager
import com.example.foodorderingapp.R
import com.example.foodorderingapp.adapter.DishesRecyclerAdapter
import com.example.foodorderingapp.adapter.HomeRecyclerAdapter
import com.example.foodorderingapp.adapter.OrderRecyclerAdapter
import com.example.foodorderingapp.model.Dish
import com.example.foodorderingapp.model.Order
import com.example.foodorderingapp.model.Restaurant
import org.json.JSONException

class OrderFragment : Fragment() {
    lateinit var recyclerPerItem: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: OrderRecyclerAdapter
    lateinit var sharedPreferences: SharedPreferences
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    var dishInfoList = arrayListOf<Order>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order, container, false)
        sharedPreferences =
            this.activity!!.getSharedPreferences(
                getString(R.string.userDetails),
                Context.MODE_PRIVATE
            )
        recyclerPerItem = view.findViewById(R.id.recyclerPerItem)
        layoutManager = LinearLayoutManager(activity)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val userId = sharedPreferences.getString("user_id", "0")
            val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"
            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
                    // handle request
                    println("Response is $it")
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val dataArray = data.getJSONArray("data")
                            for (i in 0 until dataArray.length()) {
                                val restaurantJsonObject = dataArray.getJSONObject(i)
                                val restaurantObject = Order(
                                    restaurantJsonObject.getString("restaurant_name"),
                                    restaurantJsonObject.getString("order_placed_at")
                                )
                                dishInfoList.add(restaurantObject)
                                val dishJsonArray = restaurantJsonObject.getJSONArray("food_items")
                                for (j in 0 until dishJsonArray.length()) {
                                    val dishJsonObject = dishJsonArray.getJSONObject(j)
                                    val dishObject = Order(
                                        dishJsonObject.getString("name"),
                                        dishJsonObject.getString("cost")
                                    )
                                    dishInfoList.add(dishObject)
                                }
                                progressLayout.visibility = View.GONE
                                recyclerAdapter =
                                    OrderRecyclerAdapter(activity as Context, dishInfoList)
                                recyclerPerItem.adapter = recyclerAdapter
                                recyclerPerItem.layoutManager = layoutManager
                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some Error occured",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error occured",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, Response.ErrorListener {
                    // handle error
                    println("Error is $it")
                    Toast.makeText(activity as Context, "volley error occured", Toast.LENGTH_SHORT)
                        .show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "590d13b4181c7b"
                        return headers
                    }
                }

            queue.add(jsonObjectRequest)
        } else {
            println("No Internet")
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return view
    }

}