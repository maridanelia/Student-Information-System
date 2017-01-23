package JDBC;

import models.Major;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import play.db.DB;
import models.Course;
import models.Department;

public class MajorDB {
	// my sql error code for violating unique constraint.
	private static final int UNIQUE_VIOLAITON_ERROR_CODE = 23505;

	public static int addMajor(Major major) throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect.prepareStatement(
					"insert into majors values(null,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, major.name);
			stmt.setInt(2, major.unitRequirements);
			stmt.executeUpdate();
			ResultSet resultSet = stmt.getGeneratedKeys();
			resultSet.next();
			return resultSet.getInt(1);
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @return list of all majors in database.
	 * @throws SQLException
	 */
	public static List<Major> getAllMajors() throws SQLException {
		Connection connect = DB.getConnection();

		try {

			ResultSet resultSet = connect.createStatement().executeQuery(
					"select * from majors");
			ArrayList<Major> res = new ArrayList<>();
			while (resultSet.next()) {
				Major major = new Major(resultSet.getInt(1));
				major.name = resultSet.getString(2);
				major.unitRequirements = resultSet.getInt(3);
				res.add(major);
			}

			return res;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param id
	 *            unique id of a major.
	 * @return Major with specified id. null if no such Major exists.
	 * @throws SQLException
	 */
	public static Major getMajorByID(int id) throws SQLException {
		Connection connect = DB.getConnection();

		try {

			ResultSet resultSet = connect.createStatement().executeQuery(
					"select * from majors where majorID = " + id);

			if (!resultSet.next())
				return null;

			Major major = new Major(resultSet.getInt(1));
			major.name = resultSet.getString(2);
			major.unitRequirements = resultSet.getInt(3);

			return major;
		} finally {
			connect.close();
		}
	}

	/**
	 * remove major form database.
	 * 
	 * @param majorID
	 *            - unique database id of a major.
	 * @return true if major was removed, false if not.
	 * @throws SQLException
	 */
	public static boolean removeMajor(int majorID) throws SQLException {
		Connection connect = DB.getConnection();
		TreeSet<Integer> set;

		try {
			connect.setAutoCommit(false);
			PreparedStatement stmt = connect
					.prepareStatement("DELETE from majors where majorID = "
							+ majorID);
			if (stmt.executeUpdate() == 0)
				return false;
			stmt = connect
					.prepareStatement("DELETE from major_courses where majorID = "
							+ majorID);
			stmt.executeUpdate();
			connect.commit();
			return true;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw e;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param majorID
	 * @param courseID
	 *            add course requirement to major.
	 * @throws SQLException
	 */
	public static boolean addCourseRequirement(int majorID, int courseID)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("INSERT into major_courses values(?,?)");
			stmt.setInt(1, majorID);
			stmt.setInt(2, courseID);
			return (stmt.executeUpdate() > 0);
		} catch (SQLException e) {
			if (e.getErrorCode() == UNIQUE_VIOLAITON_ERROR_CODE)
				return false;
			throw e;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param majorID
	 * @param courseID
	 *            remove course requirements from major.
	 * @throws SQLException
	 */
	public static boolean removeCourseRequirement(int majorID, int courseID)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("DELETE from major_courses where majorID = ? AND courseID = ?");
			stmt.setInt(1, majorID);
			stmt.setInt(2, courseID);
			return (stmt.executeUpdate() > 0);

		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param majorID
	 * @return return a list of all requirements for major with majorID.
	 * @throws SQLException
	 */
	public static List<Course> getCourseRequirements(int majorID)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			ArrayList<Course> res = new ArrayList<>();
			ResultSet resultSet = connect.createStatement().executeQuery(
					"Select * from major_courses, courses where "
							+ " major_courses.courseID = courses.courseID "
							+ "AND " + " major_courses.majorID = " + majorID);

			while (resultSet.next()) {
				res.add(rowToCourse(resultSet));
			}

			return res;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param studentEmail
	 * @param majorID
	 *            assign major to student.
	 * @throws SQLException
	 */
	public static void addStudentToMajor(String studentEmail, int majorID)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {

			PreparedStatement stmt = connect
					.prepareStatement("INSERT into student_majors values(?,?)"
							+ " on duplicate key update majorID = ?");
			stmt.setString(1, studentEmail.toLowerCase());
			stmt.setInt(2, majorID);
			stmt.setInt(3, majorID);
			stmt.executeUpdate();
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param email
	 *            - student email.
	 * @return major of a student specified with email.
	 * @throws SQLException
	 */
	public static Major getStudentMajor(String email) throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("select * from student_majors where email = ?");
			stmt.setString(1, email.toLowerCase());
			ResultSet resultSet = stmt.executeQuery();

			if (resultSet.next()) {
				return getMajorByID(resultSet.getInt(2));
			} else {
				return null;
			}
		} finally {
			connect.close();
		}
	}

	/*
	 * parse resultset row to course object.
	 */
	private static Course rowToCourse(ResultSet resultSet) throws SQLException {
		Course res = new Course(resultSet.getInt(3));
		res.departmentID = resultSet.getString(4);
		res.courseNumber = resultSet.getInt(5);
		res.courseName = resultSet.getString(6);
		res.numberOfUnits = resultSet.getInt(7);
		return res;
	}
}
