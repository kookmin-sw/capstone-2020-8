var express = require('express');
var router = express.Router();
const firebase = require("firebase");

/* GET home page. */
router.get('/', function(req, res, next) {
  var db = firebase.firestore();

  db.collection('user').get()
    .then((snapshot) => {
        snapshot.forEach((doc) => {
            console.log(doc.id, '=>', doc.data());
        });
    })
    .catch((err) => {
        console.log('Error getting documents', err)
    });
  res.render('index', { title: 'Express' });
});

module.exports = router;