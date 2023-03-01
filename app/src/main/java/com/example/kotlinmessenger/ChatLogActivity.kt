package com.example.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder> ()

    private lateinit var recyclerview_chat_log : RecyclerView
    private lateinit var sendButton_chatLog: Button

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log = findViewById(R.id.recyclerview_chat_log)
        recyclerview_chat_log.adapter = adapter

        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        //setupDummyData()
        listenForMessages()

        sendButton_chatLog = findViewById(R.id.send_button_chat_log)
        sendButton_chatLog.setOnClickListener{
            Log.d(TAG, "Attempt to send message....")
            performSendMessage()
        }

    }

    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance("https://kotlinmessenger-42817-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if(chatMessage != null ){
                    Log.d(TAG, chatMessage.text)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text,currentUser!!))
                    }else {
                        adapter.add(ChatToItem(chatMessage.text,toUser!!))//tum mesajların solda degilde gonderen gelene gore sag sola geciren kod.
                    }

                }



            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

        })
    }


    private fun performSendMessage(){
        val text = findViewById<EditText>(R.id.edittext_chat_log).text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        if(fromId == null || toId == null) {
            Log.d(TAG, "fromId or toId is null!!!")
            return
        }
        /*val reference = FirebaseDatabase.getInstance("https://kotlinmessenger-42817-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/messages").push()*/

        val reference = FirebaseDatabase.getInstance("https://kotlinmessenger-42817-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance("https://kotlinmessenger-42817-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/user-messages/$toId/$fromId").push()//karsilikli mesajlarin saklanmasi icin.


        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                findViewById<EditText>(R.id.edittext_chat_log).text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)//mesaj gonderildikten sonra son mesaja oto scroll.
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance("https://kotlinmessenger-42817-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/latest-messages/$fromId/$toId")//push yok boylece stack olusmuyor.
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance("https://kotlinmessenger-42817-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)


    }

/*
  private fun setupDummyData(){
      val adapter = GroupAdapter<GroupieViewHolder> ()

      adapter.add(ChatFromItem("ssssssssssssssssssssssssssssssssssss"))
      adapter.add(ChatToItem("aaaaaaaaaaaaaaaaaaaa\naaaaaaaaaaaa"))


      recyclerview_chat_log.adapter = adapter

    }*/
}

class ChatFromItem(val text:String,val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_from_row)
        Picasso.get().load(uri).into(targetImageView)
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row

    }
}
class ChatToItem(val text:String,val user:User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_to_row)
        Picasso.get().load(uri).into(targetImageView)
    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row

    }
}