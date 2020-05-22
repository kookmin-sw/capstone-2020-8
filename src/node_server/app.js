const createError = require('http-errors');
const express = require('express');
const firebase = require("firebase");
const path = require('path');
const cookieParser = require('cookie-parser');
const logger = require('morgan');

var indexRouter = require('./routes/index');
var bleRouter = require('./routes/ble');

const firebaseConfig = {
  apiKey: "AIzaSyCfgOZevt5ojBKE1f12zbH30vIVwOjA9QM",
  authDomain: "nunchiboyijo.firebaseapp.com",
  databaseURL: "https://nunchiboyijo.firebaseio.com",
  projectId: "nunchiboyijo",
  storageBucket: "nunchiboyijo.appspot.com",
  messagingSenderId: "1066448612102",
  appId: "1:1066448612102:web:ec5f0034722d5861758c87",
  measurementId: "G-GD1Y77CHWZ"
};
firebase.initializeApp(firebaseConfig);

// express
var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/ble', bleRouter);

// catch 404 and forward to error handler
app.use((req, res, next) => {
  next(createError(404));
});

// error handler
app.use((err, req, res, next) => {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});
console.log("ㅁㄴㅇㄹ");

// socket.io
app.io = require('socket.io')();
var whoIsOn = [];
app.io.on('connection', function(socket) {
  console.log("연결");
  var nickname = ``
   
    //일단 socket.on('login') 이라는 것은 클라이언트가 login 이라는 이벤트를 발생시키면
    //어떤 콜백 함수를 작동시킬 것인지 설정하는 것입니다.
    socket.on('login',function(data){
        console.log(`${data} has entered chatroom! ---------------------`)
        whoIsOn.push(data) //
        nickname = data

        // 아래와 같이 하면 그냥 String 으로 넘어가므로 쉽게 파싱을 할 수 있습니다.
        // 그냥 넘기면 JSONArray로 넘어가서 복잡해집니다.
        var whoIsOnJson = `${whoIsOn}`
        console.log(whoIsOnJson)
        
        //io.emit 과 socket.emit과 다른 점은 io는 서버에 연결된 모든 소켓에 보내는 것이고
        // socket.emit은 현재 그 소켓에만 보내는 것입니다.       
      
        app.io.emit('newUser',whoIsOnJson)
    });

    socket.on('say',function(data){
        console.log(`${nickname} : ${data}`)
    


        socket.emit('myMsg',data)
        socket.broadcast.emit('newMsg',data) // socket.broadcast.emit은 현재 소켓이외의 서버에 연결된 모든 소켓에 보내는 것.
    });

    socket.on('disconnect',function(){
        console.log(`${nickname} has left this chatroom ------------------------  `)
    });

    socket.on('logout',function(){

        //Delete user in the whoIsOn Arryay
        whoIsOn.splice(whoIsOn.indexOf(nickname),1);
        var data = {
            whoIsOn: whoIsOn,
            disconnected : nickname
        }
        socket.emit('logout',data);
    });
});

console.log("끝")

// MQTT Server 접속. 센서 데이터 읽기
var mqtt = require("mqtt") // mqtt 모듈 불러오기
var client = mqtt.connect("mqtt://192.168.137.1") // mqtt 접속 프로토콜

client.on("connect", () => {
  client.subscribe("IoT") // 구독할 토픽
});

client.on("message", (topic, message) => {
  var obj = JSON.parse(message); // 객체화
  obj.create_at = new Date(); // 날짜 정보fun
  console.log(obj);
})

function pubMinor(){
  client.publish("Minor", "1")
}

setInterval(function() {
  pubMinor();
}, 5000);

module.exports = app;
