import static org.junit.Assert.*;
import static play.test.Helpers.*;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Date;

import models.Course;
import models.Semester;
import models.Semester.SemesterStatus;

import org.junit.Before;
import org.junit.Test;

import JDBC.CourseDB;
import JDBC.UserDB;

public class SemesterTest {
	private Calendar now;
	private Calendar start;
	Calendar end;

	@Before
	public void setTimesToCurrent() {
		long currentTime = System.currentTimeMillis();

		now = Calendar.getInstance();
		start = Calendar.getInstance();
		end = Calendar.getInstance();

		now.setTimeInMillis(currentTime);
		start.setTimeInMillis(currentTime);
		end.setTimeInMillis(currentTime);
	}

	@Test
	public void testFutureSemester() {
		// set start date to 10 days from current time.
		start.add(Calendar.DAY_OF_MONTH, 10);
		// set end date to 5 days from now.
		end.add(Calendar.DAY_OF_MONTH, 15);
		Semester sem = new Semester();

		// set start date and end date to semester.
		sem.startDate = new Date(start.getTimeInMillis());
		sem.endDate = new Date(end.getTimeInMillis());

		// status of the semester should be future.
		assertTrue(sem.getStatus() == SemesterStatus.Future);

	}

	@Test
	public void testPastSemester() {
		// set start date 10 days before current time.
		start.add(Calendar.DAY_OF_MONTH, -10);
		// set end date to 5 days before current time.
		end.add(Calendar.DAY_OF_MONTH, -5);

		Semester sem = new Semester();
		// set start and end dates to semester object.
		sem.startDate = new Date(start.getTimeInMillis());
		sem.endDate = new Date(end.getTimeInMillis());

		// status of the semester should be past.
		assertTrue(sem.getStatus() == SemesterStatus.Past);

	}

	@Test
	public void testInProgressSemester() {
		// set start date 10 days before current time.
		start.add(Calendar.DAY_OF_MONTH, -10);
		// set end date to 5 after before current time.
		end.add(Calendar.DAY_OF_MONTH, 5);
		Semester sem = new Semester();
		// set start and end dates to semester object.
		sem.startDate = new Date(start.getTimeInMillis());
		sem.endDate = new Date(end.getTimeInMillis());

		// status of the semester should be in progress.
		assertTrue(sem.getStatus() == SemesterStatus.InProgresss);

	}

	@Test
	public void testLastDayOfSemester() {
		start.add(Calendar.DAY_OF_MONTH, -10);
		Semester sem = new Semester();
		sem.startDate = new Date(start.getTimeInMillis());
		sem.endDate = new Date(end.getTimeInMillis());

		assertTrue(sem.getStatus() == SemesterStatus.InProgresss);

	}

	@Test
	public void testFirstDayOfSemester() {
		end.add(Calendar.DAY_OF_MONTH, 5);
		Semester sem = new Semester();
		sem.startDate = new Date(start.getTimeInMillis());
		sem.endDate = new Date(end.getTimeInMillis());

		assertTrue(sem.getStatus() == SemesterStatus.InProgresss);

	}

	@Test
	public void testOneDayAfterEnd() {
		start.add(Calendar.DAY_OF_MONTH, -10);
		end.add(Calendar.DAY_OF_MONTH, -1);
		Semester sem = new Semester();
		sem.startDate = new Date(start.getTimeInMillis());
		sem.endDate = new Date(end.getTimeInMillis());

		assertTrue(sem.getStatus() == SemesterStatus.Past);
	}

	@Test
	public void testOneDayBeforeStart() {
		start.add(Calendar.DAY_OF_MONTH, 1);
		end.add(Calendar.DAY_OF_MONTH, 10);
		Semester sem = new Semester();
		sem.startDate = new Date(start.getTimeInMillis());
		sem.endDate = new Date(end.getTimeInMillis());

		assertTrue(sem.getStatus() == SemesterStatus.Future);
	}
}
