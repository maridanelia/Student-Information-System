package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import JDBC.StudentProfileJDBC;
import models.SchoolClass.Grade;
import models.Semester.SemesterStatus;

public class StudentProfile {
	public static class gradCheckResult {

		public static final int PASSED = 0;
		public static final int NO_MAJOR_SPECIFIED = 1;
		public static final int NOT_ENOUGH_UNITS = 2;
		public static final int COURSE_REQUIREMENTS_FAILED = 3;
		public final boolean result;
		public final int status;

		public gradCheckResult(boolean result, int status) {
			this.result = result;
			this.status = status;
		}
	}

	private final User user;
	private Map<Integer, Grade> grades;
	private List<SchoolClass> allClassesTaken;
	private Major major;

	private int unitsCompleted = -1;
	private int unitsTaken = -1;
	private int GPA = -1;

	public String getEmail() {
		return user.email;
	}

	public String getFirstName() {
		return user.firstName;
	}

	public String getLastName() {
		return user.lastName;
	}

	private StudentProfile(User user, List<SchoolClass> classes) {
		this.user = user;
		allClassesTaken = new ArrayList<>();
		for (SchoolClass cl : classes) {
			allClassesTaken.add(cl);
		}

		Collections.sort(allClassesTaken, new Comparator<SchoolClass>() {
			public int compare(SchoolClass cl1, SchoolClass cl2) {
				if (cl1.semester.year == cl2.semester.year) {
					return cl1.semester.term.getID()
							- cl2.semester.term.getID();
				}
				return cl1.semester.year - cl2.semester.year;
			}
		});
	}

	/**
	 * 
	 * @param email
	 * @return get StudentHistory object for student with specified email.
	 * @throws Exception
	 */
	public static StudentProfile getStudentHistory(String email)
			throws Exception {
		User user = User.getUserbyEmail(email);
		if (user == null)
			return null;
		return new StudentProfile(user,
				StudentProfileJDBC.getStudentClasses(email));
	}

	/**
	 * 
	 * @return student status (freshma, junior, sophomore, senior, graduated).
	 * @throws Exception
	 */
	public StudentYear getStudentYear() throws Exception {
		if (StudentProfileJDBC.isGraduated(user.email)) {
			return StudentYear.Graduated;
		}
		return StudentYear.getByUnits(unitsCompleted);
	}

	/**
	 * 
	 * @return number of units students has completed (all taken units minus
	 *         unfinished units and failed units).
	 * @throws Exception
	 */
	public int getUnitsCompleted() throws Exception {
		if (unitsCompleted == -1)
			calculateUnits();

		return unitsCompleted;
	}

	/**
	 * 
	 * @return total number of units enrolled by student.
	 * @throws Exception
	 */
	public int getUnitsTaken() throws Exception {
		if (unitsTaken == -1) {
			calculateUnits();
		}

		return unitsTaken;
	}

	/**
	 * 
	 * @return student GPA
	 * @throws Exception
	 */
	public double getGPA() throws Exception {
		if (GPA != -1)
			return GPA;
		if (grades == null) {
			getGradesEarned();
		}
		if (allClassesTaken.size() == 0)
			return -1;
		int GP = 0;
		int units = 0;
		for (SchoolClass cl : allClassesTaken) {
			int id = cl.getID();
			if (!grades.containsKey(id)) {

				continue;
			}
			GP += cl.course.numberOfUnits * grades.get(id).gradePoint;
			units += cl.course.numberOfUnits;

		}
		if (units == 0)
			return -1;
		// calculate result and round to two decimal digits
		double res = GP * 100;
		res = res / units;

		res += 0.5;
		res = (int) res;
		res = res / 100;
		return res;
	}

	/**
	 * 
	 * @return list of all classes that belong to current semester.
	 */
	public List<SchoolClass> currentClasses() {
		ArrayList<SchoolClass> res = new ArrayList<>();

		for (SchoolClass cl : allClassesTaken) {
			if (cl.semester.getStatus() == SemesterStatus.InProgresss) {
				res.add(cl);
			}
		}

		return res;
	}

