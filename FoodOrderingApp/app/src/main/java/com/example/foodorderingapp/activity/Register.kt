package com.example.foodorderingapp.activity

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookreader.utils.ConnectionManager
import com.example.foodorderingapp.R
import org.json.JSONObject
import java.lang.Exception

class Register : AppCompatActivity() {
    lateinit var etName: EditText
    lateinit var etEmail: EditText
    lateinit var etMobileNumber: EditText
    lateinit var etDeliveryAddress: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            getSharedPreferences(getString(R.string.userDetails), Context.MODE_PRIVATE)
        setContentView(R.layout.activity_register)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            /*
            1 ) Name should be min 3 chars long
            2) Password should be min 4 chars long
            */
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val mobileNumber = etMobileNumber.text.toString()
            val deliveryAddress = etDeliveryAddress.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            if (password == confirmPassword) {
                if (ConnectionManager().checkConnectivity(this@Register)) {
                    val queue = Volley.newRequestQueue(this@Register)
                    val url = "http://13.235.250.119/v2/register/fetch_result"
                    val jsonParams = JSONObject()
                    jsonParams.put("name", name)
                    jsonParams.put("mobile_number", mobileNumber)
                    jsonParams.put("password", password)
                    jsonParams.put("address", deliveryAddress)
                    jsonParams.put("email", email)
                    val jsonRequest = object : JsonObjectRequest(
                        Request.Method.POST, url, jsonParams,
                        Response.Listener {
                            println("Response is $it")
                            val data = it.getJSONObject("data")
                            println(data)
                            try {
                                val success = data.getBoolean("success")
                                if (success) {
                                    val userDetails = data.getJSONObject("data")
                                    saveSharedPreferences(userDetails)
                                    Toast.makeText(
                                        this@Register,
                                        "Registered",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this@Register, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val errorMessage = data.getString("errorMessage")
                                    Toast.makeText(
                                        this@Register,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@Register,
                                    "Some unexpected error occured",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        Response.ErrorListener {
                            Toast.makeText(
                                this@Register,
                                "volley error occured",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "590d13b4181c7b"
                            return headers
                        }
                    }
                    queue.add(jsonRequest)
                } else {
                    println("No Internet")
                    val dialog = AlertDialog.Builder(this@Register)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection is not Found")
                    dialog.setPositiveButton("Open Settings") { text, listener ->
                        val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingIntent)
                        finish()
                    }
                    dialog.setNegativeButton("Exit") { text, listener ->
                        ActivityCompat.finishAffinity(this@Register)
                    }
                    dialog.create()
                    dialog.show()
                }
            } else {
                Toast.makeText(this@Register, "Password Does not match", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun saveSharedPreferences(userDetails: JSONObject) {
        sharedPreferences.edit().putString("user_id", userDetails.getString("user_id")).apply()
        sharedPreferences.edit().putString("name", userDetails.getString("name")).apply()
        sharedPreferences.edit().putString("email", userDetails.getString("email")).apply()
        sharedPreferences.edit().putString("mobile_number", userDetails.getString("mobile_number"))
            .apply()
        sharedPreferences.edit().putString("address", userDetails.getString("address")).apply()
    }
}