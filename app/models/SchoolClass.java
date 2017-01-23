package models;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.Semester.Term;
import models.User.UserType;

import org.joda.time.DateTime;

import JDBC.ClassDB;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;

public class SchoolClass {
	public static class ScheduleItem {
		public static final int IDENTIFIER_STRING_LENGTH = 5;
		private int id;

		public final String startTime;
		public final String endTime;
		public final Weekday day;

		public ScheduleItem(Weekday day, String startTime, String endTime) {
			this.id = -1;
			this.startTime = startTime;
			this.endTime = endTime;
			this.day = day;
		}

		public ScheduleItem(int id, Weekday day, String startTime,
				String endTime) {
			this.id = id;
			this.startTime = startTime;
			this.endTime = endTime;
			this.day = day;
		}

		public int getID() {
			return id;
		}

	}

	public enum Weekday {
		Monday(1), Tuesday(2), Wednesday(3), Thursday(4), Friday(5), Saturday(6), Sunday(
				7);
		private final int weekdayNumber;

		private Weekday(int dayNumber) {
			weekdayNumber = dayNumber;
		}

		public int getDayNumber() {
			return weekdayNumber;
		}

		public static Weekday findByNumber(int number) {
			for (Weekday day : Weekday.values()) {
				if (number == day.weekdayNumber)
					return day;
			}

			return null;
		}
	}

	public static enum Grade {
		A(4), B(3), C(2), D(1), F(0);

		int gradePoint;

		private Grade(int gradePoint) {
			this.gradePoint = gradePoint;
		}

	}

	private int id;
	public int classSize;
	public int spaceLeft;

	// done
	public Course course;

	// done

	public Semester semester;

	@Email
	public User teacher;
	public String location;
	private List<User> students;
	private Map<String, Grade> grades;
	public List<String> textbooks;

	public List<ScheduleItem> schedule;

	public Map<String, Grade> getGrades() throws Exception {
		if (grades == null) {
			grades = ClassDB.classGrades(id);
		}
		return grades;
	}

	public List<User> getClassRoll() throws Exception {
		if (students == null) {
			students = classRoll(id);
		}

		return students;
	}

	public int getSize() {
		return classSize;
	}

	public SchoolClass() {

	}

	public SchoolClass(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public String getIDString() {
		String res = "" + id;
		while (res.length() < 5) {
			res = "0" + res;
		}

		return res;
	}

	/**
	 * insert this object into database.
	 * 
	 * @return unique database id of the inserted object.
	 * @throws Exception
	 */
	public int insertIntoDatabase() throws Exception {
		return ClassDB.insertClass(this);
	}

	public static boolean removeClass(int classID)
			throws IllegalArgumentException, Exception {
		return ClassDB.deleteClass(classID) > 0;
	}

	/**
	 * 
	 * @param classID
	 * @param textBook
	 *            add textBook to schoolClass with classID
	 */
	public static void addTextBook(int classID, String textBook)
			throws Exception {
		ClassDB.addTextBook(classID, textBook);
	}

	/**
	 * remove textbook from a class in database.
	 * 
	 * @param classID
	 *            - unique id of a class in database.
	 * @param textbook
	 *            - String name of a textbook in class.
	 * @return true if book was successfully removed from class. False if class
	 *         with classID is not found, or class does not have textbook with
	 *         name.
	 */
	public static boolean removeTextbook(int classID, String textbook)
			throws Exception {
		return ClassDB.removeTextbook(classID, textbook);
	}

	/**
	 * 
	 * @param classID
	 *            - unique id of a class in database.
	 * @param item
	 *            scheduleItem to be inserted.
	 * @return add scheduleItem to class schedule and return its unique database
	 *         id.
	 * 
	 */
	public static int insertScheduleItem(int classID, ScheduleItem item)
			throws Exception {
		return ClassDB.addScheduleItem(classID, item);
	}

	/**
	 * 
	 * @param itemID
	 *            - unique id of a scheduleID in database.
	 * 
	 *            remove scheduleItem from class schedule.
	 * 
	 */
	public static boolean removeScheduleItem(int itemID) throws Exception {
		return ClassDB.removeScheduleItem(itemID);
	}

	/**
	 * 
	 * @param semesterID
	 *            - unique database id of a semester.
	 * @param dept
	 *            - string id of a department.
	 * @return list of schoolclasses with specified semesterID and department.
	 *
	 */
	public static List<SchoolClass> search(int semesterID, String dept)
			throws Exception {
		return ClassDB.searchBySemesterAndDeptPart(semesterID, dept);
	}

	/**
	 * 
	 * @param classID
	 * @param studentEmail
	 *            add student to class.
	 * @throws Exception
	 */
	public static boolean addStudentToClass(int classID, String email)
			throws Exception {
		return ClassDB.addStudentToClass(classID, email);
	}

	/**
	 * 
	 * @param classID
	 * @param studentEmail
	 *            drop student from class
	 * @throws Exception
	 */
	public static boolean dropStudentFromClass(int classID, String email)
			throws Exception {
		return ClassDB.dropStudent(classID, email);
	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @return list of all students enrolled in class with classID.
	 * @throws Exception
	 */
	public static List<User> classRoll(int classID) throws Exception {
		return ClassDB.allStudents(classID);
	}

	public static Map<String, Grade> classGrades(int classID) throws Exception {
		return ClassDB.classGrades(classID);
	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @return map specifying grades of all graded students enrolled in class
	 *         with classID. key: student email. value: student grade.
	 * @throws Exception
	 */
	public static SchoolClass searchByID(int id) throws Exception {
		return ClassDB.getClassFull(id);
	}

	/**
	 * 
	 * @param lassID
	 *            - unique database id of a class.
	 * @param grades
	 *            : key - student email, value - student grade. updates grades
	 *            of students present in grades map for class specified with
	 *            classID.
	 * @throws Exception
	 */
	public static void updateGrades(int classID, Map<String, Grade> grades)
			throws Exception {
		ClassDB.updateGrades(classID, grades);
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
	 * @throws Exception
	 */
	public static void addOrUpdateClassSize(int classID, int size)
			throws Exception {
		ClassDB.addOrUpdateClassSize(classID, size);
	}

	public static void addOrUpdateTeacher(int classID, String email)
			throws Exception {
		ClassDB.addOrUpdateTeacher(classID, email);
	}

	/**
	 * 
	 * @param classID
	 *            - unique database id of a class.
	 * @param location
	 *            new location of a school class. update location parameter of
	 *            the class.
	 * @throws Exception
	 */
	public static void addOrUpdateLocation(int classID, String location)
			throws Exception {
		ClassDB.addOrUpdateLocation(classID, location);
	}

}
