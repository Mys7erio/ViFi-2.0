package xyz.alvn.vifi20

import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

fun sendLoginRequest(ctx: Context, username: String, password: String) {
    // Construct the form data as per the curl request
    val requestBodyString = "userId=$username&password=$password&serviceName=ProntoAuthentication"
    val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
    val requestBody = requestBodyString.toRequestBody(mediaType)
    Log.i("VIFIDBG", "Request body: $requestBodyString")

    val request = Request.Builder()
        .url("http://172.16.1.1/cgi-bin/authlogin") // Target URL
        .post(requestBody) // Set the request method to POST with the body
        .build()

    // Execute the request on a background thread
    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // This is called when the network request fails (e.g., no internet, host unreachable)
            e.printStackTrace()
            Log.e("VIFIDBG", "Network error: ${e.message}")
//            (ctx as? MainActivity)?.runOnUiThread {
//                Toast.makeText(
//                    ctx,
//                    "Network error: ${e.message}",
//                    Toast.LENGTH_LONG
//                ).show()
            android.os.Handler(ctx.mainLooper).post {
                Toast.makeText(ctx, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            // This is called when a response is received from the server (even if it's an error like 404, 500)
            val responseBody = response.body?.string() // Get the response body as a string

//            if ((ctx is MainActivity) || (ctx is QuickSettingLoginTile)) {
//                if (response.isSuccessful) {
//                    Toast.makeText(ctx, "Login successful!", Toast.LENGTH_LONG)
//                        .show()
//                } else {
//                    // Request failed with an HTTP error code (e.g., 401 Unauthorized, 404 Not Found)
//                    val errorMessage =
//                        "Login failed: ${response.code} - ${response.message} Response: $responseBody"
//                    Toast.makeText(ctx, errorMessage, Toast.LENGTH_LONG).show()
//                }
//            }
            android.os.Handler(ctx.mainLooper).post {
                if (response.isSuccessful) {
                    Toast.makeText(ctx, "Login successful!", Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = "Login failed: ${response.code} - ${response.message} Response: $responseBody"
                    Toast.makeText(ctx, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
            response.close()
        }
    })
}


// Define keys for SharedPreferences
private val PREFSNAME = "VIFiLoginPrefs"
private val KEYUSERNAME = "username"
private val KEYPASSWORD = "password"


fun getCredentials(ctx: Context): Pair<String, String> {
    val prefs = ctx.getSharedPreferences(PREFSNAME, Context.MODE_PRIVATE)
    val savedUsername = prefs.getString(KEYUSERNAME, "") // "" is the default value if not found
    val savedPassword = prefs.getString(KEYPASSWORD, "")

    // Set the loaded credentials to the EditText fields
    //    usernameET.setText(savedUsername)
    //    passwordET.setText(savedPassword)
    return Pair(savedUsername ?: "", savedPassword ?: "")
}


fun saveCredentials(ctx: Context, username: String, password: String) {
    val prefs = ctx.getSharedPreferences(PREFSNAME, Context.MODE_PRIVATE)
    val editor = prefs.edit()
    editor.putString(KEYUSERNAME, username)
    editor.putString(KEYPASSWORD, password)
    editor.apply() // Apply asynchronously, commit() is synchronous
}
