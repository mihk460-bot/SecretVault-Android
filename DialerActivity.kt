package com.example.secretvault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DialerActivity : AppCompatActivity() {

    private var inputCode = ""
    private lateinit var displayText: TextView
    private var lockoutTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) {
            moveTaskToBack(true)
        }

        if (!SecurityManager.isPasscodeSet(this)) {
            showSetupDialog(firstTime = true)
        } else {
            setContentView(R.layout.activity_dialer)
            initUI()
        }
    }

    private fun showSetupDialog(firstTime: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Your Secret PIN")
            .setMessage("Enter a PIN of at least ${SecurityManager.MIN_PIN_LENGTH} digits:")

        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        builder.setView(input)

        builder.setPositiveButton("Next") { dialog, _ ->
            val code = input.text.toString()
            if (code.length >= SecurityManager.MIN_PIN_LENGTH) {
                dialog.dismiss()
                showConfirmDialog(code)
            } else {
                Toast.makeText(
                    this,
                    "PIN must be at least ${SecurityManager.MIN_PIN_LENGTH} digits",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
                showSetupDialog(firstTime = true)
            }
        }

        builder.setNegativeButton("Cancel") { _, _ -> finish() }
        builder.setCancelable(false).show()
    }

    private fun showConfirmDialog(originalPin: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Your PIN")
            .setMessage("Re-enter your PIN to confirm:")

        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val confirmed = input.text.toString()
            if (confirmed == originalPin) {
                SecurityManager.setPasscode(this, confirmed)
                Toast.makeText(this, "PIN saved! Reopen the app.", Toast.LENGTH_LONG).show()
                dialog.dismiss()
                finish()
            } else {
                Toast.makeText(this, "PINs don't match. Try again.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showSetupDialog(firstTime = true)
            }
        }

        builder.setNegativeButton("Back") { dialog, _ ->
            dialog.dismiss()
            showSetupDialog(firstTime = true)
        }

        builder.setCancelable(false).show()
    }

    private fun initUI() {
        displayText = findViewById(R.id.tvDisplay)

        val dialButtons = listOf(
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnStar, R.id.btn0, R.id.btnHash
        )

        dialButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener { v ->
                if (inputCode.length < 15) {
                    inputCode += (v as Button).text.toString()
                    updateDisplay()
                }
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            if (inputCode.isNotEmpty()) {
                inputCode = inputCode.dropLast(1)
                updateDisplay()
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnLongClickListener {
            inputCode = ""
            updateDisplay()
            true
        }

        findViewById<Button>(R.id.btnCall).setOnClickListener {
            handleCallButton()
        }
    }

    private fun handleCallButton() {
        if (LockoutManager.isLockedOut(this)) {
            val secs = LockoutManager.remainingLockoutSeconds(this)
            Toast.makeText(this, "Too many attempts. Try again in ${secs}s", Toast.LENGTH_LONG).show()
            return
        }

        if (SecurityManager.verifyPasscode(this, inputCode)) {
            LockoutManager.resetAttempts(this)
            startActivity(Intent(this, SecretVaultActivity::class.java))
            inputCode = ""
            updateDisplay()
        } else {
            val lockedOut = LockoutManager.recordFailedAttempt(this)
            if (lockedOut) {
                val secs = LockoutManager.remainingLockoutSeconds(this)
                Toast.makeText(this, "Too many attempts. Try again in ${secs}s", Toast.LENGTH_LONG).show()
            }

            if (inputCode.isNotEmpty()) {
                try {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$inputCode")))
                } catch (e: Exception) { }
            }

            inputCode = ""
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        displayText.text = "•".repeat(inputCode.length)
    }

    override fun onDestroy() {
        super.onDestroy()
        lockoutTimer?.cancel()
    }
}
