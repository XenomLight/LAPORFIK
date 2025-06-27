package com.example.applaporfik

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AdminTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the simple layout created above
        setContentView(R.layout.admin_dashboard)

        // No extra setup needed here, the NavHostFragment handles the rest
        // based on the navGraph attribute in the layout.
    }
}
    