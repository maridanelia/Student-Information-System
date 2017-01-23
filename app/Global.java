import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import JDBC.ClassDB;
import JDBC.CourseDB;
import models.Course;
import models.SchoolClass;
import models.Semester;
import models.User;
import models.SchoolClass.Grade;
import models.SchoolClass.ScheduleItem;
import models.SchoolClass.Weekday;
import models.Semester.Term;
import models.User.UserType;
import play.GlobalSettings;
import play.Application;
import play.mvc.Result;

public class Global extends GlobalSettings {
	private static String dept = "CPSC";
	@Override
	public void onStart(Application app) {
		addUsers();
		try {
			addSemesters();
			addCoursesLarge();
			;
			createClasses();
			studentGrades();
		} catch (Exception e) {

		}
	}

	public static void addSemesters() throws Exception {
		(new Semester(2016, Term.Spring, true, daysFromToday(-10),
				daysFromToday(10))).insertIntoDatabase();
		(new Semester(2016, Term.Summer, true, daysFromToday(20),
				daysFromToday(30))).insertIntoDatabase();

		(new Semester(2015, Term.Winter, false, daysFromToday(-60),
				daysFromToday(-65))).insertIntoDatabase();
		(new Semester(2015, Term.Spring, false, daysFromToday(-50),
				daysFromToday(-55))).insertIntoDatabase();
		(new Semester(2015, Term.Summer, false, daysFromToday(-35),
				daysFromToday(-45))).insertIntoDatabase();
		(new Semester(2015, Term.Autumn, false, daysFromToday(-20),
				daysFromToday(-30))).insertIntoDatabase();

	}

	public static void addCoursesSmall() throws Exception {
		(new Course(101, "CPSC", "Introduction to Programming", 3))
				.insertIntoDatabase();
		(new Course(102, "CPSC", "Programming Concepts", 4))
				.insertIntoDatabase();
		(new Course(103, "CPSC", "Data Structures and Algorithms", 5))
				.insertIntoDatabase();

		(new Course(201, "CPSC", "Software Design", 4)).insertIntoDatabase();

	}

	public static void createClasses() throws Exception {

		List<Semester> allSemesters = Semester.allSemesters();
		List<Course> allCourses = CourseDB.allCourses();
		for (Semester sem : allSemesters) {
			for (Course course : allCourses) {
				SchoolClass cl = new SchoolClass();
				cl.semester = sem;
				cl.course = course;
				cl.classSize = 10;
				cl.teacher = new User("teacher@mail.com", "pass",
						UserType.TEACHER, "teacher", "teach");
				int id = cl.insertIntoDatabase();
				SchoolClass.addOrUpdateLocation(id, "Room 18");
				ScheduleItem item = new ScheduleItem(Weekday.Monday, "1:00 PM",
						"2:00 PM");
				SchoolClass.insertScheduleItem(id, item);
				item = new ScheduleItem(Weekday.Wednesday, "2:00 PM", "3:00 PM");
				SchoolClass.insertScheduleItem(id, item);
				item = new ScheduleItem(Weekday.Friday, "1:00 PM", "2:00 PM");
				SchoolClass.insertScheduleItem(id, item);

				SchoolClass.addTextBook(id, "Textbook of "
						+ cl.course.courseName);
				for (int i = 1; i < 7; i++) {
					SchoolClass.addStudentToClass(id, "stud" + i + "@mail.com");
				}
			}
		}
	}

	public static void addCoursesLarge() throws Exception {

		// some of the course names are taken from California State University
		// Fullerton course catalog.
		(new Course(101, "CPSC", "Introduction to Programming", 5))
				.insertIntoDatabase();
		(new Course(102, "CPSC", "Programming Concepts", 5))
				.insertIntoDatabase();
		(new Course(103, "CPSC", "Data Structures and Algorithms", 5))
				.insertIntoDatabase();
		(new Course(104, "CPSC", "Java Programming", 5)).insertIntoDatabase();
		(new Course(105, "CPSC", "Python Porgramming", 5)).insertIntoDatabase();
		(new Course(106, "CPSC", "C++ Programming", 5)).insertIntoDatabase();

		(new Course(201, "CPSC", "Social and Ethical Issues if Software", 5))
				.insertIntoDatabase();
		(new Course(202, "CPSC", "Programming Languages and Translation", 5))
				.insertIntoDatabase();
		(new Course(203, "CPSC", "File Structures and Database Systems", 5))
				.insertIntoDatabase();

		(new Course(204, "CPSC", "Software Architecture", 5))
				.insertIntoDatabase();
		(new Course(205, "CPSC", "Operating Systems", 5)).insertIntoDatabase();
		(new Course(206, "CPSC", "Artificial Intelligence", 5))
				.insertIntoDatabase();

		(new Course(201, "ACCT", "Financial Accounting", 5))
				.insertIntoDatabase();
		(new Course(202, "ACCT", "Managerial Accounting", 5))
				.insertIntoDatabase();
		(new Course(303, "ACCT", "Advanced Accounting", 5))
				.insertIntoDatabase();
		(new Course(404, "ACCT", "Auditing", 5)).insertIntoDatabase();
		(new Course(205, "ACCT", "Valuation Concepts", 5)).insertIntoDatabase();

		Course.addPrerequisiteToDatabase(12, "CPSC", 205);
		//
	}

