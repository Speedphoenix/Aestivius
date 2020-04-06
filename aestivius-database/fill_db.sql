DROP TABLE IF EXISTS match;
CREATE TABLE match (
  _id SERIAL PRIMARY KEY,
  localid INTEGER,
  date bigint,
  location TEXT,
  winner TEXT,
  loser TEXT,
  score TEXT,
  phoneid TEXT
);
