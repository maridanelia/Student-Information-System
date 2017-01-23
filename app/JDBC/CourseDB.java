package JDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import play.db.DB;
import models.Course;
import models.Department;

public class CourseDB {
	public static class noSuchCourseException extends Exception {
		public noSuchCourseException(String message) {
			super(message);
		}
	}

	private static final String COURSE_TABLE = "courses";
	private static final String DEPARTMENTS_TABLE = "departments";

	private static final int COURSE_ID_COL = 1;
	private static final int DEPARTMENT_ID_COL = 2;
	private static final int COURSE_NUMBER_COL = 3;
	private static final int COURSE_NAME_COL = 4;
	private static final int UNITS_COL = 5;

	/**
	 * 
	 * @return list of all existing Departments in database.
	 * @throws SQLException
	 */
	public static List<Department> getAllDepartments() throws SQLException {
		ArrayList<Department> result = new ArrayList<>();
		Connection connect = DB.getConnection();

		try {
			ResultSet resultSet = connect.createStatement().executeQuery(
					"select * from " + DEPARTMENTS_TABLE);

			while (resultSet.next()) {
				Department dep = new Department(resultSet.getString(1),
						resultSet.getString(2));
				result.add(dep);
			}
		} finally {
			connect.close();
		}

		return result;
	}

	/**
	 * inserts course into database. assignes unique courseID.
	 * 
	 * @param course
	 * @return true if insert successful.
	 * @throws SQLException
	 */
	public static boolean insertCourse(Course course) throws SQLException {
		if (course.departmentID == null) {
			throw new NullPointerException("departmentID cannot be null");
		}

		if (course.courseName == null) {
			throw new NullPointerException("courseName cannot be null");
		}

		Connection connect = DB.getConnection();
		try {

			PreparedStatement stmt = connect.prepareStatement("insert into "
					+ COURSE_TABLE + " values(null,?,?,?,?)");
			stmt.setString(1, course.departmentID.toUpperCase());
			stmt.setInt(2, course.courseNumber);
			stmt.setString(3, course.courseName);
			stmt.setInt(4, course.numberOfUnits);
			stmt.executeUpdate();

			if (course.description != null && course.description != null) {
				int id = courseID(course.departmentID, course.courseNumber,
						connect);
				if (id != -1) {
					insertDescription(id, course.description, connect);
				}
			}

		} finally {
			connect.close();
		}

		return true;
	}

	public static int deleteCourse(int courseID)
			throws IllegalArgumentException, SQLException {
		Connection connect = DB.getConnection();
		try {
			ResultSet resultSet = connect.createStatement().executeQuery(
					"select * from classes where courseID = " + courseID);
			if (resultSet.next()) {
				throw new IllegalArgumentException(
						"Cannot delete a course to which classes are assigned.");
			}

			resultSet = connect.createStatement().executeQuery(
					"select * from major_courses where courseID = " + courseID);
			if (resultSet.next()) {
				throw new IllegalArgumentException(
						"Cannot delete a course which is a part of major course requirements.");
			}

			int rowsDeleted = connect.createStatement().executeUpdate(
					"delete from courses where courseID = " + courseID);
			connect.createStatement().executeUpdate(
					"delete from courseDescriptions where courseID = "
							+ courseID);
			connect.createStatement().executeUpdate(
					"delete from prerequisites where courseID = " + courseID);

			return rowsDeleted;
		} finally {
			connect.close();
		}
	}

