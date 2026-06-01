package com.example.vocabtrainer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vocabtrainer.data.local.UserNamePrefs
import com.example.vocabtrainer.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userNamePrefs: UserNamePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()
        userNamePrefs = UserNamePrefs(this)

        b.btnRegister.setOnClickListener {
            val name = b.etRegisterName.text.toString().trim()
            val email = b.etRegisterEmail.text.toString().trim()
            val pass = b.etRegisterPassword.text.toString().trim()
            val passAgain = b.etRegisterPasswordAgain.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || passAgain.isEmpty()) {
                Toast.makeText(this, getString(R.string.register_fields_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != passAgain) {
                Toast.makeText(this, getString(R.string.register_password_mismatch), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            userNamePrefs.saveName(name)
                            Toast.makeText(this, getString(R.string.welcome_user, name), Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, getString(R.string.profile_update_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.register_error_prefix, task.exception?.message.orEmpty()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}