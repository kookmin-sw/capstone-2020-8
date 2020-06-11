const createError = require('http-errors');
const express = require('express');
const firebase = require("firebase");
const path = require('path');
const cookieParser = require('cookie-parser');
const logger = require('morgan');

var indexRouter = require('./routes/index');

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

// socket.io
app.io = require('socket.io')();
let room = ['room0', 'room1'] // 룸 선언
var a = 0;
var minor = "";
var id = "";
var carNum, seatNum;
var isReservation = false;

app.io.on('connection', (socket) => {
  console.log("연결");
  // join room 추가
  socket.on('joinRoom', (num) => {
    socket.join(room[num], () => {
      console.log('joinroom');
      app.io.to(room[num]).emit('joinRoom', num);
    });
  });
  // leave room 추가
  socket.on('leaveRoom', (num) => {
    console.log('leaveroom');
    socket.leave(room[num]).emit('leaveRoom', num);
  })
  socket.on('login', (data, num, email, isR) => { // minor
    console.log(`minor : ${data}`);
    // whoIsOn.push(data) 
    a = num;
    minor = data;
    id = email;
    carNum = minor[0];
    seatNum = minor[2];
    isReservation = isR;
    console.log(`${carNum}, ${seatNum}`);


    var minorJson = `${minor}`;
    console.log(minorJson);
    console.log(`a : ${a}, num : ${num}, email : ${email}, `)
    console.log(`room${num}의 minor : ${data}`);

    app.io.to(room[a]).emit('login', data, num, email, isR);
  });

  socket.on('say', (data) => {
    console.log(`say msg : ${data}`);
    socket.emit('myMsg', data);
  });

  socket.on('disconnect', () => {
    console.log(`${minor} has left this connection ------------------------  `);
  });

  socket.on('logout', () => {
    socket.emit('logout', data);
  });
});

console.log("끝")

// MQTT Server 접속. 센서 데이터 읽기
var mqtt = require("mqtt") // mqtt 모듈 불러오기
var client = mqtt.connect("mqtt://192.168.137.1") // mqtt 접속 프로토콜
var topic_list = ['Pi1', 'Pi2']

client.on("connect", () => {
  client.subscribe(topic_list) // 구독할 토픽
});

client.on("message", (topic, message) => {
  var db = firebase.firestore();
  var obj = JSON.parse(message); // 객체화

  console.log(topic);
  console.log(obj);

  if (topic === 'Pi1') {
    if (obj.s1_isSit)
      client.publish('LED601', '0');
  }
  if (topic === 'Pi2') {
    if (obj.s2_isSit)
      client.publish('LED602', '0');
  }

  db.collection('Demo_subway').doc('line8').collection('Down').doc('8201').
    collection('car').doc('6').update(obj);
})

function pubMinor() {
  // 8호선 하행, 잠실, 814
  var db = firebase.firestore();
  var isUser;
  if (minor != "") {
    db.collection('Demo_subway').doc('line8').collection('Down').doc('8201').
      collection('car').doc(carNum).collection('section').doc('814').get()
      .then(doc => {
        if (!doc.exists) {
          console.log('No such document!');
        } else {
          //console.log('Document data:', doc.data());
          console.log(`seatNum = ${seatNum}`);

          isUser = 's' + seatNum + '_User';

          //
          if(doc.data().s1_isReservation = true) {
            client.publish('LED' + minor, '3')
          } else {
            client.publish('LED' + minor, 'r')
          }

          if (isReservation) { // 사용자가 예약을 한 경우
            if (doc.data().s1_User === id) {// 현재 사용자가 예약한 자리인 경우
              console.log(doc.id, '=>', doc.data());// 본인이 예약한 자리에 잘 찾아왔을 경우
              console.log('예약한 산모 + <-가 예약한 자리')
              client.publish('LED' + minor, '2')
              
            } else { // 사용자는 예약했는데 다른 자리에 찾아온 경우
              console.log('예악한 산모 + <-가 예약 안 한 자리')
            }
          } else { // 사용자가 예약하지 않은 경우
            if (doc.data().s1_User === '') { // 예약하지 않은 사용자 + 예약 안된 자리
              console.log('예약 안 한 산모 + 예약 안 된 자리')
              client.publish('LED' + minor, '1')
             
            } else { // 예약하지 않은 사용자 + 다른 사람이 예약한 자리
              console.log('예약 안 한 산모 + 예약되어 있는 자리');
            }
          }
          minor = ""
          return;
        }
      })
      .catch(err => {
        console.log('Error getting document', err);
      });

  }
}

setInterval(() => {
  pubMinor();
}, 1000);

module.exports = app;