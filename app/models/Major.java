package models;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.activity.InvalidActivityException;

import play.data.validation.Constraints.Min;
import play.data.validation.Constraints.Required;
import play.db.DB;
import JDBC.MajorDB;

public class Major {
	public int id = -1;
	@Required
	public String name;

	@Required
	@Min(0)
	public int unitRequirements;
	private List<Course> courseRequirements;

	public Major() {

	}

	public Major(int id) {
		this.id = id;
	}

	public int insertIntoDatabase() throws Exception {
		id = MajorDB.addMajor(this);
		return id;
	}

	public String toString() {
		StringBuilder build = new StringBuilder();
		build.append(name);
		build.append(", unitReq = ");
		build.append(unitRequirements);
		build.append(", courseReq = ");
		build.append(courseRequirements);
		return build.toString();
	}

	public int getID() {
		return id;
	}

	public String idString() {
		return "" + id;
	}

	public List<Course> getCourseRequirements() throws Exception {
		if (id == -1) {
			throw new InvalidActivityException(
					"unit requirements cannnot be retreived as this object is not in database");
		}

		if (courseRequirements == null) {
			retrieveCourseRequirements();
		}

		return courseRequirements;
	}

	/**
	 * 
	 * @param studentEmail
	 * @param majorID
	 *            assign major to student.
	 */
	public static void addStudentToMajor(String email, int majorID)
			throws Exception {
		User student = User.getUserbyEmail(email);
		if (student == null)
			throw new IllegalAccessException("Student with email " + email
					+ " is not in the system");
		if (MajorDB.getMajorByID(majorID) == null)
			throw new IllegalAccessException("major with ID " + majorID
					+ " is not in the system");
		MajorDB.addStudentToMajor(email, majorID);
	}

	/**
	 * 
	 * @param email
	 *            - student email.
	 * @return major of a student specified with email.
	 */
	public static Major getStudentMajor(String email) throws Exception {
		return MajorDB.getStudentMajor(email);
	}

	/**
	 * 
	 * @param id
	 *            unique id of a major.
	 * @return Major with specified id. null if no such Major exists.
	 */
	public static Major searchByID(int id) throws Exception {
		return MajorDB.getMajorByID(id);
	}

	/**
	 * 
	 * @param majorID
	 * @param courseID
	 *            add course requirement to major.
	 * @throws Exception
	 */
	public static boolean addCourseToMajor(int majorID, int courseID)
			throws Exception {

		return MajorDB.addCourseRequirement(majorID, courseID);

	}

	/**
	 * 
	 * @param majorID
	 * @param courseID
	 *            remove course requirements from major.
	 * @throws Exception
	 */
	public static boolean removeCourseFromMajor(int majorID, int courseID)
			throws Exception {
		return MajorDB.removeCourseRequirement(majorID, courseID);
	}

	/**
	 * remove major form database.
	 * 
	 * @param majorID
	 *            - unique database id of a major.
	 * @return true if major was removed, false if not.
	 */
	public static boolean removeMajor(int majorID) throws Exception {
		return MajorDB.removeMajor(majorID);
	}

	/**
	 * 
	 * @return list of all majors.
	 * @throws Exception
	 */
	public static List<Major> getAllMajors() throws Exception {
		return MajorDB.getAllMajors();
	}

	private void retrieveCourseRequirements() throws SQLException {
		courseRequirements = MajorDB.getCourseRequirements(id);
	}
}
