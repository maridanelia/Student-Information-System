# Users schema
 
# --- !Ups
 
CREATE TABLE user (
  Email VARCHAR(40) NOT NULL,
  FirstName VARCHAR(20) NOT NULL,
  LastName VARCHAR(30) NOT NULL,
  UserType INT NOT NULL,
  Password VARCHAR(45) NOT NULL,
PRIMARY KEY (Email)
);

CREATE TABLE removed_users (
  Email VARCHAR(40) NOT NULL,
  PRIMARY KEY (Email)
);
 
# --- !Downs
 
DROP TABLE user;
DROP TABLE removed_users;
