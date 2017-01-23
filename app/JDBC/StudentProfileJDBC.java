package JDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.List;
import java.util.Map;

import play.db.DB;
import models.Course;
import models.SchoolClass;
import models.SchoolClass.Grade;
import models.Semester;
import models.Semester.Term;

public class StudentProfileJDBC {
	/**
	 * 
	 * @param email
	 *            of a student.
	 * @return map of student grades of a student. key: class id, value: grade
	 * @throws SQLException
	 */
	public static Map<Integer, Grade> getStudentGrades(String email)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			Map<Integer, Grade> res = new HashMap();
			PreparedStatement stmt = connect
					.prepareStatement("select * from grades where studentID = ?");
			stmt.setString(1, email);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				res.put(resultSet.getInt(1),
						Grade.valueOf(resultSet.getString(3)));
			}

			return res;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param email
	 *            of a student.
	 * @return list of classess student has taken in the past or is currently
	 *         enrolled. class is partial, and contails only classID and course
	 *         parameters.
	 * @throws SQLException
	 */

	public static List<SchoolClass> getStudentClasses(String email)
			throws SQLException {
		Connection connect = DB.getConnection();

		try {
			ArrayList<SchoolClass> res = new ArrayList<>();
			PreparedStatement stmt = connect
					.prepareStatement("select * from classRoll "
							+ "join classes on classRoll.classID = classes.classID "
							+ "join semesters on classes.semesterID = semesters.semesterID "
							+ "join courses on classes.courseID = courses.courseID "
							+ "where classRoll.studentID = ?");

			stmt.setString(1, email);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				res.add(rowToClass(resultSet));
			}

			return res;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw e;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param student
	 *            email. udpate student status to graduated.
	 * @throws SQLException
	 */
	public static boolean graduate(String email) throws SQLException {
		if (isGraduated(email))
			return false;
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("insert into gradStudents values(?)");
			stmt.setString(1, email);
			stmt.executeUpdate();
			return true;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param email
	 *            student email.
	 * @return check if student is "graduated"
	 * @throws SQLException
	 */
	public static boolean isGraduated(String email) throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("select * from gradStudents where email = ?");
			stmt.setString(1, email);
			ResultSet resultSet = stmt.executeQuery();
			return resultSet.next();
		} finally {
			connect.close();
		}
	}

	private static SchoolClass rowToClass(ResultSet resultSet)
			throws SQLException {
		SchoolClass res = new SchoolClass(resultSet.getInt("classes.classID"));

		Semester semester = new Semester(
				resultSet.getInt("semesters.semesterID"));
		semester.year = resultSet.getInt("semesters.year");
		semester.term = Term.getByID(resultSet.getInt("semesters.term"));
		semester.startDate = new Date(resultSet.getLong("semesters.startDate"));
		semester.endDate = new Date(resultSet.getLong("semesters.endDate"));

		Course course = new Course(resultSet.getInt("courses.courseID"));
		course.departmentID = resultSet.getString("courses.departmentID");
		course.courseNumber = resultSet.getInt("courses.courseNumber");
		course.courseName = resultSet.getString("courses.courseName");
		course.numberOfUnits = resultSet.getInt("courses.units");

		res.semester = semester;
		res.course = course;

		res.schedule = ClassDB.getClassSchedule(res.getID());
		return res;
	}
}
