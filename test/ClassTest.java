import static org.junit.Assert.*;
import static play.test.Helpers.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Course;
import models.SchoolClass;
import models.SchoolClass.ScheduleItem;
import models.SchoolClass.Weekday;
import models.Semester;
import models.User;
import models.Semester.Term;
import models.User.UserType;

import org.junit.Test;

import JDBC.ClassDB;
import JDBC.UserDB;

public class ClassTest {

	// test add student to class. number of added students = class size.
	@Test
	public void addStudentsToClassFull() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();
					List<User> students = generateStudents(10);
					insertUsersIntoDatabase(students);
					int classID = singleClass(10).getID();
					SchoolClass cl = SchoolClass.searchByID(classID);

					for (User stud : students) {

						assertNotNull(UserDB.getUser(stud.email));
						assertTrue(SchoolClass.addStudentToClass(classID,
								stud.email));

					}

					cl = SchoolClass.searchByID(classID);
					assertEquals(0, cl.spaceLeft);
					userListEquals(students, SchoolClass.classRoll(classID));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test add student to class. number of added students < class size.
	@Test
	public void addStudentsToClassNotFull() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();
					List<User> students = generateStudents(10);
					insertUsersIntoDatabase(students);
					int classID = singleClass(11).getID();
					SchoolClass cl = SchoolClass.searchByID(classID);

					for (User stud : students) {
						assertNotNull(UserDB.getUser(stud.email));
						SchoolClass.addStudentToClass(classID, stud.email);

					}

					cl = SchoolClass.searchByID(classID);
					assertEquals(1, cl.spaceLeft);
					userListEquals(students, SchoolClass.classRoll(classID));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test add student to class. number of added students > class size.
	@Test
	public void addStudentsToClassOverLimit() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();
					List<User> students = generateStudents(12);
					insertUsersIntoDatabase(students);
					int classID = singleClass(9).getID();
					SchoolClass cl = SchoolClass.searchByID(classID);

					for (User stud : students) {
						assertNotNull(UserDB.getUser(stud.email));
						SchoolClass.addStudentToClass(classID, stud.email);

					}

					cl = SchoolClass.searchByID(classID);
					assertEquals(0, cl.spaceLeft);
					students.remove(students.size() - 1);
					students.remove(students.size() - 1);
					students.remove(students.size() - 1);
					userListEquals(students, SchoolClass.classRoll(classID));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test drop students.
	@Test
	public void dropStudents() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();
					List<User> students = generateStudents(10);
					insertUsersIntoDatabase(students);
					int classID = singleClass(11).getID();
					SchoolClass cl = SchoolClass.searchByID(classID);

					for (User stud : students) {
						// System.out.println(stud);
						assertNotNull(UserDB.getUser(stud.email));
						SchoolClass.addStudentToClass(classID, stud.email);

					}
					for (int i = 0; i < 3; i++) {
						SchoolClass.dropStudentFromClass(classID,
								students.get(0).email);
						students.remove(0);
					}

					cl = SchoolClass.searchByID(classID);
					assertEquals(4, cl.spaceLeft);
					userListEquals(students, SchoolClass.classRoll(classID));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test add textbooks to class.
	@Test
	public void testAddTextBooks() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();

					int classID = singleClass(1).getID();

					for (int i = 0; i < 5; i++) {
						SchoolClass.addTextBook(classID, "book" + i);
						;
					}

					SchoolClass cl = SchoolClass.searchByID(classID);
					assertEquals(5, cl.textbooks.size());
					Collections.sort(cl.textbooks);
					assertArrayEquals(("book0").toCharArray(), cl.textbooks
							.get(0).toCharArray());
					assertArrayEquals(("book1").toCharArray(), cl.textbooks
							.get(1).toCharArray());
					assertArrayEquals(("book2").toCharArray(), cl.textbooks
							.get(2).toCharArray());
					assertArrayEquals(("book3").toCharArray(), cl.textbooks
							.get(3).toCharArray());
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	@Test
	public void testAddAndRemoveBooks() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();

					int classID = singleClass(1).getID();

					for (int i = 0; i < 5; i++) {
						SchoolClass.addTextBook(classID, "book" + i);
						;
					}

					for (int i = 3; i < 5; i++) {
						SchoolClass.removeTextbook(classID, "book" + i);
						;
					}

					SchoolClass cl = SchoolClass.searchByID(classID);
					assertEquals(3, cl.textbooks.size());
					Collections.sort(cl.textbooks);
					assertArrayEquals(("book0").toCharArray(), cl.textbooks
							.get(0).toCharArray());
					assertArrayEquals(("book1").toCharArray(), cl.textbooks
							.get(1).toCharArray());
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test add schedule item to class.
	@Test
	public void testAddSchedule() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();

					int classID = singleClass(1).getID();

					ScheduleItem item1 = new ScheduleItem(Weekday.Monday,
							"10:00", "11:00");
					SchoolClass.insertScheduleItem(classID, item1);

