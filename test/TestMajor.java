import static org.junit.Assert.*;
import static play.test.Helpers.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Date;
import java.util.Set;

import models.Course;
import models.Major;
import models.SchoolClass;
import models.SchoolClass.Grade;
import models.Semester;
import models.Semester.SemesterStatus;
import models.Semester.Term;
import models.StudentProfile;
import models.User;
import models.User.UserType;

import org.junit.Before;
import org.junit.Test;

import JDBC.ClassDB;
import JDBC.CourseDB;
import JDBC.MajorDB;
import JDBC.UserDB;

public class TestMajor {
	@Test
	public void addMajorSingle() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Major major = new Major();
				major.name = "Computer Science";
				major.unitRequirements = 60;
				try {
					major.insertIntoDatabase();
					List<Major> fromDb = MajorDB.getAllMajors();
					assertEquals(1, fromDb.size());
					assertEquals(60, fromDb.get(0).unitRequirements);
					assertNotNull(fromDb.get(0).name);
					assertTrue(fromDb.get(0).name.equals("Computer Science"));
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void addMajorSeveral() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					create("Computer Science", 60).insertIntoDatabase();
					assertEquals(1, MajorDB.getAllMajors().size());
					create("Business Administration", 50).insertIntoDatabase();
					assertEquals(2, MajorDB.getAllMajors().size());
					create("Physics", 30).insertIntoDatabase();
					assertEquals(3, MajorDB.getAllMajors().size());
					//
					List<Major> fromDb = MajorDB.getAllMajors();
					// System.out.println(fromDb.toString());
					for (Major mj : fromDb) {
						assertNotNull(mj.name);
					}

					assertTrue(containsMajor(fromDb,
							create("Computer Science", 60)));
					assertTrue(containsMajor(fromDb,
							create("Business Administration", 50)));
					assertTrue(containsMajor(fromDb, create("Physics", 30)));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
	}

	// test get major with ID.
	@Test
	public void testGetById() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					int id1 = create("Computer Science", 60)
							.insertIntoDatabase();
					int id2 = create("Business Administration", 50)
							.insertIntoDatabase();
					int id3 = create("Physics", 30).insertIntoDatabase();

