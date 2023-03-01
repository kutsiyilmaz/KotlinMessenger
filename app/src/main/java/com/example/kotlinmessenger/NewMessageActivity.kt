package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.example.kotlinmessenger.R.layout.user_row_new_message
import com.example.kotlinmessenger.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NewMessageActivity : AppCompatActivity() {

    private lateinit var recyclerview_newmessage :RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        recyclerview_newmessage = findViewById(R.id.recyclerview_newmessage) as RecyclerView

        /*val adapter = GroupAdapter<GroupieViewHolder>()

        adapter.add(UserItem())
        adapter.add(UserItem())
        //adapter.add(UserItem())

        recyclerview_newmessage.adapter = adapter*/
        //xml icindede acilabilir.
        //recyclerview_newmessage.layoutManager = LinearLayoutManager(this)

        fetcUser()

    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetcUser(){
        val ref = FirebaseDatabase.getInstance("https://kotlinmessenger-42817-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if(user != null){
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                   // intent.putExtra(USER_KEY,userItem.user.username)
                    intent.putExtra(USER_KEY,userItem.user)// tum objecti gecirebilmek icin parcelize gerekti.
                    startActivity(intent)
                    finish()//newmessage yi bitir. geri tusuna basinca latestmessage ye don.
                }

                recyclerview_newmessage.adapter = adapter

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

class UserItem(val user:User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.username_textview_new_message).text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.imageview_new_message_row))
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

}


/*Bunun yerine groupie library kullanıcaz.
class CustomAdapter: RecyclerView.Adapter<ViewHolder>{
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}*/