					ScheduleItem item2 = new ScheduleItem(Weekday.Wednesday,
							"12:00", "13:00");
					SchoolClass.insertScheduleItem(classID, item2);

					ScheduleItem item3 = new ScheduleItem(Weekday.Friday,
							"2:00", "3:00");
					SchoolClass.insertScheduleItem(classID, item3);

					ScheduleItem item4 = new ScheduleItem(Weekday.Friday,
							"2:00", "3:01");
					SchoolClass cl = SchoolClass.searchByID(classID);

					assertEquals(3, cl.schedule.size());

					assertTrue(containsItem(cl.schedule, item1));
					assertTrue(containsItem(cl.schedule, item2));
					assertTrue(containsItem(cl.schedule, item3));

					assertFalse(containsItem(cl.schedule, item4));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	@Test
	public void testAddAndRemoveSchedule() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();

					int classID = singleClass(1).getID();

					ScheduleItem item1 = new ScheduleItem(Weekday.Monday,
							"10:00", "11:00");
					int id1 = SchoolClass.insertScheduleItem(classID, item1);

					ScheduleItem item2 = new ScheduleItem(Weekday.Wednesday,
							"12:00", "13:00");
					int id2 = SchoolClass.insertScheduleItem(classID, item2);

					ScheduleItem item3 = new ScheduleItem(Weekday.Friday,
							"2:00", "3:00");
					int id3 = SchoolClass.insertScheduleItem(classID, item3);

					SchoolClass.removeScheduleItem(id2);
					SchoolClass.removeScheduleItem(id3);
					SchoolClass cl = SchoolClass.searchByID(classID);
					assertEquals(1, cl.schedule.size());

					assertTrue(containsItem(cl.schedule, item1));

