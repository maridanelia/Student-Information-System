import static org.junit.Assert.*;
import static play.test.Helpers.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import models.Course;
import models.SchoolClass;
import models.Semester;
import models.Semester.Term;

import org.junit.Test;

import JDBC.CourseDB;
import JDBC.UserDB;

public class CourseTest {
	@Test
	public void testInsertForExceptions() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course course = new Course(103, "CPSC", "Java programming", 3);
				try {
					course.insertIntoDatabase();
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testGetAllForExceptions() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					List<Course> ls = Course.allCourses();
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test insert course into database.
	@Test
	public void testInsert() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert = new Course(103, "CPSC", "Java programming", 3);
				String message = "Introduction to Java programming";
				toInsert.description = message;
				try {
					toInsert.insertIntoDatabase();

					List<Course> allCourses = Course.allCourses();
					assertEquals(1, allCourses.size());

					Course fromDB = allCourses.get(0);

					assertEquals(toInsert.courseNumber, fromDB.courseNumber);
					assertEquals(toInsert.courseName, fromDB.courseName);
					assertEquals(toInsert.numberOfUnits, fromDB.numberOfUnits);
					assertEquals(toInsert.departmentID, fromDB.departmentID);
					assertEquals(message, fromDB.description);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// insert multiple courses into database.
	@Test
	public void testInsertMultiple() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert1 = new Course(103, "CPSC", "Java programming",
						3);
				Course toInsert2 = new Course(203, "CPSC",
						"Algorithmsand Data Structures", 4);
				Course toInsert3 = new Course(103, "PHYS", "Java programming",
						5);
				try {

					toInsert1.insertIntoDatabase();
					assertEquals(1, Course.allCourses().size());

					toInsert2.insertIntoDatabase();
					assertEquals(2, Course.allCourses().size());

					toInsert3.insertIntoDatabase();
					assertEquals(3, Course.allCourses().size());

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testGetCourseByCourseID() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert = new Course(103, "CPSC", "Java programming", 3);
				try {
					toInsert.insertIntoDatabase();

					Course fromDB = CourseDB.getCourse(toInsert.departmentID,
							toInsert.courseNumber);
					fromDB = CourseDB.getCourse(fromDB.getCourseID());
					assertNotNull(fromDB);
					assertEquals(toInsert.courseName, fromDB.courseName);
					assertEquals(toInsert.courseNumber, fromDB.courseNumber);
					assertEquals(toInsert.numberOfUnits, fromDB.numberOfUnits);
					assertEquals(toInsert.departmentID, fromDB.departmentID);
					assertNull(fromDB.description);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testInsertAndRetrive1() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert = new Course(103, "CPSC", "Java programming", 3);
				try {
					toInsert.insertIntoDatabase();

					Course fromDB = CourseDB.getCourse(toInsert.departmentID,
							toInsert.courseNumber);

					assertNotNull(fromDB);
					assertEquals(toInsert.courseName, fromDB.courseName);
					assertEquals(toInsert.courseNumber, fromDB.courseNumber);
					assertEquals(toInsert.numberOfUnits, fromDB.numberOfUnits);
					assertEquals(toInsert.departmentID, fromDB.departmentID);
					assertNull(fromDB.description);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test insert and CourseDB.getCourse(String dept, String number). use some
	// Lowercase letters in "dept" string for getCourse, and only uppercase
	// letters in insert to verify that getCourse is caseinsensitive.
	@Test
	public void testInsertAndRetriveLowerCase() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert = new Course(103, "CPSC", "Java programming", 3);
				String message = "Introduction to Java programming";
				toInsert.description = message;
				try {
					toInsert.insertIntoDatabase();

					Course fromDB = CourseDB.getCourse("CpSC",
							toInsert.courseNumber);

					assertNotNull(fromDB);
					assertEquals(toInsert.courseName, fromDB.courseName);
					assertEquals(toInsert.courseNumber, fromDB.courseNumber);
					assertEquals(toInsert.numberOfUnits, fromDB.numberOfUnits);
					assertEquals(toInsert.departmentID, fromDB.departmentID);
					assertEquals(message, fromDB.description);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test insert and CourseDB.getCourse(String dept, String number). use only
	// uppercase letters in "dept" string for getCourse, and some lowercase
	// letters in insert to verify that getCourse is caseinsensitive.
	@Test
	public void testInsertAndRetriveLowerCase1() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert = new Course(103, "CpSC", "Java programming", 3);
				try {
					toInsert.insertIntoDatabase();

					Course fromDB = CourseDB.getCourse("CPSC",
							toInsert.courseNumber);

					assertNotNull(fromDB);
					assertEquals(toInsert.courseName, fromDB.courseName);
					assertEquals(toInsert.courseNumber, fromDB.courseNumber);
					assertEquals(toInsert.numberOfUnits, fromDB.numberOfUnits);
					assertEquals(toInsert.departmentID.toUpperCase(),
							fromDB.departmentID);

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// try to get non-existent course from database.
	@Test
	public void testRetreiveNonExistent() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert = new Course(103, "CPSC", "Java programming", 3);
				try {
					toInsert.insertIntoDatabase();

					assertNull(CourseDB.getCourse("CPSC", 104));

					assertNull(CourseDB.getCourse("ART", 103));
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test insert course in database. Check correctness of description
	// parameter.
	@Test
	public void testInsertWithDescription() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert = new Course(103, "CPSC", "Java programming", 3);
				String message = "this is introductory course to java";
				toInsert.description = message;
				try {
					toInsert.insertIntoDatabase();

					List<Course> allCourses = Course.allCourses();
					assertEquals(1, allCourses.size());

					Course fromDB = allCourses.get(0);
					assertEquals(toInsert.description, fromDB.description);

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test modify course description.
	@Test
	public void modifyDescription() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert = new Course(103, "CPSC", "Java programming", 3);
				String message = "this is introductory course to java";
				toInsert.description = message;
				try {
					toInsert.insertIntoDatabase();
					int id = Course.findCourse("CPSC", 103).getCourseID();
					Course.updateDescription(id, "New Description");
					List<Course> allCourses = Course.allCourses();
					assertEquals(1, allCourses.size());

					Course fromDB = allCourses.get(0);
					assertEquals("New Description", fromDB.description);

					fromDB = Course.findCourse("CPSC", 103);
					assertEquals("New Description", fromDB.description);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test modify units methods.
	@Test
	public void testModifyUnits() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					(new Course(101, "CPSC", "Introduction to Programming", 5))
							.insertIntoDatabase();
					(new Course(102, "CPSC", "Programming Concepts", 5))
							.insertIntoDatabase();
					(new Course(101, "ACCT", "Accounting", 5))
							.insertIntoDatabase();

					int id1 = Course.findCourse("CPSC", 101).getCourseID();
					int id2 = Course.findCourse("CPSC", 102).getCourseID();
					int id3 = Course.findCourse("ACCT", 101).getCourseID();

					Course.updateUnits(id1, 3);
					assertEquals(3, Course.findCourse(id1).numberOfUnits);
					assertEquals(5, Course.findCourse(id2).numberOfUnits);
					assertEquals(5, Course.findCourse(id3).numberOfUnits);

					Course.updateUnits(id1, 4);
					assertEquals(4, Course.findCourse(id1).numberOfUnits);
					assertEquals(5, Course.findCourse(id2).numberOfUnits);
					assertEquals(5, Course.findCourse(id3).numberOfUnits);

					Course.updateUnits(id1, 1);
					assertEquals(1, Course.findCourse(id1).numberOfUnits);
					assertEquals(5, Course.findCourse(id2).numberOfUnits);
					assertEquals(5, Course.findCourse(id3).numberOfUnits);
				} catch (Exception e) {
					// e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
	}

	// add prerequisites to course, then retrieve all prerequisites of that
	// course. Test that inserted and retrieved valies match.
	@Test
	public void addPrerequisitesGetAll() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Course toInsert1 = new Course(103, "CPSC", "Java programming",
						3);
				Course toInsert2 = new Course(203, "CPSC",
						"Algorithmsand Data Structures", 4);
				Course toInsert3 = new Course(204, "CPSC",
						"Software Architecture", 5);
				try {

					toInsert1.insertIntoDatabase();
					toInsert2.insertIntoDatabase();
					toInsert3.insertIntoDatabase();

					Course fromDB = Course.findCourse("CPsC", 204);
					int id = fromDB.getCourseID();
					assertNotNull(fromDB);
					Course.addPrerequisiteToDatabase(id, "CpSc", 203);

					fromDB = Course.findCourse(id);
					assertEquals(1, fromDB.prerequisites.size());

					Course prereq = fromDB.prerequisites.get(0);
					int prereqID = CourseDB.getCourse("cpsc", 203)
							.getCourseID();

					assertEquals("CPSC", prereq.departmentID);
					assertEquals(203, prereq.courseNumber);
					assertEquals(prereqID, prereq.getCourseID());

					Course.addPrerequisiteToDatabase(id, "CPsC", 103);

					fromDB = Course.findCourse(id);

					assertEquals(2, fromDB.prerequisites.size());

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test removing prerequisites.
	@Test
	public void testRemovePrerequisite() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					(new Course(101, "CPSC", "Introduction to Programming", 5))
							.insertIntoDatabase();
					(new Course(102, "CPSC", "Programming Concepts", 5))
							.insertIntoDatabase();
					(new Course(301, "CPSC", "Accounting", 5))
							.insertIntoDatabase();

					int id1 = Course.findCourse("CPSC", 101).getCourseID();
					int id2 = Course.findCourse("CPSC", 102).getCourseID();
					int id3 = Course.findCourse("CPSC", 301).getCourseID();

					Course.addPrerequisiteToDatabase(id1, "CPSC", 102);
					Course.addPrerequisiteToDatabase(id1, "CPSC", 301);

					Course course = Course.findCourse(id1);

					assertTrue(containsCourse(course.prerequisites, "CPSC", 102));
					assertTrue(containsCourse(course.prerequisites, "CPSC", 301));

					Course.removePrerequisite(id1, id3);

					course = Course.findCourse(id1);
					assertEquals(1, course.prerequisites.size());
					assertFalse(containsCourse(course.prerequisites, "CPSC",
							301));
					assertTrue(containsCourse(course.prerequisites, "CPSC", 102));

					Course.removePrerequisite(id1, id2);
					course = Course.findCourse(id1);
					assertEquals(0, course.prerequisites.size());
					assertFalse(containsCourse(course.prerequisites, "CPSC",
							301));
					assertFalse(containsCourse(course.prerequisites, "CPSC",
							102));

				} catch (Exception e) {

					fail(e.getMessage());
				}
			}
		});
	}

	// test get all courses from database.
	@Test
	public void testGetAllCourses() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					insertManyCourses();
					List<Course> courses = Course.allCourses();
					assertTrue(containsCourse(courses, "CPSC", 101));
					assertTrue(containsCourse(courses, "CPSC", 102));
					assertTrue(containsCourse(courses, "CPSC", 103));
					assertTrue(containsCourse(courses, "CPSC", 104));
					assertTrue(containsCourse(courses, "CPSC", 105));

					assertTrue(containsCourse(courses, "ACCT", 404));
					assertTrue(containsCourse(courses, "ACCT", 201));
					assertTrue(containsCourse(courses, "ACCT", 202));

					assertFalse(containsCourse(courses, "CPSC", 111));
					assertFalse(containsCourse(courses, "CPSC", 121));
					assertFalse(containsCourse(courses, "PHYS", 101));
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testDeleteCourse() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					(new Course(101, "CPSC", "Introduction to Programming", 5))
							.insertIntoDatabase();
					(new Course(102, "CPSC", "Programming Concepts", 5))
							.insertIntoDatabase();
					(new Course(301, "CPSC", "Accounting", 5))
							.insertIntoDatabase();

					int id1 = Course.findCourse("CPSC", 101).getCourseID();
					int id2 = Course.findCourse("CPSC", 102).getCourseID();
					int id3 = Course.findCourse("CPSC", 301).getCourseID();

					Course.deleteCourse(id1);
					List<Course> courses = Course.allCourses();

					assertEquals(2, courses.size());
					assertTrue(containsCourse(courses, "CPSC", 102));
					assertFalse(containsCourse(courses, "CPSC", 101));

					assertNull(Course.findCourse(id1));
					assertNull(Course.findCourse("CPSC", 101));

					assertNotNull(Course.findCourse(id2));
					assertNotNull(Course.findCourse(id3));
				} catch (Exception e) {

					fail(e.getMessage());
				}
			}
		});
	}

