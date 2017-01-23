package JDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jolbox.bonecp.ConnectionTesterThread;

import akka.io.Tcp.Connect;
import play.db.DB;
import scala.util.control.Exception.Finally;
import models.Course;
import models.SchoolClass;
import models.SchoolClass.Grade;
import models.SchoolClass.ScheduleItem;
import models.SchoolClass.Weekday;
import models.Semester;
import models.User;
import models.User.UserType;

public class ClassDB {

	/**
	 * 
	 * @param cl
	 * @return unique database id of inserted class add SchoolClass cl to
	 *         database.
	 */
	public static int insertClass(SchoolClass cl) throws SQLException {
		Connection connect = DB.getConnection();

		try {
			PreparedStatement stmt = connect.prepareStatement(
					"insert into classes values(null,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(2, cl.course.getCourseID());
			stmt.setInt(1, cl.semester.getID());
			stmt.executeUpdate();

			ResultSet idRes = stmt.getGeneratedKeys();
			idRes.next();

			int classID = idRes.getInt(1);

			if (cl.teacher != null) {
				addOrUpdateTeacher(connect, classID, cl.teacher.email);
			}

			if (cl.location != null) {
				addOrUpdateLocation(connect, classID, cl.location);
			}

			addClassSize(connect, classID, cl.classSize);
			connect.createStatement().executeUpdate(
					"INSERT into classSpaceLeft values (" + classID + ", "
							+ cl.classSize + ")");

			return classID;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param id
	 *            unique database id of class
	 * @return returns fully constructed class with all data present in database
	 *         for given class (including fully constructed Semester, Course
	 *         objects of returned class)
	 * @throws SQLException
	 */
	public static SchoolClass getClassFull(int id) throws SQLException {
		Connection connect = DB.getConnection();

		try {
			PreparedStatement stmt = connect
					.prepareStatement("select "
							+ "classes.classID, classes.semesterID, classes.courseID,classteachers.teacherEmail, "
							+ " classLocations.location, classSpaceLeft.spaceLeft "
							// + "*"
							+ " from classes "
							+ "left join classteachers on classes.classID = classTeachers.classID "
							+ "left join classLocations on classes.classID = classLocations.classID "
							+ "left join classSpaceLeft on classes.classID = classSpaceLeft.classID "
							+ "where classes.classID = ?");
			stmt.setInt(1, id);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				SchoolClass res = rowToClassFull(resultSet);
				res.textbooks = getTextbooks(res.getID());
				res.schedule = getClassSchedule(res.getID(), connect);
				res.classSize = getClassSize(connect, res.getID());
				return res;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			connect.close();
		}
	}

	public static int deleteClass(int classID) throws SQLException,
			IllegalArgumentException {
		Connection connect = DB.getConnection();
		try {
			ResultSet resultSet = connect.createStatement().executeQuery(
					"select * from classRoll where classID = " + classID);
			if (resultSet.next()) {

				throw new IllegalArgumentException(
						"Can't delete a class which has students enrolled");
			}

			int rowsDeleted = connect.createStatement().executeUpdate(
					"delete from classes where classID = " + classID);
			connect.createStatement().executeUpdate(
					"delete from classLocations where classID = " + classID);
			connect.createStatement().executeUpdate(
					"delete from classTeachers where classID = " + classID);
			connect.createStatement().executeUpdate(
					"delete from textbooks where classID = " + classID);
			connect.createStatement().executeUpdate(
					"delete from classDays where classID = " + classID);
			connect.createStatement().executeUpdate(
					"delete from classDays where classID = " + classID);
			connect.createStatement().executeUpdate(
					"delete from classSize where classID = " + classID);
			connect.createStatement().executeUpdate(
					"delete from classSpaceLeft where classID = " + classID);
			return rowsDeleted;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param classID
	 * @param textBook
	 *            add textBook to schoolClass with classID
	 */
	public static void addTextBook(int classID, String textBook)
			throws SQLException {
		Connection connect = DB.getConnection();

		try {
			PreparedStatement stmt = connect
					.prepareStatement("insert into textbooks values(?,?)");
			stmt.setInt(1, classID);
			stmt.setString(2, textBook);
			stmt.execute();
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param classID
	 *            - unique id of a class in database.
	 * @return list of textbook names.
	 * @throws SQLException
	 */
	public static ArrayList<String> getTextbooks(int classID)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {

			PreparedStatement stmt = connect
					.prepareStatement("select * from textbooks where classID = ?");
			stmt.setInt(1, classID);
			ResultSet set = stmt.executeQuery();
			ArrayList<String> res = new ArrayList<>();
			while (set.next()) {
				res.add(set.getString(2));
			}
			return res;
		} finally {
			connect.close();
		}
	}

	/**
	 * remove textbook from a class.
	 * 
	 * @param classID
	 *            - unique id of a class in database.
	 * @param book
	 *            - String name of a textbook in class.
	 * @return true if book was successfully removed from class. False if class
	 *         with classID is not found, or class does not have textbook with
	 *         name. book
	 * @throws SQLException
	 */
	public static boolean removeTextbook(int classID, String book)
			throws SQLException {
		Connection connnect = DB.getConnection();
		try {
			PreparedStatement stmt = connnect
					.prepareStatement("delete from textbooks where textbook = ? and classID = ?");
			stmt.setString(1, book);
			stmt.setInt(2, classID);
			int rowsDeleted = stmt.executeUpdate();
			return rowsDeleted > 0;
		} finally {
			connnect.close();
		}
	}

	/**
	 * 
	 * @param classID
	 *            - unique id of a class in database.
	 * @param item
	 *            scheduleItem to be inserted.
	 * @return add scheduleItem to class schedule.
	 * @throws SQLException
	 */
	public static int addScheduleItem(int classID, ScheduleItem item)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect.prepareStatement(
					"insert into classDays values(null,?,?,?,?,)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, classID);
			stmt.setInt(2, item.day.getDayNumber());
			stmt.setString(3, item.startTime);
			stmt.setString(4, item.endTime);
			stmt.executeUpdate();
			ResultSet set = stmt.getGeneratedKeys();

			set.next();
			return set.getInt(1);
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param itemID
	 *            - unique id of a scheduleID in database.
	 * 
	 *            remove scheduleItem from class schedule.
	 * @throws SQLException
	 */
	public static boolean removeScheduleItem(int itemID) throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("delete from classDays where dayID = ?");
			stmt.setInt(1, itemID);
			return stmt.executeUpdate() > 0;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param semesterID
	 *            - unique database id of a semester.
	 * @param dept
	 *            - string id of a department.
	 * @return list of schoolclasses with specified semesterID and department.
	 * @throws SQLException
	 */
	public static List<SchoolClass> searchBySemesterAndDeptPart(int semesterID,
			String dept) throws SQLException {
		ArrayList<SchoolClass> res = new ArrayList<>();
		Connection connect = DB.getConnection();
		dept = dept.toUpperCase();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("select "
							+ "classes.classID, courses.courseID, courses.departmentID, courses.courseNumber, courses.courseName, courses.units"
							+ " from classes, courses, semesters where "
							+ "classes.courseID = courses.courseID " + "AND "
							+ "classes.semesterID = semesters.semesterID "
							+ "AND " + "classes.semesterID = ? " + "AND "
							+ "courses.departmentID = ?");
			stmt.setInt(1, semesterID);
			stmt.setString(2, dept);

			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				res.add(rowToClasspart(resultSet));
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
	 * @param classID
	 * @param studentEmail
	 *            add student to class.
	 * @throws SQLException
	 */
	public static boolean addStudentToClass(int classID, String studentEmail)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {

			connect.setAutoCommit(false);
			PreparedStatement addStudent = connect
					.prepareStatement("Insert into classRoll values (?,?)");
			addStudent.setInt(1, classID);
			addStudent.setString(2, studentEmail);
			PreparedStatement reduceSpace = connect
					.prepareStatement("UPDATE classSpaceLeft SET spaceLeft = spaceLeft - 1 where classID = ? and spaceLeft > 0");
			reduceSpace.setInt(1, classID);

			int a = reduceSpace.executeUpdate();
			addStudent.executeUpdate();

			if (a == 0) {
				connect.rollback();
				return false;
			} else {
				connect.commit();
				return true;
			}

		} catch (Exception e) {
			return false;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param classID
	 * @param studentEmail
	 *            drop student from class
	 * @throws SQLException
	 */
	public static boolean dropStudent(int classID, String studentEmail)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			connect.setAutoCommit(false);
			PreparedStatement dropStudent = connect
					.prepareStatement("DELETE from classRoll WHERE classID = ? AND studentID = ?");
			dropStudent.setInt(1, classID);
			dropStudent.setString(2, studentEmail);

			if (dropStudent.executeUpdate() == 0) {
				connect.rollback();
				return false;
			}

			connect.createStatement().execute(
					"UPDATE classSpaceLeft SET spaceLeft = spaceLeft + 1 where classID = "
							+ classID);

			connect.commit();
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @param studentEmail
	 * @return true if student with studentEmail is enrolled from class with id
	 *         classID. false otherwise.
	 * @throws SQLException
	 */
	public static boolean isStudentEnrolled(int classID, String studentEmail)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("select * from classRoll where classID = ?  AND studentID = ?");
			stmt.setInt(1, classID);
			stmt.setString(2, studentEmail);
			ResultSet resultSet = stmt.executeQuery();
			return resultSet.next();
		} finally {

		}
	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @return list of all students enrolled in class with classID.
	 * @throws Exception
	 */
	public static List<User> allStudents(int classID) throws Exception {
		Connection connect = DB.getConnection();
		try {
			ResultSet set = connect
					.createStatement()
					.executeQuery(
							"select * from classRoll, user where classRoll.studentID = user.Email AND classRoll.classID = "
									+ classID);

			ArrayList<User> res = new ArrayList<>();
			while (set.next()) {

				User student = new User();
				student.email = set.getString(3);
				student.firstName = set.getString(4);
				student.lastName = set.getString(5);
				student.userType = UserType.STUDENT;

				res.add(student);
			}

			return res;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param lassID
	 *            - unique database id of a class.
	 * @param grades
	 *            : key - student email, value - student grade. updates grades
	 *            of students present in grades map for class specified with
	 *            classID.
	 * @throws SQLException
	 */
	public static void updateGrades(int classID, Map<String, Grade> grades)
			throws SQLException {
		if (grades == null)
			throw new NullPointerException("grades map can't be null");
		if (grades.isEmpty())
			return;

		Connection connect = DB.getConnection();

		try {
			connect.setAutoCommit(false);
			PreparedStatement stmt = connect
					.prepareStatement("insert into grades values (?,?,?) on duplicate key update grade = ?");
			Iterator<String> students = grades.keySet().iterator();
			while (students.hasNext()) {
				stmt.setInt(1, classID);
				String email = students.next();

				stmt.setString(2, email.toLowerCase());
				stmt.setString(3, grades.get(email).toString());
				stmt.setString(4, grades.get(email).toString());
				stmt.addBatch();
			}

			stmt.executeBatch();
			connect.commit();
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @return map specifying grades of all graded students enrolled in class
	 *         with classID. key: student email. value: student grade.
	 * @throws SQLException
	 */
	public static Map<String, Grade> classGrades(int classID)
			throws SQLException {
		Connection connect = DB.getConnection();

		try {
			ResultSet resultSet = connect.createStatement().executeQuery(
					"select * from grades where classID = " + classID);
			Map<String, Grade> res = new HashMap<String, Grade>();
			while (resultSet.next()) {
				res.put(resultSet.getString(2),
						Grade.valueOf(resultSet.getString(3)));
			}

			return res;
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @param location
	 *            new location of a school class. update location parameter of
	 *            the class.
	 * @throws SQLException
	 */
	public static void addOrUpdateLocation(int classID, String location)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			addOrUpdateLocation(connect, classID, location);
		} finally {
			connect.close();
		}

	}

	public static void addOrUpdateTeacher(int classID, String teacherEmail)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			addOrUpdateTeacher(connect, classID, teacherEmail.toLowerCase());
		} finally {
			connect.close();
		}

	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @return class schedule.
	 * @throws SQLException
	 */
	public static List<ScheduleItem> getClassSchedule(int classID)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			return getClassSchedule(classID, connect);
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @param classSize
	 *            new class size. updates class size of class with classID. also
	 *            updates spaceleft parameter of the class by number equal to
	 *            difference between new and old classSize parameters. spaceleft
	 *            may become negative if class size is reduced with number
	 *            larger than prior value of spaceLeft.
	 * @throws SQLException
	 */
	public static void addOrUpdateClassSize(int classID, int classSize)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			UpdateClassSize(connect, classID, classSize);
		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @param email
	 * @return all classes taught by teacher with email.
	 * @throws SQLException
	 */
	public static List<SchoolClass> teacherClasses(String email)
			throws SQLException {
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("select * from classTeachers where teacherEmail =?");
			stmt.setString(1, email);
			ArrayList<SchoolClass> res = new ArrayList<>();
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				res.add(getClassFull(resultSet.getInt(1)));
			}
			return res;
		} finally {

		}
	}

	/*
	 * parse resultset row to SchoolClass object. parse is partial, only
	 * following parameters of resulting Schoolclass object are filled: *
	 * classID * course.
	 */
	private static SchoolClass rowToClasspart(ResultSet resultSet)
			throws SQLException {
		int classID = resultSet.getInt(1);
		int courseID = resultSet.getInt(2);
		String dept = resultSet.getString(3);
		int courseNumber = resultSet.getInt(4);
		String courseName = resultSet.getString(5);
		int units = resultSet.getInt(6);

		Course course = new Course(courseID);
		course.departmentID = dept;
		course.courseNumber = courseNumber;
		course.courseName = courseName;
		course.numberOfUnits = units;

		SchoolClass res = new SchoolClass(classID);
		res.course = course;

		return res;
	}

	/*
	 * parse resultset row to SchoolClass object. parse is full. all parameters
	 * of resulting schoolclass object are set if corresponding values are
	 * present in database.
	 */
	private static SchoolClass rowToClassFull(ResultSet resultSet)
			throws SQLException {
		SchoolClass result = new SchoolClass(resultSet.getInt(1));
		result.semester = SemesterDB.findByID(resultSet.getInt(2));
		result.course = CourseDB.getCourse(resultSet.getInt(3));

		result.location = resultSet.getString(5);
		String teacherEmail = resultSet.getString(4);
		result.spaceLeft = resultSet.getInt(6);

		if (teacherEmail != null) {
			result.teacher = UserDB.getUser(teacherEmail);
		}

		return result;
	}

	/*
	 * add or update location of class.
	 */
	private static void addOrUpdateLocation(Connection connect, int classID,
			String location) throws SQLException {
		PreparedStatement stmt = connect
				.prepareStatement("insert into classlocations values(?,?),on duplicate key update location = ?");

		stmt.setInt(1, classID);
		stmt.setString(2, location);
		stmt.setString(3, location);

		stmt.executeUpdate();

	}

	/*
	 * add or update teacher parameter of class with classID using Connection
	 * connect.
	 */
	private static void addOrUpdateTeacher(Connection connect, int classID,
			String teacherEmail) throws SQLException {
		PreparedStatement stmt = connect
				.prepareStatement("insert into classteachers values(?,?),on duplicate key update teacherEmail = ?");

		stmt.setInt(1, classID);
		stmt.setString(2, teacherEmail);
		stmt.setString(3, teacherEmail);

		stmt.executeUpdate();

	}

	private static List<ScheduleItem> getClassSchedule(int classID,
			Connection connect) throws SQLException {
		PreparedStatement stmt = connect
				.prepareStatement("select * from classDays where classID =?");
		stmt.setInt(1, classID);
		ResultSet resultSet = stmt.executeQuery();
		ArrayList<ScheduleItem> result = new ArrayList<>();
		while (resultSet.next()) {
			Weekday w = Weekday.findByNumber(resultSet.getInt(3));
			result.add(new ScheduleItem(resultSet.getInt(1), w, resultSet
					.getString(4), resultSet.getString(5)));
		}
		return result;
	}

	/*
	 * update class size and space left.
	 */
	private static void UpdateClassSize(Connection connect, int classID,
			int classSize) throws SQLException {
		connect.setAutoCommit(false);
		int prevSize = getClassSize(connect, classID);
		int difference = classSize - prevSize;
		PreparedStatement stmt = connect
				.prepareStatement("insert into classSize values(?,?) on duplicate key update size = ?");
		stmt.setInt(1, classID);
		stmt.setInt(2, classSize);
		stmt.setInt(3, classSize);
		stmt.executeUpdate();

		PreparedStatement updateSpaceLeft = connect
				.prepareStatement("Update classSpaceLeft SET classSpaceLeft.spaceLeft = classSpaceLeft.spaceLeft+? where classSpaceLeft.classID = ?");
		updateSpaceLeft.setInt(2, classID);
		updateSpaceLeft.setInt(1, difference);
		// stmt.setInt(3, difference);
		updateSpaceLeft.executeUpdate();

		connect.commit();
	}

	/*
	 * add size parameter of a class to database.
	 */
	private static void addClassSize(Connection connect, int classID,
			int classSize) throws SQLException {

		PreparedStatement stmt = connect
				.prepareStatement("insert into classSize values(?,?) on duplicate key update size = ?");
		stmt.setInt(1, classID);
		stmt.setInt(2, classSize);
		stmt.setInt(3, classSize);
		stmt.executeUpdate();

	}

	private static int getClassSize(Connection connect, int classID)
			throws SQLException {
		PreparedStatement stmt = connect
				.prepareStatement("select size from classSize where classID = ?");
		stmt.setInt(1, classID);
		ResultSet res = stmt.executeQuery();

		if (res.next()) {

			return res.getInt(1);
		}
		return 0;
	}
}
