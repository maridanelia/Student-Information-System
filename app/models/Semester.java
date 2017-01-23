package models;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import JDBC.SemesterDB;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints.Max;
import play.data.validation.Constraints.Min;
import play.data.validation.Constraints.Required;
import play.data.format.*;

public class Semester {
	private int id;
	@Required
	@Formats.DateTime(pattern = "dd/mm/yyyy")
	public Date startDate;
	@Required
	public Date endDate;

	@Required
	@Min(1900)
	@Max(2200)
	public int year;

	public Term term;
	@Required
	public boolean availableForEnrolment;
	/**
	 * 
	 * Enum Specofying the term of the semester.
	 *
	 */
	public enum Term {
		Spring(1), Summer(2), Autumn(3), Winter(4);

		private final int termNumber;

		private Term(int id) {
			termNumber = id;
		}

		public int getID() {
			return termNumber;
		}

		public static Term getByID(int ID) {
			for (Term term : Term.values()) {
				if (ID == term.getID())
					return term;
			}
			return null;
		}
	}

	/**
	 * 
	 * Semester status indicating if the semester is past semester, upcoming future semester or currently in progress.
	 *
	 */
	public enum SemesterStatus {
		Past, InProgresss, Future;
	}
	/**
	 * 
	 * @return status of the semester (past, future, in progress) based on current date and semester star and end dates.
	 */
	public SemesterStatus getStatus() {

		if (startDate == null) {
			throw new NullPointerException(
					"startDate for this semester is null");
		}

		if (endDate == null) {
			throw new NullPointerException("endDate for this semester is null");
		}
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(System.currentTimeMillis());
		trimHoursMinutes(today);

		Calendar start = dateToCalendar(startDate);
		trimHoursMinutes(start);
		Calendar end = dateToCalendar(endDate);
		trimHoursMinutes(end);

		if (today.after(end))
			return SemesterStatus.Past;
		if (today.before(start))
			return SemesterStatus.Future;
		return SemesterStatus.InProgresss;

	}

	public Semester() {

	}

	public Semester(int id) {
		this.id = id;
	}

	public Semester(int year, Term term, boolean availableForEnrollment,
			Date startDate, Date endDate) {
		this.year = year;
		this.term = term;
		this.startDate = startDate;
		this.endDate = endDate;
		this.availableForEnrolment = availableForEnrollment;
	}

	public int getID() {
		return this.id;
	}

	public String toString() {
		return "" + term + year;
	}
	/**
	 * 
	 * Insert this object into database.
	 */
	public void insertIntoDatabase() throws Exception {

		SemesterDB.insertSemester(this);
	}

	public static List<Semester> allSemesters() throws Exception {
		try {
			List<Semester> res = SemesterDB.allSemesters();
			Collections.sort(res, new Comparator<Semester>() {
				public int compare(Semester s1, Semester s2) {
					if (s2.year == s1.year) {
						return s2.term.getID() - s1.term.getID();
					}
					return s2.year - s1.year;
				}
			});
			return res;
		} catch (Exception e) {
			throw e;
		}

	}

	private static void trimHoursMinutes(Calendar res) {
		res.clear(Calendar.HOUR_OF_DAY);
		res.clear(Calendar.MINUTE);
		res.clear(Calendar.SECOND);
		res.clear(Calendar.MILLISECOND);
	
	}

	private static Calendar dateToCalendar(Date date) {
		Calendar res = Calendar.getInstance();
		res.setTime(date);
		return res;
	}
}