	// try deleting course that has classes assigned.
	@Test
	public void testDeleteCourseWithClass() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					(new Course(101, "CPSC", "Introduction to Programming", 5))
							.insertIntoDatabase();
					(new Semester(2015, Term.Spring, true, new Date(System
							.currentTimeMillis()), new Date(System
							.currentTimeMillis()))).insertIntoDatabase();

					SchoolClass cl = new SchoolClass();
					cl.semester = new Semester(1);
					cl.course = new Course(1);
					cl.classSize = 10;
					cl.insertIntoDatabase();

					int id1 = Course.findCourse("CPSC", 101).getCourseID();

					Course.deleteCourse(id1);

					fail("Exception not thrown");
				} catch (IllegalArgumentException e) {
					assert (true);
				} catch (Exception e) {

					fail(e.getMessage());
				}
			}
		});
	}

	// test inserting several courses and check if retrieving all courses works
	// correctly. test that insert is case insensitive in terms of department id
	// string.
	@Test
	public void testGetAllCoursesCaseSensitive() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					// insert course with department ids specified with strings
					// that have mix of upper and lowercase characters.
					insertManyCoursesDifferentCase();
					List<Course> courses = Course.allCourses();
					assertTrue(containsCourse(courses, "CPSC", 101));
					assertTrue(containsCourse(courses, "CPSC", 102));
					assertTrue(containsCourse(courses, "CPSC", 103));
					assertTrue(containsCourse(courses, "CPSC", 104));
					assertTrue(containsCourse(courses, "CPSC", 105));

					assertTrue(containsCourse(courses, "ACCT", 404));
					assertTrue(containsCourse(courses, "ACCT", 201));
					assertTrue(containsCourse(courses, "ACCT", 202));

					assertFalse(containsCourse(courses, "CPSC", 111));
					assertFalse(containsCourse(courses, "CPSC", 121));
					assertFalse(containsCourse(courses, "PHYS", 101));
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test search by department.
	@Test
	public void testSearchByDept() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					insertManyCourses();
					// list of all courses with specified department.
					List<Course> courses = Course.coursesByDept("cPsC");

					assertTrue(containsCourse(courses, "CPSC", 101));
					assertTrue(containsCourse(courses, "CPSC", 102));
					assertTrue(containsCourse(courses, "CPSC", 103));
					assertTrue(containsCourse(courses, "CPSC", 104));
					assertTrue(containsCourse(courses, "CPSC", 105));

					assertFalse(containsCourse(courses, "ACCT", 404));
					assertFalse(containsCourse(courses, "ACCT", 201));
					assertFalse(containsCourse(courses, "ACCT", 202));

					assertFalse(containsCourse(courses, "CPSC", 111));
					assertFalse(containsCourse(courses, "CPSC", 121));
					assertFalse(containsCourse(courses, "PHYS", 101));
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test inserting same course twise.
	@Test
	public void testInsertTwise() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					(new Course(101, "CPSC", "Introduction to Programming", 5))
							.insertIntoDatabase();
					(new Course(101, "CPSC", "Introduction to Programming", 5))
							.insertIntoDatabase();
					fail("Exception not thrown when expected");
				} catch (Exception e) {
					assert (true);
				}
			}
		});
	}

	// add several differenc courses to database.
	private static void insertManyCourses() throws Exception {
		(new Course(101, "CPSC", "Introduction to Programming", 5))
				.insertIntoDatabase();
		(new Course(102, "CPSC", "Programming Concepts", 5))
				.insertIntoDatabase();
		(new Course(103, "CPSC", "Data Structures and Algorithms", 5))
				.insertIntoDatabase();
		(new Course(104, "CPSC", "Java Programming", 5)).insertIntoDatabase();
		(new Course(105, "CPSC", "Python Porgramming", 5)).insertIntoDatabase();
		(new Course(106, "CPSC", "C++ Programming", 5)).insertIntoDatabase();

		(new Course(201, "ACCT", "Financial Accounting", 5))
				.insertIntoDatabase();
		(new Course(202, "ACCT", "Managerial Accounting", 5))
				.insertIntoDatabase();
		(new Course(303, "ACCT", "Advanced Accounting", 5))
				.insertIntoDatabase();
		(new Course(404, "ACCT", "Auditing", 5)).insertIntoDatabase();
		(new Course(101, "ACCT", "Valuation Concepts", 5)).insertIntoDatabase();
		(new Course(105, "ACCT", "Valuation Concepts", 5)).insertIntoDatabase();
		(new Course(305, "ACCT", "Valuation Concepts", 5)).insertIntoDatabase();
	}

	// add several different course to database. departments id strings are
	// specified with mix of lower and uppercase characters to test for case
	// sensitivity.
	private static void insertManyCoursesDifferentCase() throws Exception {
		(new Course(101, "CpsC", "Introduction to Programming", 5))
				.insertIntoDatabase();
		(new Course(102, "CPsC", "Programming Concepts", 5))
				.insertIntoDatabase();
		(new Course(103, "cpsc", "Data Structures and Algorithms", 5))
				.insertIntoDatabase();
		(new Course(104, "CPsC", "Java Programming", 5)).insertIntoDatabase();
		(new Course(105, "cpSc", "Python Porgramming", 5)).insertIntoDatabase();
		(new Course(106, "CPSC", "C++ Programming", 5)).insertIntoDatabase();

		(new Course(201, "ACcT", "Financial Accounting", 5))
				.insertIntoDatabase();
		(new Course(202, "AccT", "Managerial Accounting", 5))
				.insertIntoDatabase();
		(new Course(303, "ACCT", "Advanced Accounting", 5))
				.insertIntoDatabase();
		(new Course(404, "ACCt", "Auditing", 5)).insertIntoDatabase();
		(new Course(205, "aCCT", "Valuation Concepts", 5)).insertIntoDatabase();
	}

	private static boolean containsCourse(List<Course> courses, String dept,
			int number) {
		for (Course course : courses) {
			if (course.departmentID != null && course.departmentID.equals(dept)
					&& course.courseNumber == number)
				return true;
		}
		return false;
	}
}
