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
import models.SchoolClass;
import models.SchoolClass.Grade;
import models.Semester;
import models.Semester.SemesterStatus;
import models.Semester.Term;
import models.StudentProfile;

import org.junit.Before;
import org.junit.Test;

import JDBC.ClassDB;
import JDBC.CourseDB;
import JDBC.UserDB;

public class TestProfile {

	private static String studentEmail = "student@mail.com";
	private static String dept = "CPSC";

	private static void prepareCourses() {

		try {
			TestHelpers.addCoursesSmall();
			TestHelpers.addSemesters();
			TestHelpers.createClasses();
		} catch (Exception e) {
			fail("could not initialize classes: " + e.getMessage());
		}
	}

	private void enrollOneClass(int number, int semesterID) {
		try {
			List<SchoolClass> classes = ClassDB.searchBySemesterAndDeptPart(
					semesterID, dept);

			for (SchoolClass cl : classes) {
				if (cl.course.courseNumber == number) {
					ClassDB.addStudentToClass(cl.getID(), studentEmail);
					return;
				}
			}
		} catch (Exception e) {
			fail("could not enroll " + number + " " + e.getMessage());
		}
	}

	private void enrollSeveral() {
		try {
			// CPSC101, summer2015
			enrollOneClass(101, 1);

			// CPSC102 autumn2015
			enrollOneClass(102, 2);

			// CPSC103 spring2015
			enrollOneClass(103, 3);

			// CPSC201 summer2016
			enrollOneClass(201, 4);

		} catch (Exception e) {
			fail("could not enroll in class");
		}
	}

	@Test
	public void currentClassesTest() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {
					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					List<SchoolClass> classes = profile.currentClasses();
					assertEquals(1, classes.size());

					SchoolClass cl1 = classes.get(0);

					assertNotNull(cl1);
					assertEquals(5, cl1.course.numberOfUnits);
					assertEquals(103, cl1.course.courseNumber);
					assertTrue(cl1.course.departmentID.equals("CPSC"));
					assertEquals(2016, cl1.semester.year);
					assertEquals(Term.Spring, cl1.semester.term);
					assertTrue(cl1.semester.getStatus() == SemesterStatus.InProgresss);

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	public void allClassesTakenSeveralClasses() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {
					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					List<SchoolClass> classes = profile.getAllClasses();
					assertEquals(4, classes.size());

					SchoolClass cl1 = getFromList(classes, 201);

					assertNotNull(cl1);
					assertEquals(4, cl1.course.numberOfUnits);
					assertEquals(201, cl1.course.courseNumber);
					assertTrue(cl1.course.departmentID.equals("CPSC"));
					assertEquals(2016, cl1.semester.year);
					assertEquals(Term.Summer, cl1.semester.term);
					assertTrue(cl1.semester.getStatus() == SemesterStatus.Future);

					SchoolClass cl2 = getFromList(classes, 103);

					assertNotNull(cl2);
					assertEquals(5, cl2.course.numberOfUnits);
					assertEquals(103, cl2.course.courseNumber);
					assertTrue(cl2.course.departmentID.equals("CPSC"));
					assertEquals(2016, cl2.semester.year);
					assertEquals(Term.Spring, cl2.semester.term);
					assertTrue(cl2.semester.getStatus() == SemesterStatus.InProgresss);

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void allClassesTakenSingleClasses() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				// CPSC201 summer2016
				enrollOneClass(201, 4);
				try {
					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					List<SchoolClass> classes = profile.getAllClasses();
					assertEquals(1, classes.size());

					SchoolClass cl1 = classes.get(0);

					assertNotNull(cl1);
					assertEquals(4, cl1.course.numberOfUnits);
					assertEquals(201, cl1.course.courseNumber);
					assertTrue(cl1.course.departmentID.equals("CPSC"));
					assertEquals(2016, cl1.semester.year);
					assertEquals(Term.Summer, cl1.semester.term);
					assertTrue(cl1.semester.getStatus() == SemesterStatus.Future);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testUnitsNoGrades() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {
					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);

					assertEquals(0, profile.getUnitsCompleted());
					assertEquals(16, profile.getUnitsTaken());
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testUnitsFailedGrades() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);

					List<SchoolClass> classes = profile.getAllClasses();

					for (SchoolClass cl : classes) {
						updateGrade(cl, Grade.F);
					}

					assertEquals(0, profile.getUnitsCompleted());
					assertEquals(16, profile.getUnitsTaken());
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testUnitsNoFailed() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);

					List<SchoolClass> classes = profile.getAllClasses();

					for (SchoolClass cl : classes) {
						updateGrade(cl, Grade.A);
					}

					assertEquals(16, profile.getUnitsCompleted());
					assertEquals(16, profile.getUnitsTaken());
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testUnitsPartFailed() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);

