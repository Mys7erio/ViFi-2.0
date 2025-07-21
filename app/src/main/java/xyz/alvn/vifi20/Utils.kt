package xyz.alvn.vifi20

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import androidx.work.Worker
import androidx.work.WorkerParameters


// New Worker to call your existing function
class LoginWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val username = inputData.getString("username") ?: return Result.failure()
        val password = inputData.getString("password") ?: return Result.failure()

        sendLoginRequest(applicationContext, username, password)  // Call your original function
        return Result.success()  // Assume success; adjust if you need to check outcomes
    }
}



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

    val client = OkHttpClient()

    if (Looper.myLooper() == Looper.getMainLooper()) {
        // On main thread (e.g., foreground button): Use async enqueue to avoid blocking UI
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("VIFIDBG", "Network error: ${e.message}")
                Handler(ctx.mainLooper).post {
                    Toast.makeText(ctx, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Handler(ctx.mainLooper).post {
                    if (response.isSuccessful) {
                        Toast.makeText(ctx, "Login successful!", Toast.LENGTH_LONG).show()
                    } else {
                        Log.e("VIFIDBG", "${response.code} - ${response.message}")
                        val errorMessage = "Login failed: ${response.code} - ${response.message} Response: $responseBody"
                        Toast.makeText(ctx, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                response.close()
            }
        })
    } else {
        // On background thread (e.g., WorkManager): Use synchronous execute for reliability
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Handler(ctx.mainLooper).post {
                if (response.isSuccessful) {
                    Toast.makeText(ctx, "Login successful!", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("VIFIDBG", "${response.code} - ${response.message}")
                    val errorMessage = "Login failed: ${response.code} - ${response.message} Response: $responseBody"
                    Toast.makeText(ctx, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
            response.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("VIFIDBG", "Network error: ${e.message}")
            Handler(ctx.mainLooper).post {
                Toast.makeText(ctx, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}




// Define keys for SharedPreferences
private val PREFSNAME = "VIFiLoginPrefs"
private val KEYUSERNAME = "username"
private val KEYPASSWORD = "password"


fun getCredentials(ctx: Context): Pair<String, String> {
    val prefs = ctx.getSharedPreferences(PREFSNAME, Context.MODE_PRIVATE)
    val savedUsername = prefs.getString(KEYUSERNAME, "") // "" is the default value if not found
    val savedPassword = prefs.getString(KEYPASSWORD, "")

    return Pair(savedUsername ?: "", savedPassword ?: "")
}


fun saveCredentials(ctx: Context, username: String, password: String) {
    val prefs = ctx.getSharedPreferences(PREFSNAME, Context.MODE_PRIVATE)
    val editor = prefs.edit()
    editor.putString(KEYUSERNAME, username)
    editor.putString(KEYPASSWORD, password)
    editor.apply() // Apply asynchronously, commit() is synchronous
}

