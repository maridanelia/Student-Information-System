package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import JDBC.CourseDB;
import JDBC.CourseDB.noSuchCourseException;
import play.data.validation.Constraints.Max;
import play.data.validation.Constraints.Min;
import play.data.validation.Constraints.Required;
import play.db.DB;
import views.html.defaultpages.notFound;

public class Course {
	// * unique index of Course object in database system. Automatically
	// assigned by database.
	private int courseID = -1;

	// department id.
	@Required
	public String departmentID;

	@Required
	@Min(0)
	@Max(999)
	public int courseNumber;
	@Required
	public String courseName;

	public String description;

	@Required
	@Min(0)
	public int numberOfUnits;

	public List<Course> prerequisites;

	public Course() {

	}

	public int getCourseID() {
		return courseID;
	}

	public Course(int courseID) {
		this.courseID = courseID;
	}

	public Course(int courseNumber, String departmentID, String courseName,
			int numberOfUnits) {
		this.courseNumber = courseNumber;
		this.departmentID = departmentID;
		this.courseName = courseName;
		this.numberOfUnits = numberOfUnits;
	}

	public String toString() {
		return "(" + courseNumber + ") " + courseName + ", " + numberOfUnits
				+ " units";
	}

	/**
	 * Inserts this Course object into database.
	 * 
	 * @throws Exception
	 *             if: courseName or departmentID are null or numberOfUnits or
	 *             courseNumber is negative or courseNumber is more than three
	 *             digit number
	 */
	public void insertIntoDatabase() throws Exception {
		if (courseName == null)
			throw new NullPointerException("courseName can't be null");
		if (departmentID == null)
			throw new NullPointerException("departmentID can't be null");

		if (numberOfUnits < 0)
			throw new IllegalArgumentException(
					"numberOfUnits cannot be negative");
		if (courseNumber < 0)
			throw new IllegalArgumentException(
					"courseNumber cannot be negative");
		if (courseNumber > 999)
			throw new IllegalArgumentException(
					"courseNumber cannot be more that three digit number");

		try {
			CourseDB.insertCourse(this);
		} catch (SQLException e) {
			throw new Exception(e.getMessage());
		}
	}

	public static boolean deleteCourse(int courseID)
			throws IllegalArgumentException, Exception {
		return CourseDB.deleteCourse(courseID) > 0;
	}

	/**
	 * 
	 * @return all existing courses in database
	 * @throws Exception
	 */
	public static List<Course> allCourses() throws Exception {
		return CourseDB.allCourses();
	}

	/**
	 * 
	 * @return all existing courses in database with department containing
	 *         String dept as substring
	 * @throws Exception
	 */
	public static List<Course> coursesByDept(String dept) throws Exception {
		List<Course> allCourse = CourseDB.allCourses();
		ArrayList<Course> res = new ArrayList<>();
		for (Course course : allCourse) {
			if (course.departmentID != null
					&& course.departmentID.toLowerCase().contains(
							dept.toLowerCase())) {
				res.add(course);
			}
		}
		return res;
	}

	/**
	 * searches for a course in a database with
	 * 
	 * @return
	 */
	public static Course findCourse(String departmentID, int courseNumber)
			throws Exception {
		return CourseDB.getCourse(departmentID, courseNumber);
	}

	/**
	 * searchs a course based on course ID (Note: not same as course number)
	 * 
	 * @param courseID
	 * @return return course with courseID if present in database. return null
	 *         otherwise.
	 */
	public static Course findCourse(int courseID) throws Exception {
		return CourseDB.getCourse(courseID);
	}

	/**
	 * inserts prerequisiteID as preerquisite to course with courseID into
	 * database
	 */
	public static int addPrerequisiteToDatabase(int courseID,
			String prereqDepartmentID, int prereqCourseNumber)
			throws noSuchCourseException, Exception {
		return CourseDB.addPrerequisite(courseID, prereqDepartmentID,
				prereqCourseNumber);
	}

	/**
	 * updates description of course with courseID equal to param. also modifies
	 * description in system database. adds new description into database if no
	 * description exists. modifies otherwise.
	 * 
	 * @param description
	 */
	public static void updateDescription(int courseID, String description)
			throws Exception {
		CourseDB.insertDescription(courseID, description);
	}

	/**
	 * updates number of units for course with given courseID number
	 * 
	 * @param courseID
	 * @param units
	 * @throws Exception
	 */
	public static void updateUnits(int courseID, int units) throws Exception {
		CourseDB.updateUnits(courseID, units);
	}

	/**
	 * 
	 * @param courseID
	 * @param prereqID
	 *            remove course with id prereqID from list of prerequisites of
	 *            course with id courseID
	 * @throws Exception
	 */
	public static void removePrerequisite(int courseID, int prereqID)
			throws Exception {
		CourseDB.removePrereq(courseID, prereqID);
	}

}