	private void studentGrades() {
		enrollOneClass(101, 3, Grade.A, "student@mail.com");
		enrollOneClass(102, 3, Grade.A, "student@mail.com");
		enrollOneClass(103, 3, Grade.C, "student@mail.com");
		enrollOneClass(104, 4, Grade.B, "student@mail.com");

		enrollOneClass(201, 1, Grade.A, "student@mail.com");
		enrollOneClass(202, 1, Grade.B, "student@mail.com");

		enrollOneClass(101, 3, Grade.A, "student1@mail.com");
		enrollOneClass(102, 3, Grade.A, "student1@mail.com");
		enrollOneClass(103, 3, Grade.C, "student1@mail.com");
		enrollOneClass(104, 4, Grade.B, "student1@mail.com");
		enrollOneClass(201, 1, Grade.A, "student1@mail.com");
		enrollOneClass(202, 1, Grade.B, "student1@mail.com");
		enrollOneClass(203, 1, Grade.B, "student1@mail.com");
		enrollOneClass(204, 1, Grade.B, "student1@mail.com");
		enrollOneClass(205, 1, Grade.B, "student1@mail.com");
		enrollOneClass(206, 1, Grade.B, "student1@mail.com");
	}

	private static void addUsers() {
		try {
			(new User("student1@mail.com", "pass", User.UserType.STUDENT,
					"name1", "lastname1")).insertIntoDatabase();
			(new User("stud1@mail.com", "pass", User.UserType.STUDENT, "name1",
					"lastname1")).insertIntoDatabase();
			(new User("stud2@mail.com", "pass", User.UserType.STUDENT, "name2",
					"lastname2")).insertIntoDatabase();
			(new User("stud3@mail.com", "pass", User.UserType.STUDENT, "name3",
					"lastname3")).insertIntoDatabase();
			(new User("stud4@mail.com", "pass", User.UserType.STUDENT, "name4",
					"lastname4")).insertIntoDatabase();
			(new User("stud5@mail.com", "pass", User.UserType.STUDENT, "name5",
					"lastname5")).insertIntoDatabase();
			(new User("stud6@mail.com", "pass", User.UserType.STUDENT, "name5",
					"lastname6")).insertIntoDatabase();
			(new User("stud7@mail.com", "pass", User.UserType.STUDENT, "name6",
					"lastname7")).insertIntoDatabase();
			(new User("teacher1@mail.com", "pass", User.UserType.STUDENT,
					"name1", "name1")).insertIntoDatabase();
		} catch (Exception e) {

		}
	}

	private static Date daysFromToday(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DAY_OF_MONTH, n);
		return new Date(cal.getTimeInMillis());
	}

	private void updateGrade(SchoolClass cl, Grade grade, String studentEmail)
			throws Exception {
		int id = cl.getID();
		HashMap<String, Grade> studGrade = new HashMap<>();

		studGrade.put(studentEmail, grade);
		ClassDB.updateGrades(id, studGrade);
	}

	private void enrollOneClass(int number, int semesterID, Grade grade,
			String studentEmail) {
		try {
			List<SchoolClass> classes = ClassDB.searchBySemesterAndDeptPart(
					semesterID, dept);

			for (SchoolClass cl : classes) {
				if (cl.course.courseNumber == number) {
					ClassDB.addStudentToClass(cl.getID(), studentEmail);
					if (grade != null) {
						updateGrade(cl, grade, studentEmail);
					}
					return;
				}
			}
		} catch (Exception e) {

		}
	}
}
