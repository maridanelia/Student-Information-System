# Departments schema
# Department names and IDs are taken from California State #University Fullerton course catalog: #http://catalog.fullerton.edu/content.php?catoid=1&catoid=1#&navoid=74&filter%5Bitem_type%5D=3&filter%5Bonly_active%5D=1#&filter%5B3%5D=1&filter%5Bcpage%5D=1#acalog_template_course_filter 


# --- !Ups
 

insert into departments values('ACCT', 'Accounting');
insert into departments values('AFAM', 'African American Studies');
insert into departments values('AMST', 'American Studies');
insert into departments values('ANTH', 'Anthropology');
insert into departments values('ART', 'Art');
insert into departments values('CHEM', 'Chemistry and Biochemistry');
insert into departments values('CPSC', 'Computer Science');
insert into departments values('MATH', 'Mathematics');
insert into departments values('PHYS', 'Physics');

 
# --- !Downs
remove from departments;
