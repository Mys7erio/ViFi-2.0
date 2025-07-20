package xyz.alvn.vifi20

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import okhttp3.Call // For 'Call'
import okhttp3.Callback // For 'Callback' interface
import okhttp3.OkHttpClient // For OkHttpClient class
import okhttp3.Request // For Request.Builder
import okhttp3.Response // For Response class
import okhttp3.MediaType.Companion.toMediaTypeOrNull // Extension function for MediaType
import okhttp3.RequestBody.Companion.toRequestBody   // Extension function for RequestBody
import java.io.IOException // For network error handling
class MainActivity : AppCompatActivity() {

    private lateinit var usernameET: EditText
    private lateinit var passwordET: EditText
    private lateinit var loginBtn: Button
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameET = findViewById(R.id.editTextUsername)
        passwordET = findViewById(R.id.editTextPassword)
        loginBtn = findViewById(R.id.buttonLogin)

        // Set an OnClickListener for the button
        loginBtn.setOnClickListener {
            sendLoginRequest()
        }

        // Check if the activity was launched from the Quick Settings Tile
        if (intent.getStringExtra("source") == "quick_settings_tile") {
            sendLoginRequest()
        }
    }

    // Encapsulate the button's action in a separate function
//    private fun sendLoginRequest() {
//        Toast.makeText(this, "Texts combined successfully!", Toast.LENGTH_SHORT).show()
//    }

    private fun sendLoginRequest() {
        val userId = usernameET.text.toString()
        val password = passwordET.text.toString()

        // Input validation
        if (userId.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT)
                .show()
            return // Stop if inputs are empty
        }

        // Construct the form data as per the curl request
        val requestBodyString = "userId=$userId&password=$password&serviceName=ProntoAuthentication"
        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val requestBody = requestBodyString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://172.16.1.1/cgi-bin/authlogin") // Target URL
            .post(requestBody) // Set the request method to POST with the body
            .build()

        // Execute the request on a background thread
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // This is called when the network request fails (e.g., no internet, host unreachable)
                e.printStackTrace()
                Log.e("LoginError", "Network error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // This is called when a response is received from the server (even if it's an error like 404, 500)
                val responseBody = response.body?.string() // Get the response body as a string

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        // Request failed with an HTTP error code (e.g., 401 Unauthorized, 404 Not Found)
                        val errorMessage =
                            "Login failed: ${response.code} - ${response.message} Response: $responseBody"
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                response.close()
            }
        })
    }
}
