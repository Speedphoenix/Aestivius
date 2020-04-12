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

-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO aestivius;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO aestivius;