	/**
	 * inserts description into database if description does not exist. updates
	 * existing description otherwise.
	 * 
	 * @param courseID
	 * @param description
	 * @throws SQLException
	 */
	public static void insertDescription(int courseID, String description)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			insertDescription(courseID, description, connect);
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param courseID
	 *            - unique database id of a Course.
	 * @return String description of course with given id.
	 * @throws SQLException
	 */
	public static String getCourseDescription(int courseID) throws SQLException {
		Connection connect = DB.getConnection();
		try {
			return courseDescription(courseID, connect);
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @return list of all courses in the database.
	 * @throws SQLException
	 */
	public static List<Course> allCourses() throws SQLException {
		List<Course> result = new ArrayList<>();
		Connection connect = DB.getConnection();

		try {
			ResultSet resultSet = connect
					.prepareStatement(
							"select * from "
									+ COURSE_TABLE
									+ " left join "
									+ "courseDescriptions on courses.courseID=courseDescriptions.courseID")
					.executeQuery();

			while (resultSet.next()) {
				result.add(rowToCourse(resultSet));
			}
		} finally {
			connect.close();
		}
		return result;
	}

	/**
	 * 
	 * @param departmentID
	 * @param courseNumber
	 * @return a course with specified departmentID and courseNumber. return
	 *         null if no such course exists in database
	 * @throws SQLException
	 */
	public static Course getCourse(String departmentID, int courseNumber)
			throws SQLException {
		Connection connect = DB.getConnection();
		departmentID = departmentID.toUpperCase();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("select * from "
							+ COURSE_TABLE
							+ " left join "
							+ "courseDescriptions on courses.courseID = courseDescriptions.courseID"

							+ " where departmentID = ? AND courseNumber = ?");
			stmt.setString(1, departmentID);
			stmt.setInt(2, courseNumber);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				return rowToCourse(resultSet);
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param courseID
	 * @return a course with specified id. return null if no such course exists
	 *         in database
	 * @throws SQLException
	 */
	public static Course getCourse(int courseID) throws SQLException {
		Connection connect = DB.getConnection();

		try {
			PreparedStatement stmt = connect.prepareStatement("select * from "
					+ COURSE_TABLE + " left join " + "courseDescriptions"
					+ " on courses.courseID = courseDescriptions.courseID"
					+ " where courses.courseID = ?");
			stmt.setInt(1, courseID);

			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				return rowToCourse(resultSet);
			} else {
				return null;
			}
		} finally {
			connect.close();
		}
	}

	/**
	 * add prerequisite to a course.
	 * 
	 * @param courseID
	 *            - unique database id of a course to which prerequisite is
	 *            added.
	 * @param prereqDepartment
	 *            - prerequisite department id.
	 * @param prereqCourseNumber
	 *            - prerequisite course number.
	 * @return unique database id of the course which was added as prerequisite.
	 * @throws SQLException
	 * @throws noSuchCourseException
	 *             if course with specified prerequisite department and course
	 *             number does not exist.
	 */
	public static int addPrerequisite(int courseID, String prereqDepartment,
			int prereqCourseNumber) throws SQLException, noSuchCourseException {
		Connection connect = DB.getConnection();
		prereqDepartment = prereqDepartment.toUpperCase();

		try {
			int prereqID = courseID(prereqDepartment, prereqCourseNumber,
					connect);
			if (prereqID == -1) {

				throw new noSuchCourseException(
						"course with prereqDepartment = " + prereqCourseNumber
								+ " and prereqCourseNumber = "
								+ prereqCourseNumber + " does not exist");
			}
			addPrerequisite(courseID, prereqID, connect);
			return prereqID;

		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param courseID
	 *            unique database ID of a course.
	 * @return list of all course id-s of prerequisite course of course
	 *         specified by courseID.
	 * @throws SQLException
	 */
	public static List<Course> getPrerequisites(int courseID)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			return getPrerequisites(courseID, connect);
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param id
	 *            - unique database ID of a course.
	 * @param units
	 *            new value of units of a course. update units of a course.
	 * @throws SQLException
	 */
	public static void updateUnits(int id, int units) throws SQLException {
		if (units < 0) {
			throw new IllegalArgumentException("units can't be negative");
		}
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("update courses set units = ? where courseID = ?");
			stmt.setInt(1, units);
			stmt.setInt

			(2, id);
			stmt.executeUpdate();
		} finally {
			connect.close();
		}

	}

	/**
	 * removes pair of courseID and prereqID from prerequisites table.
	 * 
	 * @param courseID
	 * @param prereqID
	 */
	public static void removePrereq(int courseID, int prereqID)
			throws SQLException {
		Connection connect = DB.getConnection();
		PreparedStatement stmt = connect
				.prepareStatement("delete from prerequisites where courseID = ? and prerequisite = ?");
		try {
			stmt.setInt(1, courseID);
			stmt.setInt(2, prereqID);
			stmt.executeUpdate();
		} finally {
			connect.close();
		}

	}

	/*
	 * find courseID of a class specified by department and coursenumber. return
	 * -1 if no such class present in database.
	 */
	private static int courseID(String department, int courseNumber,
			Connection connect) throws SQLException {
		PreparedStatement stmt = connect
				.prepareStatement("select courseID from courses where departmentID = ? AND courseNumber = ?");
		stmt.setString(1, department.toUpperCase());
		stmt.setInt(2, courseNumber);
		ResultSet resultSet = stmt.executeQuery();
		if (resultSet.next())
			return resultSet.getInt(1);

		return -1;
	}

	private static void insertDescription(int courseID, String description,
			Connection connect) throws SQLException {

		PreparedStatement stmt = connect
				.prepareStatement("insert into coursedescriptions values (?,?) on duplicate key update description = ?");

		stmt.setInt(1, courseID);
		stmt.setString(2, description);
		stmt.setString(3, description);
		stmt.execute();
	}

	private static String courseDescription(int courseID, Connection connect)
			throws SQLException {

		PreparedStatement stmt = connect
				.prepareStatement("select * from courseDescriptions where courseID = ?");
		stmt.setInt(1, courseID);
		// stmt.setString(parameterIndex, x);
		ResultSet resultSet = stmt.executeQuery();
		if (resultSet.next()) {
			return resultSet.getString(2);
		} else {
			return null;
		}

	}

	/*
	 * parse resultset row into Course object.
	 */
	private static Course rowToCourse(ResultSet resultSet) throws SQLException {
		Course res = null;

		int courseID = resultSet.getInt(COURSE_ID_COL);

		res = new Course(courseID);
		res.courseName = resultSet.getString(COURSE_NAME_COL);
		res.courseNumber = resultSet.getInt(COURSE_NUMBER_COL);
		res.departmentID = resultSet.getString(DEPARTMENT_ID_COL);
		res.numberOfUnits = resultSet.getInt(UNITS_COL);
		res.description = resultSet.getString(UNITS_COL + 2);

		res.prerequisites = getPrerequisites(courseID);
		return res;

	}

	private static void addPrerequisite(int courseID, int prereqID,
			Connection connect) throws SQLException {

		PreparedStatement stmt = connect
				.prepareStatement("insert into prerequisites values(?,?)");
		stmt.setInt(1, courseID);
		stmt.setInt(2, prereqID);
		stmt.execute();
	}

	private static List<Course> getPrerequisites(int courseID,
			Connection connect) throws SQLException {
		//printAllPrereq(connect);
		PreparedStatement stmt = connect
				.prepareStatement("select courses.departmentID, courses.courseNumber,prerequisites.prerequisite from prerequisites, courses "
						+ "where prerequisites.courseID = ? "
						+ "AND courses.courseID = prerequisites.prerequisite");
		stmt.setInt(1, courseID);
		ResultSet resultSet = stmt.executeQuery();
		ArrayList<Course> result = new ArrayList<>();
		while (resultSet.next()) {
			Course c = new Course(resultSet.getInt(3));
			c.departmentID = resultSet.getString(1);
			c.courseNumber = resultSet.getInt(2);
			result.add(c);
		}
		return result;
	}

	private static void printAllPrereq(Connection connect) throws SQLException {
		ResultSet set = connect.prepareStatement("select * from prerequisites")
				.executeQuery();
		while (set.next()) {

			System.out.println(set.getInt(1) + " " + set.getInt(2));
		}
	}
}
