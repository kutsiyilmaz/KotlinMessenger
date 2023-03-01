package com.example.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {


    private lateinit var registerButton: Button
    private lateinit var selectPhotoButton:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val already_have_account = findViewById(R.id.already_have_account_text_view) as TextView



        registerButton = findViewById(R.id.register_button_register)
        selectPhotoButton = findViewById(R.id.selectphoto_button_register)


        registerButton.setOnClickListener{

            performRegister()
        }

        already_have_account.setOnClickListener {
            Log.d(TAG, "Try to show login activity!")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }

        selectPhotoButton.setOnClickListener{
            Log.d(TAG, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)

        }

    }


    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //google android circleimageview
        val selectphoto_imageview_register = findViewById(R.id.selectphoto_imageview_register) as CircleImageView

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG, "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //selectPhotoButton.setBackgroundDrawable(bitmapDrawable)

        }
    }

    private fun performRegister(){

        val email = findViewById(R.id.email_edittext_register) as EditText
        val password = findViewById(R.id.password_edittext_register) as EditText

        Log.d(TAG, "Email is: " + email.text.toString())
        Log.d(TAG, "Password is: ${password.text.toString()}")

        //email ve password editText turunden string turune cevriliyor.
        if(email.text.toString().isEmpty() || password.text.toString().isEmpty()){
            Toast.makeText(this, "Please enter your email/password!",Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.text.toString(),password.text.toString())
            .addOnCompleteListener{
                if(!it.isSuccessful)return@addOnCompleteListener
                //else if success
                Log.d(TAG, "Successfully created user with uid: ${it.result.user?.uid}")

                uploadImageToFirebaseStorage()

            }.addOnFailureListener {
                Log.d(TAG, "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }




    private fun uploadImageToFirebaseStorage(){

        if(selectedPhotoUri == null)return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }.addOnFailureListener{
                Log.d(TAG, "Could'nt upload image!")
            }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance("https://kotlinmessenger-42817-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/users/$uid")

        //val database = Firebase.database
        //val ref = database.getReference("/users/$uid")

        val usernameEditText = findViewById(R.id.username_edittext_register) as EditText

        val user = User(uid, usernameEditText.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "We saved the user to database!!!")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                //yeni activitye gecince eskisini yok et. geri rusuyla donemeyiz.
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


            }.addOnFailureListener{
                Log.d(TAG, "Couldnt save user to database!!!")
            }

    }

}

/*class User(val uid: String, val username: String, val profileImageUrl: String){
    constructor(): this("","","")//default constr.//disari cikti.
}*/