					assertFalse(containsItem(cl.schedule, item2));
				} catch (Exception e) {
					fail(e.getMessage());
				}

			}
		});
	}

	// test grade class functionality.
	@Test
	public void testClassGrade() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();
					List<User> students = generateStudents(10);
					insertUsersIntoDatabase(students);
					int classID = singleClass(10).getID();

					// add students to class.
					for (User stud : students) {
						SchoolClass.addStudentToClass(classID, stud.email);
					}

					Map<String, SchoolClass.Grade> grades = new HashMap();
					// create array of grades.
					SchoolClass.Grade[] randomGrades = { SchoolClass.Grade.A,
							SchoolClass.Grade.B, SchoolClass.Grade.F,
							SchoolClass.Grade.C };
					int i = 0;

					// assign grades to students.
					for (User stud : students) {
						String str = stud.email;
						grades.put(str, randomGrades[(i++)
								% randomGrades.length]);
					}

					// update grades.
					SchoolClass.updateGrades(classID, grades);

					SchoolClass cl = SchoolClass.searchByID(classID);
					Map<String, SchoolClass.Grade> gradesFromDb = SchoolClass
							.classGrades(classID);
					// check grades in database are same as assigned.
					for (User stud : students) {
						assertNotNull(gradesFromDb.get(stud.email));
						assertEquals(grades.get(stud.email),
								gradesFromDb.get(stud.email));
						;
					}
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// check that class grading is case insensitive.
	@Test
	public void testClassGradeUpperCaseEmails() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();
					List<User> students = generateStudents(10);
					insertUsersIntoDatabase(students);
					int classID = singleClass(10).getID();

					for (User stud : students) {

						SchoolClass.addStudentToClass(classID, stud.email);
					}

					Map<String, SchoolClass.Grade> grades = new HashMap();
					SchoolClass.Grade[] randomGrades = { SchoolClass.Grade.A,
							SchoolClass.Grade.B, SchoolClass.Grade.F,
							SchoolClass.Grade.C };
					int i = 0;
					for (User stud : students) {
						String str = stud.email;
						grades.put(str.toUpperCase(), randomGrades[(i++)
								% randomGrades.length]);
					}

					SchoolClass.updateGrades(classID, grades);

					SchoolClass cl = SchoolClass.searchByID(classID);
					Map<String, SchoolClass.Grade> gradesFromDb = SchoolClass
							.classGrades(classID);

					for (User stud : students) {
						// assertNotNull(gradesFromDb.get(stud.email));
						assertEquals(grades.get(stud.email.toUpperCase()),
								gradesFromDb.get(stud.email));
						;
					}
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test add location to class.
	@Test
	public void testAddLocation() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();

					int classID = singleClass(1).getID();

					SchoolClass.addOrUpdateLocation(classID, "Class 101");
					assertTrue("Class 101".equals(SchoolClass
							.searchByID(classID).location));

					SchoolClass.addOrUpdateLocation(classID, "Class 102");
					assertTrue("Class 102".equals(SchoolClass
							.searchByID(classID).location));

					SchoolClass.addOrUpdateLocation(classID, "Room 17");
					assertTrue("Room 17".equals(SchoolClass.searchByID(classID).location));

					SchoolClass.addOrUpdateLocation(classID, "Room 109");
					assertTrue("Room 109".equals(SchoolClass
							.searchByID(classID).location));

					SchoolClass.addOrUpdateLocation(classID, "");
					assertTrue("".equals(SchoolClass.searchByID(classID).location));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test add teacher to class.
	@Test
	public void testAddTeacher() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();
					List<User> teachers = generateTeachers(5);
					for (User teach : teachers) {
						assertTrue(teach.insertIntoDatabase());
					}
					int classID = singleClass(1).getID();

					for (User teach : teachers) {
						SchoolClass.addOrUpdateTeacher(classID, teach.email);
						String str = SchoolClass.searchByID(classID).teacher.email;

						assertTrue(teach.email.equals(str));
					}

				} catch (Exception e) {
					fail(e.getMessage());
				}

			}
		});
	}

	// test adding teacher using uppercase letter in email parameter.
	@Test
	public void testAddTeacherUpperCase() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					initCourses();
					List<User> teachers = generateTeachers(5);
					for (User teach : teachers) {
						assertTrue(teach.insertIntoDatabase());
					}
					int classID = singleClass(1).getID();

					for (User teach : teachers) {
						SchoolClass.addOrUpdateTeacher(classID,
								teach.email.toUpperCase());
						String str = SchoolClass.searchByID(classID).teacher.email;

						assertTrue(teach.email.equals(str));
					}

				} catch (Exception e) {
					fail(e.getMessage());
				}

			}
		});
	}

	// check that list of Schedule items contains target ScheduleItem object.
	static boolean containsItem(List<ScheduleItem> list, ScheduleItem target) {
		for (ScheduleItem item : list) {
			if (item.day == target.day
					&& item.startTime.equals(target.startTime)
					&& item.endTime.equals(target.endTime))
				return true;
		}
		return false;
	}

	// return true if two list of User objects are equal.
	static void userListEquals(List<User> expected, List<User> actual) {
		if (actual == expected)
			assert (true);
		if (actual == null)
			fail("actual is null, while expected is not null");
		if (expected == null)
			fail("expected is null while actual is not null");
		if (expected.size() != actual.size())
			fail("expected list size = " + expected.size() + " actual= "
					+ actual.size());
		Comparator<User> sortComparator = new Comparator<User>() {
			public int compare(User a, User b) {
				return a.email.compareTo(b.email);
			}
		};
		Collections.sort(expected, sortComparator);
		Collections.sort(actual, sortComparator);
		for (int i = 0; i < expected.size(); i++) {
			User a = expected.get(i);
			User b = actual.get(i);
			if (a.email.equals(b.email))
				;
		}
	}

	// creates a list of size n of User objects with usertype = student and
	// unique email addresses.
	static List<User> generateStudents(int n) {
		ArrayList<User> res = new ArrayList<>();
		String lastName = "lstnm";
		String firstName = "frstnm";
		String pass = "pass";
		UserType type = UserType.STUDENT;
		for (int i = 0; i < n; i++) {
			String email = "student" + i + "@mail.com";
			User student = new User(email, pass, type, firstName, lastName);
			res.add(student);
		}
		return res;
	}

	static List<User> generateTeachers(int n) {
		ArrayList<User> res = new ArrayList<>();
		String lastName = "lstnm";
		String firstName = "frstnm";
		String pass = "pass";
		UserType type = UserType.TEACHER;
		for (int i = 0; i < n; i++) {
			String email = "teacher" + i + "@mail.com";
			User student = new User(email, pass, type, firstName, lastName);
			res.add(student);
		}
		return res;
	}

	static void insertUsersIntoDatabase(List<User> users) throws Exception {
		for (User user : users) {

			assertTrue(user.insertIntoDatabase());
			assertNotNull(User.searchByEmail(user.email));
		}
	}

	public static void initCourses() throws Exception {

		Date date = new Date(System.currentTimeMillis());
		(new Semester(2015, Term.Spring, true, date, date))
				.insertIntoDatabase();
		(new Semester(2015, Term.Summer, true, date, date))
				.insertIntoDatabase();
		(new Semester(2015, Term.Winter, false, date, date))
				.insertIntoDatabase();
		(new Semester(2014, Term.Autumn, true, date, date))
				.insertIntoDatabase();
		(new Semester(2016, Term.Spring, false, date, date))
				.insertIntoDatabase();

		(new Course(101, "CPSC", "Java", 3)).insertIntoDatabase();
		(new Course(102, "CPSC", "C++", 3)).insertIntoDatabase();
		(new Course(203, "CPSC", "Algorithms", 3)).insertIntoDatabase();
		(new Course(305, "CPSC", "Design", 3)).insertIntoDatabase();

	}

	// create a single class.
	static SchoolClass singleClass(int size) {
		try {
			SchoolClass cl = new SchoolClass();
			cl.semester = new Semester(1);
			cl.course = new Course(1);
			cl.classSize = size;
			int id = cl.insertIntoDatabase();
			assertNotNull(SchoolClass.searchByID(id));
			return SchoolClass.searchByID(id);
		} catch (Exception e) {
			fail("could not add class to database" + e.getMessage());
			return null;
		}
	}

}
