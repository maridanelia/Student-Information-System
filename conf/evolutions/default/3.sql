# Departments schema
# Department names and IDs are taken from California State #University Fullerton course catalog: #http://catalog.fullerton.edu/content.php?catoid=1&catoid=1#&navoid=74&filter%5Bitem_type%5D=3&filter%5Bonly_active%5D=1#&filter%5B3%5D=1&filter%5Bcpage%5D=1#acalog_template_course_filter 


# --- !Ups
 
CREATE TABLE departments (
  departmentID VARCHAR(4) NOT NULL,
  departmentName VARCHAR(60) NOT NULL,
PRIMARY KEY (departmentID)
);

 
# --- !Downs
DROP TABLE departments;




