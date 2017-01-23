# Courses schema


# --- !Ups
 
CREATE TABLE courses (
  courseID INT NOT NULL AUTO_INCREMENT,
  departmentID VARCHAR(4) NOT NULL,
  courseNumber INT NOT NULL,
  courseName VARCHAR(40) NOT NULL,
  units INT NOT NULL,
  PRIMARY KEY (courseID)
);


ALTER TABLE courses
ADD UNIQUE INDEX code_unique (departmentID,courseNumber);

CREATE TABLE courseDescriptions (
  courseID INT NOT NULL,
  description VARCHAR(200) NOT NULL,
  PRIMARY KEY (courseID)
);

CREATE TABLE prerequisites(
	courseID INT NOT NULL,
	prerequisite INT NOT NULL
); 

ALTER TABLE prerequisites
ADD UNIQUE INDEX prereq_pair_unique (courseID,prerequisite);
 
# --- !Downs

DROP TABLE courses;
DROP TABLE courseDescriptions;
DROP TABLE prerequisites;

