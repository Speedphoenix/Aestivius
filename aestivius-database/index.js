const express = require('express');
const  bodyparser = require('body-parser');
const { Pool, Client } = require('pg');
const cors = require('cors');

const pool = new Pool({
  user: process.env.PGUSER || 'aestivius',
  host: process.env.PGHOST || 'localhost',
  database: process.env.PGDATABASE || 'aestivius',
  password: process.env.PGPASSWORD || 'aestiviuspassword',
  port: process.env.PGPORT || 5432,
});

console.error(process.env.PGHOST);
console.error(process.env);


const app = express();
const port = process.env.PORT || '8096';
app.use(bodyparser.json());
app.use(bodyparser.urlencoded());
app.use(cors());

const inserttext = 'INSERT INTO '
  + 'match(localid, date, location, winner, loser, score, phoneid) '
  + 'VALUES($1, $2, $3, $4, $5, $6, $7) RETURNING _id';

const insertMatch = (match, phone, callback) => {
  const values = [
    match._id,
    match.date,
    match.location,
    match.winner,
    match.loser,
    match.score,
    phone,
  ];

  pool.query(inserttext, values, (err, res) => {
    callback(err, res);
  });
};

const getAll = (phone, callback) => {
  pool.query('SELECT * FROM match WHERE phoneid=$1', [ phone ], (err, res) => {
    if (err) {
      callback(err, res);
    } else {
      callback(err, res.rows);
    }
  });
};

const printErrStack = (err) => {
  console.error(err.name + ": " + err.message);
  console.error(err.stack);
};

app.get('/match/:phoneid', (req, res) => {
  getAll(req.params.phoneid, (err, result) => {
    if (err) {
      res.status(403).send(err.toString());
      printErrStack(err);
    }
    else {
      console.log(`${req.params.phoneid} asked for matches`);
      togive = result.map((val) => {
        delete val.phoneid;
        delete val._id;
        return val;
      })
      res.status(200).json(togive);
    }
  });
});

app.post('/match/:phoneid', (req, res) => {
  console.log(`${req.params.phoneid} is creating a match`);
  insertMatch(req.body, req.params.phoneid, (err, result) => {
    if (err) {
      res.status(403).send(err.toString());
      printErrStack(err);
    } else {
      console.log(`${req.params.phoneid} created a match`);
      togive = {
        result: "success"
      }
      res.status(200).json(togive);
    }
  });
  // insert the received match into the database
});

app.listen(port, (err) => {
  if (err) {
    throw err;
  }
  console.log(`server is listening on port ${port}`);
});
