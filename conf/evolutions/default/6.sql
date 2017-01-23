# --- !Ups
CREATE TABLE semesters(
	semesterID INT NOT NULL AUTO_INCREMENT,
	year INT NOT NULL,
	term INT NOT NULL,
	startDate BIGINT(1) NOT NULL,
	endDate BIGINT(2) NOT NULL,
	primary key(semesterID)
); 

CREATE TABLE activeSemesters(
	semesterID INT NOT NULL,
	primary key(semesterID)
);

CREATE TABLE classes(
	classID INT NOT NULL AUTO_INCREMENT,
	semesterID INT NOT NULL,
	courseID INT NOT NULL,
	primary key(classID)
);

CREATE table classLocations(
	classID INT NOT NULL,
	location varchar(60) NOT NULL,
	primary key(classID)
);

CREATE table classTeachers(
	classID INT NOT NULL,
	teacherEmail varchar(40) NOT NULL,
	primary key(classID)
);

CREATE table textbooks(
	classID INT NOT NULL,
	textBook varchar(60) NOT NULL
);

CREATE table classDays(
	dayID INT NOT NULL AUTO_INCREMENT,
	classID INT NOT NULL,
	weekday INT NOT NULL,
	startTime varchar(10) NOT NULL,
	endTime varchar(10) NOT NULL,
	primary key(dayID)
);

CREATE table classSize(
	classID INT NOT NULL,
	size INT NOT NULL,
	primary key(classID)
);

CREATE table classSpaceLeft(
	classID INT NOT NULL,
	spaceLeft INT NOT NULL,
	primary key(classID)

);

CREATE table classRoll(
	classID INT NOT NULL,
	studentID varchar(50) NOT NUll
);

CREATE table grades(
	classID INT NOT NULL,
	studentID varchar(50) NOT NULL,
	grade varchar(1) NOT NUll,
	primary key(classID, studentID)
);

CREATE table majors(
	majorID INT NOT NUll AUTO_INCREMENT,
	majorName varchar(50) NOT NULL,
	unitReq INT NOT NULL,
	primary key(majorID)
);

CREATE table major_courses(
	majorID INT NOT NULL,
	courseID INT NOT NULL,
	primary key (majorID,courseID)
);

CREATE table student_majors(
	email varchar(50) NOT NULL,
	majorID INT NOT NULL,
	primary key(email)
);

CREATE table gradStudents(
	email varchar(50),
	primary key (email)
);
ALTER TABLE semesters
ADD UNIQUE INDEX semest_pair_unique (year,term);

ALTER TABLE textbooks
ADD UNIQUE INDEX class_book_pair (classID, textBook);

ALTER TABLE classRoll
ADD UNIQUE INDEX class_student_unique (classID,studentID
);


# --- !Downs
drop table semesters;
drop table activesemesters;
drop table classes;
drop table classLocations;
drop table classTeachers;
drop table textbooks;
drop table classDays;
drop table classSize;
drop table classRoll;
drop table classSpaceLeft;
drop table grades;
drop table majors;
drop table major_courses;
drop table student_majors;
drop table gradStudents;