					List<SchoolClass> classes = profile.getAllClasses();
					assertNotNull(classes);
					updateGrade(getFromList(classes, 101), Grade.A);
					updateGrade(getFromList(classes, 102), Grade.B);
					updateGrade(getFromList(classes, 103), Grade.F);
					updateGrade(getFromList(classes, 201), Grade.B);

					assertEquals(11, profile.getUnitsCompleted());
					assertEquals(16, profile.getUnitsTaken());
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testGPAAllA() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);

					List<SchoolClass> classes = profile.getAllClasses();
					assertNotNull(classes);
					updateGrade(getFromList(classes, 101), Grade.A);
					updateGrade(getFromList(classes, 102), Grade.A);
					updateGrade(getFromList(classes, 103), Grade.A);
					updateGrade(getFromList(classes, 201), Grade.A);

					assertEquals(4, profile.getGPA(), 0.001);
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testSomeNotgraded() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);

					List<SchoolClass> classes = profile.getAllClasses();
					assertNotNull(classes);
					updateGrade(getFromList(classes, 101), Grade.A);
					updateGrade(getFromList(classes, 102), Grade.A);

					assertEquals(4, profile.getGPA(), 0.001);
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testGPANothingGraded() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);

					List<SchoolClass> classes = profile.getAllClasses();
					assertNotNull(classes);

					assertEquals(-1, profile.getGPA(), 0.001);
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testGPAAllFailed() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					//
					List<SchoolClass> classes = profile.getAllClasses();
					assertNotNull(classes);
					updateGrade(getFromList(classes, 101), Grade.F);
					updateGrade(getFromList(classes, 102), Grade.F);
					updateGrade(getFromList(classes, 103), Grade.F);
					updateGrade(getFromList(classes, 201), Grade.F);

					assertEquals(0, profile.getGPA(), 0.001);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testGPADifferentGrades() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					//
					List<SchoolClass> classes = profile.getAllClasses();
					assertNotNull(classes);
					updateGrade(getFromList(classes, 101), Grade.A);
					updateGrade(getFromList(classes, 102), Grade.F);
					updateGrade(getFromList(classes, 103), Grade.B);
					updateGrade(getFromList(classes, 201), Grade.C);

					assertEquals(2.19, profile.getGPA(), 0.001);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testGPADifferentGrades2() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					//
					List<SchoolClass> classes = profile.getAllClasses();
					assertNotNull(classes);
					updateGrade(getFromList(classes, 101), Grade.A);
					updateGrade(getFromList(classes, 102), Grade.F);
					updateGrade(getFromList(classes, 103), Grade.A);
					updateGrade(getFromList(classes, 201), Grade.D);

					assertEquals(2.25, profile.getGPA(), 0.001);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	@Test
	public void testGPADifferentGrades3() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				prepareCourses();
				enrollSeveral();
				try {

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					//
					List<SchoolClass> classes = profile.getAllClasses();
					assertNotNull(classes);
					updateGrade(getFromList(classes, 101), Grade.B);
					updateGrade(getFromList(classes, 102), Grade.A);
					updateGrade(getFromList(classes, 103), Grade.A);
					updateGrade(getFromList(classes, 201), Grade.D);

					assertEquals(3.06, profile.getGPA(), 0.001);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	private void updateGrade(SchoolClass cl, Grade grade) throws Exception {
		int id = cl.getID();
		HashMap<String, Grade> studGrade = new HashMap<>();
		studGrade.put(studentEmail, grade);
		ClassDB.updateGrades(id, studGrade);
	}

	private static SchoolClass getFromList(List<SchoolClass> list, int number) {
		for (SchoolClass cl : list) {
			if (cl.course.courseNumber == number)
				return cl;
		}

		return null;
	}
}
