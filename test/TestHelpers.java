import java.util.Calendar;
import java.util.Date;
import java.util.List;

import JDBC.CourseDB;
import models.Course;
import models.Major;
import models.SchoolClass;
import models.Semester;
import models.Semester.Term;

public class TestHelpers {

	public static void addSemesters() throws Exception {

		(new Semester(2015, Term.Summer, false, daysFromToday(-35),
				daysFromToday(-45))).insertIntoDatabase();
		(new Semester(2015, Term.Autumn, false, daysFromToday(-20),
				daysFromToday(-30))).insertIntoDatabase();

		(new Semester(2016, Term.Spring, true, daysFromToday(-10),
				daysFromToday(10))).insertIntoDatabase();
		(new Semester(2016, Term.Summer, true, daysFromToday(20),
				daysFromToday(30))).insertIntoDatabase();

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

		//
	}

	// create a class for each course in database for each semester in database.
	public static void createClasses() throws Exception {

		List<Semester> allSemesters = Semester.allSemesters();
		List<Course> allCourses = CourseDB.allCourses();
		for (Semester sem : allSemesters) {
			for (Course course : allCourses) {
				SchoolClass cl = new SchoolClass();
				cl.semester = sem;
				cl.course = course;
				cl.classSize = 10;
				cl.insertIntoDatabase();
			}
		}
	}

	public static void addStudents() throws Exception {

	}

	// return a Date object that specifies a date n days from today (negative
	// values for past days, positive values for future dates).
	private static Date daysFromToday(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DAY_OF_MONTH, n);
		return new Date(cal.getTimeInMillis());
	}

	static Major createMajor(String name, int units) {
		Major res = new Major();
		res.unitRequirements = units;
		res.name = name;
		return res;
	}
}
