package com.example.subway_for_pregnant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_server.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URISyntaxException

class NodeTestActivity : AppCompatActivity() {

    val Tag = "NodeTestActivity"
    lateinit var mSocket: Socket
    lateinit var username: String
    var users: Array<String> = arrayOf()
    var minor = 0

    //var minor = intent.getStringArrayExtra("MINOR").toString()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        try {
            //IO.socket 메소드는 은 저 URL 을 토대로 클라이언트 객체를 Return 합니다.
           mSocket = IO.socket("https://146025ea.ngrok.io")
            Log.d("NodeTestActivity","socket 생성")
        } catch (e: URISyntaxException) {
            Log.e("NodeTestActivity", e.reason)
        } catch (e: IOException){
            Log.e("NodeTestActivity", "IO에러")
        }

        //중요한 것은 아니며 전 액티비티에서 username을 가져왔습니다.
        //원하시는 방법 대로 username을 가져오시면 될 듯 합니다.
        try {
            var intent = intent;
            username = intent.getStringExtra("username")
            Log.d(Tag,username)
        }
        catch (e: Exception){
            Log.d(Tag, " username NULL")
        }

        try{
           minor = intent.getIntExtra("minor",0)
            Log.i(Tag,minor.toString())
        }
        catch (e: Exception){
            Log.d(Tag, "minor NULL")
        }


        // mSocket.connect() 는 socket과 서버를 연결하는 것으로
        // server 측의 io.on('connection',function (socket){-} 을 트리깅합니다.
        // 다시말하자면 mSocket.emit('connection',socket)을 한 것 과 동일하다고 할 수 있습니다.
        mSocket.connect()
        Log.d(Tag, "connect")

        // 이제 연결이 성공적으로 되게 되면, server측에서 "connect" event 를 발생시키고
        // 아래코드가 그 event 를 핸들링 합니다. onConnect는 65번째 줄로 가서 살펴 보도록 합니다.
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on("newUser", onNewUser)
        mSocket.on("myMsg", onMyMessage)
        mSocket.on("newMsg", onNewMessage)
        mSocket.on("logout", onLogout)

        //send button을 누르면 "say"라는 이벤트를 서버측으로 보낸다.
        send.setOnClickListener {
            val chat = editText.text.toString()
            mSocket.emit("say", minor)
        }

        //logout button을 누르면 "logout"이라는 이벤트를 서버측으로 보낸다.
        logout.setOnClickListener {
            mSocket.emit("logout")
        }
    }

    // onConnect는 Emmiter.Listener를 구현한 인터페이스입니다.
    // 여기서 Server에서 주는 데이터를 어떤식으로 처리할지 정하는 거죠.
    val onConnect: Emitter.Listener = Emitter.Listener {
        //여기서 다시 "login" 이벤트를 서버쪽으로 username 과 함께 보냅니다.
        //서버 측에서는 이 username을 whoIsON Array 에 추가를 할 것입니다.
        Log.d(Tag,"connect success")
        mSocket.emit("login", username)
        Log.d(Tag,"login success")
        Log.d(Tag, "Socket is connected with ${username}")
    }

    val onMyMessage = Emitter.Listener {
        Log.d("on", "Mymessage has been triggered.")
        Log.d("mymsg : ", it[0].toString())
    }

    val onNewMessage = Emitter.Listener {
        Log.d("on", "New message has been triggered.")
        Log.d("new msg : ", it[0].toString())
    }

    val onLogout = Emitter.Listener {
        Log.d("on", "Logout has been triggered.")

        try {
//             {"diconnected" : nickname,
//              "whoIsOn" : whoIsOn
//         } 이런 식으로 넘겨진 데이터를 파싱하는 방법입니다.
            val jsonObj: JSONObject = it[0] as JSONObject //it[0] 은 사실 Any 타입으로 넘어오지만 캐스팅을 해줍니다.
            Log.d("logout ", jsonObj.getString("disconnected"))
            Log.d("WHO IS ON NOW", jsonObj.getString("whoIsOn"))

            //Disconnect socket!
            mSocket.disconnect()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    val onMessageRecieved: Emitter.Listener = Emitter.Listener {
        try {
            val receivedData: Any = it[0]
            Log.d(Tag, receivedData.toString())

        } catch (e: Exception) {
            Log.e(Tag, "error", e)
        }
    }

    val onNewUser: Emitter.Listener = Emitter.Listener {

        var data = it[0] //String으로 넘어옵니다. JSONArray로 넘어오지 않도록 서버에서 코딩한 것 기억나시죠?
        if (data is String) {
            users = data.split(",").toTypedArray() //파싱해줍니다.
            for (a: String in users) {
                Log.d("user", a) //누구누구 있는지 한 번 쫘악 뽑아줘봅시다.
            }
        } else {
            Log.d("error", "Something went wrong")
        }

    }

    override fun onStop() {
        super.onStop()
    }
    override fun onDestroy() {
        super.onDestroy()
    }

}