	/**
	 * 
	 * @return list of all classes student is enrolled in present or was
	 *         enrolled in past.
	 */
	public List<SchoolClass> getAllClasses() {
		return allClassesTaken;
	}

	/**
	 * 
	 * @return map of grades earned by student. Key : class id, value : grade,
	 * @throws Exception
	 */
	public Map<Integer, Grade> getGradesEarned() throws Exception {
		if (grades == null) {
			grades = StudentProfileJDBC.getStudentGrades(user.email);
		}

		return grades;

	}

	/**
	 * 
	 * @param cl
	 * @return grade earned in class cl by student. null if student is not
	 *         enrolled in class or has not earned grade yet.
	 * @throws Exception
	 */
	public Grade getGrade(SchoolClass cl) throws Exception {
		if (grades == null) {
			grades = StudentProfileJDBC.getStudentGrades(user.email);
		}

		if (!grades.containsKey(cl.getID()))
			return null;
		return grades.get(cl.getID());
	}

	/**
	 * 
	 * @return major of student.
	 * @throws Exception
	 */
	public Major getMajor() throws Exception {
		if (major == null) {
			major = Major.getStudentMajor(getEmail());
		}

		return major;
	}

	/**
	 * 
	 * perform graduation check.
	 * 
	 * @throws Exception
	 */
	public gradCheckResult graduationCheck() throws Exception {
		getMajor();
		if (major == null) {
			return new gradCheckResult(false,
					gradCheckResult.NO_MAJOR_SPECIFIED);
		}

		int CompletedNotF = getUnitsCompleted();
		if (CompletedNotF < major.unitRequirements) {
			return new gradCheckResult(false, gradCheckResult.NOT_ENOUGH_UNITS);
		}

		if (!checkCourseRequirements()) {
			return new gradCheckResult(false,
					gradCheckResult.COURSE_REQUIREMENTS_FAILED);
		}
		return new gradCheckResult(true, 0);
	}

	/**
	 * 
	 * @param courseID
	 * @return return true if student has completed class with of course
	 *         specified by courseID.
	 * @throws Exception
	 */
	public boolean hasCompletedCourse(int courseID) throws Exception {
		getGradesEarned();

		for (SchoolClass cl : getAllClasses()) {

			if (cl.course.getCourseID() == courseID
					&& grades.containsKey(courseID)
					&& grades.get(courseID) != Grade.F)
				return true;
		}
		return false;
	}

	/*
	 * calculate number of total units student has taken calculate numebr of
	 * units student has completed.
	 * 
	 * @throws Exception
	 */
	private void calculateUnits() throws Exception {
		this.getGradesEarned();

		unitsCompleted = 0;
		unitsTaken = 0;
		for (SchoolClass cl : allClassesTaken) {
			int classID = cl.getID();
			int units = cl.course.numberOfUnits;

			unitsTaken += units;

			if (grades.containsKey(classID) && grades.get(classID) != Grade.F) {
				unitsCompleted += units;
			}
		}
	}

	private boolean checkCourseRequirements() throws Exception {
		Set<Integer> studentCourses = nonFailedCourses();
		for (Course course : major.getCourseRequirements()) {
			if (!studentCourses.contains(course.getCourseID()))
				return false;
		}

		return true;
	}

	private Set<Integer> nonFailedCourses() {
		Set<Integer> res = new HashSet<>();

		for (SchoolClass cl : allClassesTaken) {
			int classID = cl.getID();

			if (!grades.containsKey(classID) || grades.get(classID) != Grade.F) {
				res.add(cl.course.getCourseID());
			}
		}

		return res;
	}

	private int failedUnits() {
		int res = 0;
		for (SchoolClass cl : allClassesTaken) {
			int classID = cl.getID();
			int units = cl.course.numberOfUnits;
			if (grades.containsKey(classID) && grades.get(classID) == Grade.F) {
				res += units;
			}
		}
		return res;
	}
}
