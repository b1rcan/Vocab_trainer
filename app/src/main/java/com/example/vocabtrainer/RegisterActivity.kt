package com.example.vocabtrainer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vocabtrainer.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()

        b.btnRegister.setOnClickListener {
            val name = b.etRegisterName.text.toString().trim() // İsimimizin alanı buaray gelcek
            val email = b.etRegisterEmail.text.toString().trim()
            val pass = b.etRegisterPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Lütfen her yeri doldur!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Kayıt başarılı! Şimdi ismi profile kaydedelim:
                    val user = auth.currentUser

                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        displayName = name
                    }

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            Toast.makeText(this, "Hoş geldin $name!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this, "Hata: ${task.exception?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}