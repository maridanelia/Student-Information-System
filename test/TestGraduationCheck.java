import static org.junit.Assert.*;
import static play.test.Helpers.*;

import java.util.HashMap;
import java.util.List;

import models.Course;
import models.Major;
import models.SchoolClass;
import models.StudentProfile;
import models.StudentProfile.gradCheckResult;
import models.User;
import models.SchoolClass.Grade;
import models.User.UserType;

import org.junit.Before;
import org.junit.Test;

import JDBC.ClassDB;
import JDBC.CourseDB;
import JDBC.MajorDB;
import JDBC.UserDB;

public class TestGraduationCheck {
	private static final String studentEmail = "student@mail.com";
	private static String dept = "CPSC";

	private void setUp() throws Exception {
		TestHelpers.addCoursesLarge();
		TestHelpers.addSemesters();
		TestHelpers.createClasses();

		Major maj = TestHelpers.createMajor("Computer Science", 40);
		maj.insertIntoDatabase();
		MajorDB.addStudentToMajor(studentEmail, 1);

		MajorDB.addCourseRequirement(maj.getID(), 1);
		MajorDB.addCourseRequirement(maj.getID(), 2);
		MajorDB.addCourseRequirement(maj.getID(), 3);
		MajorDB.addCourseRequirement(maj.getID(), 5);
	}

	// test graduation check for a student that passes check.
	@Test
	public void testGradCheckPass() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					setUp();
					enrollOneClass(101, 1, Grade.A);
					enrollOneClass(102, 1, Grade.A);
					enrollOneClass(103, 1, Grade.C);
					enrollOneClass(104, 1, Grade.B);
					enrollOneClass(105, 1, Grade.D);
					enrollOneClass(106, 1, Grade.C);
					enrollOneClass(201, 1, Grade.A);
					enrollOneClass(202, 1, Grade.B);

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					gradCheckResult gradCheck = profile.graduationCheck();

					assertTrue(gradCheck.result);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test graduation check for a student that does not have enough units
	// because some of his units are failed.
	@Test
	public void testGradCheckFailedUnits() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					setUp();
					enrollOneClass(101, 1, Grade.A);
					enrollOneClass(102, 1, Grade.F);
					enrollOneClass(103, 1, Grade.C);
					enrollOneClass(104, 1, Grade.B);
					enrollOneClass(105, 1, Grade.F);
					enrollOneClass(106, 1, Grade.C);
					enrollOneClass(201, 1, Grade.A);
					enrollOneClass(202, 1, Grade.B);

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					gradCheckResult gradCheck = profile.graduationCheck();

					assertFalse(gradCheck.result);
					assertEquals(gradCheckResult.NOT_ENOUGH_UNITS,
							gradCheck.status);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test graduation check for a student that does not have enough units yet
	// because some of student's classes are not graded.
	@Test
	public void testGradCheckNotGraded() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					setUp();
					enrollOneClass(101, 1, Grade.A);
					enrollOneClass(102, 1, null);
					enrollOneClass(103, 1, Grade.C);
					enrollOneClass(104, 1, Grade.B);
					enrollOneClass(105, 1, null);
					enrollOneClass(106, 1, Grade.C);
					enrollOneClass(201, 1, Grade.A);
					enrollOneClass(202, 1, Grade.B);

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					gradCheckResult gradCheck = profile.graduationCheck();

					assertFalse(gradCheck.result);
					assertEquals(gradCheckResult.NOT_ENOUGH_UNITS,
							gradCheck.status);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test graduation check for a student that does not have enough units.
	@Test
	public void testGradCheckNotEnoughUnits() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					setUp();
					enrollOneClass(101, 1, Grade.A);

					enrollOneClass(103, 1, Grade.C);
					enrollOneClass(104, 1, Grade.B);

					enrollOneClass(106, 1, Grade.C);
					enrollOneClass(201, 1, Grade.A);
					enrollOneClass(202, 1, Grade.B);

					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					gradCheckResult gradCheck = profile.graduationCheck();

					assertFalse(gradCheck.result);
					assertEquals(gradCheckResult.NOT_ENOUGH_UNITS,
							gradCheck.status);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test graduation check. some of required courses are missing.
	@Test
	public void requiredCourseMissing() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					setUp();

					enrollOneClass(102, 1, Grade.A);
					enrollOneClass(103, 1, Grade.C);
					enrollOneClass(104, 1, Grade.B);
					enrollOneClass(105, 1, Grade.D);
					enrollOneClass(106, 1, Grade.C);
					enrollOneClass(201, 1, Grade.A);
					enrollOneClass(202, 1, Grade.B);
					enrollOneClass(203, 1, Grade.A);
					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					gradCheckResult gradCheck = profile.graduationCheck();

					assertFalse(gradCheck.result);
					assertEquals(gradCheckResult.COURSE_REQUIREMENTS_FAILED,
							gradCheck.status);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test graduation check failed because student has failed a required
	// course.
	@Test
	public void testGradCheckFailedRequiredCourse() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					setUp();
					enrollOneClass(101, 1, Grade.F);
					enrollOneClass(102, 1, Grade.A);
					enrollOneClass(103, 1, Grade.C);
					enrollOneClass(104, 1, Grade.B);
					enrollOneClass(105, 1, Grade.D);
					enrollOneClass(106, 1, Grade.C);
					enrollOneClass(201, 1, Grade.A);
					enrollOneClass(202, 1, Grade.B);
					enrollOneClass(205, 1, Grade.B);
					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					gradCheckResult gradCheck = profile.graduationCheck();

					assertEquals(gradCheckResult.COURSE_REQUIREMENTS_FAILED,
							gradCheck.status);
					assertFalse(gradCheck.result);
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		});
	}

	// test graduation check passed if student failed required course but then
	// passed required course in other semester.
	@Test
	public void testGradCheckFailedRequiredCourse1() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					setUp();
					enrollOneClass(101, 1, Grade.F);
					enrollOneClass(101, 2, Grade.A);
					enrollOneClass(102, 1, Grade.A);
					enrollOneClass(103, 1, Grade.C);
					enrollOneClass(104, 1, Grade.B);
					enrollOneClass(105, 1, Grade.D);
					enrollOneClass(106, 1, Grade.C);
					enrollOneClass(201, 1, Grade.A);
					enrollOneClass(202, 1, Grade.B);
					enrollOneClass(205, 1, Grade.B);
					StudentProfile profile = StudentProfile
							.getStudentHistory(studentEmail);
					gradCheckResult gradCheck = profile.graduationCheck();

					assertEquals(gradCheckResult.PASSED, gradCheck.status);
					assertTrue(gradCheck.result);
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

	// enroll student in CPSC class by specifying course number semester ID.
	// Grade student in specified class.
	private void enrollOneClass(int number, int semesterID, Grade grade) {
		try {
			List<SchoolClass> classes = ClassDB.searchBySemesterAndDeptPart(
					semesterID, dept);

			for (SchoolClass cl : classes) {
				if (cl.course.courseNumber == number) {
					ClassDB.addStudentToClass(cl.getID(), studentEmail);
					if (grade != null) {
						updateGrade(cl, grade);
					}
					return;
				}
			}
		} catch (Exception e) {
			fail("could not enroll " + number + " " + e.getMessage());
		}
	}
}
