# Users schema
 
# --- !Ups
insert into user values('admin@mail.com', 'mariam','admin',3,'5b56611c37473f3f67e25b6c8331b7e1a7128'); 

insert into user values('teacher@mail.com', 'tom','teacher',1,'5b56611c37473f3f67e25b6c8331b7e1a7128'); 

insert into user values('student@mail.com', 'jerry','student',2,'5b56611c37473f3f67e25b6c8331b7e1a7128');

 
# --- !Downs
 
Remove from user;