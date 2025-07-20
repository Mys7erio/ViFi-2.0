package xyz.alvn.vifi20

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


import android.widget.Button
import android.widget.EditText
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private lateinit var usernameET: EditText
    private lateinit var passwordET: EditText
    private lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameET = findViewById(R.id.editTextUsername)
        passwordET = findViewById(R.id.editTextPassword)
        loginBtn = findViewById(R.id.buttonLogin)

        Log.i("VIFIDBG", "onCreate called")
        // Load saved credentials when the activity is created
        val (username, password) = getCredentials(ctx = this)
        usernameET.setText(username)
        passwordET.setText(password)

        // Set an OnClickListener for the button
        loginBtn.setOnClickListener {
            handleWifiLogin()
        }

        // Check if the activity was launched from the Quick Settings Tile
        if (intent.getStringExtra("source") == "quick_settings_tile") {
            handleWifiLogin()
        }
    }

    private fun handleWifiLogin() {
        val username = usernameET.text.toString()
        val password = passwordET.text.toString()
        Log.i("VIFIDBG", "Username: $username, Password: $password")

        // Input validation
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT)
                .show()
            return // Stop if inputs are empty
        }

        saveCredentials(this, username, password)
        sendLoginRequest(this, username, password)


    }
}