					assertTrue(majorsEqual(MajorDB.getMajorByID(id1),
							create("Computer Science", 60)));
					assertTrue(majorsEqual(MajorDB.getMajorByID(id2),
							create("Business Administration", 50)));
					assertTrue(majorsEqual(MajorDB.getMajorByID(id3),
							create("Physics", 30)));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
	}

	// test remove major by inserting majors and then removing all majors from
	// database.
	@Test
	public void testRemoveAll() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					Major maj1 = create("Computer Science", 60);
					Major maj2 = create("Business Administration", 50);
					Major maj3 = create("Physics", 30);

					int id1 = maj1.insertIntoDatabase();
					int id2 = maj2.insertIntoDatabase();
					int id3 = maj3.insertIntoDatabase();

					assertTrue(MajorDB.removeMajor(id1));

					List<Major> fromDB = MajorDB.getAllMajors();
					assertEquals(2, fromDB.size());
					assertFalse(containsMajor(fromDB, maj1));
					assertTrue(containsMajor(fromDB, maj2));
					assertTrue(containsMajor(fromDB, maj3));
					assertNull(MajorDB.getMajorByID(id1));

					assertTrue(MajorDB.removeMajor(id2));
					assertTrue(MajorDB.removeMajor(id3));

					assertEquals(0, MajorDB.getAllMajors().size());

					// create("Physics", 30)));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
	}

	// try to remove major using a nonexisting ID.
	@Test
	public void testRemoveInvalidID() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					Major maj1 = create("Computer Science", 60);
					Major maj2 = create("Business Administration", 50);
					Major maj3 = create("Physics", 30);

					int id1 = maj1.insertIntoDatabase();
					int id2 = maj2.insertIntoDatabase();
					int id3 = maj3.insertIntoDatabase();

					// check that id-s are positive to be sure that
					// id1+id2+id3+1 does not equal to any of the ids.
					assertTrue(id1 > 0 && id2 > 0 && id3 > 0);
					assertFalse(MajorDB.removeMajor(id1 + id2 + id3 + 1));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
	}

	// add single course requirement to major.
	@Test
	public void addCourseRequirementSingle() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					TestHelpers.addCoursesSmall();
					Major maj = create("Computer Science", 60);
					int majID = maj.insertIntoDatabase();

					assertTrue(MajorDB.addCourseRequirement(majID, 1));

					List<Course> fromDB = MajorDB.getCourseRequirements(majID);

					assertEquals(1, fromDB.size());

					Course exp = CourseDB.getCourse(1);
					Course act = fromDB.get(0);

					assertCoursesEqual(exp, act);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// add several course requirements to major.
	@Test
	public void addCourseRequirementsSeveral() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					TestHelpers.addCoursesSmall();
					Major maj = create("Computer Science", 60);
					int majID = maj.insertIntoDatabase();

					assertTrue(MajorDB.addCourseRequirement(majID, 1));
					List<Course> fromDB = MajorDB.getCourseRequirements(majID);
					assertEquals(1, fromDB.size());

					assertTrue(MajorDB.addCourseRequirement(majID, 2));
					fromDB = MajorDB.getCourseRequirements(majID);
					assertEquals(2, fromDB.size());

					assertTrue(MajorDB.addCourseRequirement(majID, 3));
					fromDB = MajorDB.getCourseRequirements(majID);
					assertEquals(3, fromDB.size());

					for (Course course : fromDB) {
						assertCoursesEqual(
								CourseDB.getCourse(course.getCourseID()),
								course);
					}
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// try to add duplicate course requirements to major.
	@Test
	public void addDupblicateRequirements() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					TestHelpers.addCoursesSmall();
					Major maj = create("Computer Science", 60);
					int majID = maj.insertIntoDatabase();

					assertTrue(MajorDB.addCourseRequirement(majID, 1));
					List<Course> fromDB = MajorDB.getCourseRequirements(majID);
					assertEquals(1, fromDB.size());

					assertFalse(MajorDB.addCourseRequirement(majID, 1));
					fromDB = MajorDB.getCourseRequirements(majID);
					assertEquals(1, fromDB.size());
					System.out.println(fromDB);

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test by adding requirements to different majors.
	@Test
	public void addRequirementsTwoMajors() {

		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					TestHelpers.addCoursesSmall();
					Major maj1 = create("major1", 60);
					int majID1 = maj1.insertIntoDatabase();

					Major maj2 = create("major2", 60);
					int majID2 = maj2.insertIntoDatabase();

					assertTrue(MajorDB.addCourseRequirement(majID1, 1));
					assertTrue(MajorDB.addCourseRequirement(majID1, 2));
					assertTrue(MajorDB.addCourseRequirement(majID1, 3));

					assertTrue(MajorDB.addCourseRequirement(majID2, 3));
					assertTrue(MajorDB.addCourseRequirement(majID2, 4));

					List<Course> reqs1 = MajorDB.getCourseRequirements(majID1);
					List<Course> reqs2 = MajorDB.getCourseRequirements(majID2);

					assertEquals(3, reqs1.size());
					assertEquals(2, reqs2.size());

					assertTrue(containsCourse(reqs1, 1));
					assertTrue(containsCourse(reqs1, 2));
					assertTrue(containsCourse(reqs1, 3));

					assertTrue(containsCourse(reqs2, 3));
					assertTrue(containsCourse(reqs2, 4));

					assertCoursesEqual(
							CourseDB.getCourse(reqs1.get(0).getCourseID()),
							reqs1.get(0));

					assertCoursesEqual(
							CourseDB.getCourse(reqs2.get(0).getCourseID()),
							reqs2.get(0));

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});

	}

	// test removing course requirements.
	@Test
	public void removeRequirements() {

		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					TestHelpers.addCoursesSmall();
					Major maj1 = create("major1", 60);
					int majID1 = maj1.insertIntoDatabase();

					Major maj2 = create("major2", 60);
					int majID2 = maj2.insertIntoDatabase();

					assertTrue(MajorDB.addCourseRequirement(majID1, 1));
					assertTrue(MajorDB.addCourseRequirement(majID1, 2));
					assertTrue(MajorDB.addCourseRequirement(majID1, 3));

					assertTrue(MajorDB.addCourseRequirement(majID2, 3));
					assertTrue(MajorDB.addCourseRequirement(majID2, 4));

					assertTrue(MajorDB.removeCourseRequirement(majID1, 2));
					assertTrue(MajorDB.removeCourseRequirement(majID2, 4));
					//
					List<Course> reqs1 = MajorDB.getCourseRequirements(majID1);
					List<Course> reqs2 = MajorDB.getCourseRequirements(majID2);

					assertEquals(2, reqs1.size());
					assertEquals(1, reqs2.size());

					assertTrue(containsCourse(reqs1, 1));
					assertTrue(containsCourse(reqs1, 3));
					assertTrue(containsCourse(reqs2, 3));

					assertCoursesEqual(
							CourseDB.getCourse(reqs1.get(0).getCourseID()),
							reqs1.get(0));
					assertCoursesEqual(
							CourseDB.getCourse(reqs2.get(0).getCourseID()),
							reqs2.get(0));

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});

	}

	// test assigning major to student.
	@Test
	public void testStudentMajor() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					TestHelpers.addCoursesSmall();
					Major maj1 = create("major1", 60);
					int majID1 = maj1.insertIntoDatabase();

					Major maj2 = create("major2", 60);
					int majID2 = maj2.insertIntoDatabase();

					(new User("stud1@mail.com", "pass", UserType.STUDENT,
							"name", "name")).insertIntoDatabase();
					(new User("stud2@mail.com", "pass", UserType.STUDENT,
							"name", "name")).insertIntoDatabase();

					MajorDB.addStudentToMajor("stud1@mail.com", majID1);
					assertTrue(majorsEqual(maj1,
							MajorDB.getStudentMajor("stud1@mail.com")));

					MajorDB.addStudentToMajor("stud1@mail.com", majID2);
					assertTrue(majorsEqual(maj2,
							MajorDB.getStudentMajor("stud1@mail.com")));

					assertNull(MajorDB.getStudentMajor("stud2@mail.com"));
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	private boolean containsCourse(List<Course> courses, int courseID) {
		for (Course course : courses) {
			if (course.getCourseID() == courseID)
				return true;
		}
		return false;
	}

	private static void assertCoursesEqual(Course exp, Course act) {

		assertEquals(exp.getCourseID(), act.getCourseID());
		assertEquals(exp.courseNumber, act.courseNumber);
		assertEquals(exp.numberOfUnits, act.numberOfUnits);

		assertNotNull(act.courseName);
		assertNotNull(act.departmentID);

		assertTrue(act.courseName.equals(exp.courseName));
		assertTrue(act.departmentID.equals(exp.departmentID));
	}

	Major create(String name, int units) {
		Major res = new Major();
		res.unitRequirements = units;
		res.name = name;
		return res;
	}

	private boolean containsMajor(List<Major> majors, Major target) {
		for (Major mj : majors) {
			if (majorsEqual(target, mj))
				return true;
		}

		return false;
	}

	private static boolean majorsEqual(Major mj1, Major mj2) {
		return (mj1.unitRequirements == mj2.unitRequirements && mj1.name
				.equals(mj2.name));

	}

}
