import static org.junit.Assert.*;
import static play.test.Helpers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.validation.constraints.AssertTrue;

import models.SchoolClass;
import models.User;

import org.junit.Test;

import play.api.Play;
import JDBC.UserDB;

public class TestEnrollmentConcurent {
	// test adding enroll and drop students in class with multiple concurrent
	// threads. add and drop each student multiple times. number of students =
	// classSize;
	// class should be full after enrolling is done.
	@Test
	public void addStudentsToClassFull() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					ClassTest.initCourses();
					List<User> students = ClassTest.generateStudents(10);
					ClassTest.insertUsersIntoDatabase(students);
					int classID = ClassTest.singleClass(10).getID();

					List<Thread> threads = new ArrayList<Thread>();
					CountDownLatch latch = new CountDownLatch(10);
					for (User stud : students) {
						User stud1 = stud;
						threads.add(new Thread(new Runnable() {

							@Override
							public void run() {
								for (int i = 0; i < 100; i++) {
									try {
										SchoolClass.addStudentToClass(classID,
												stud1.email);
										SchoolClass.dropStudentFromClass(
												classID, stud1.email);
									} catch (Exception e) {
										fail("Exception " + e.getMessage());
										latch.countDown();
									}
								}
								try {
									SchoolClass.addStudentToClass(classID,
											stud1.email);

								} catch (Exception e) {
									fail("Exception " + e.getMessage());
									latch.countDown();
								}
								latch.countDown();
							}
						}));
					}

					for (Thread thr : threads) {
						thr.start();
					}
					latch.await();

					SchoolClass cl = SchoolClass.searchByID(classID);
					assertEquals(0, cl.spaceLeft);
					ClassTest.userListEquals(students,
							SchoolClass.classRoll(classID));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test adding enroll and drop students in class with multiple concurrent
	// threads. add and drop each student multiple times. final action for each
	// student is drop class. class should be empty in the end.
	// number of students = classSize
	@Test
	public void addStudentsToClassEmpty() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {
					ClassTest.initCourses();
					List<User> students = ClassTest.generateStudents(10);
					ClassTest.insertUsersIntoDatabase(students);
					int classID = ClassTest.singleClass(10).getID();

					List<Thread> threads = new ArrayList<Thread>();
					CountDownLatch latch = new CountDownLatch(10);
					for (User stud : students) {
						User stud1 = stud;
						threads.add(new Thread(new Runnable() {

							@Override
							public void run() {
								for (int i = 0; i < 100; i++) {
									try {
										SchoolClass.addStudentToClass(classID,
												stud1.email);
										SchoolClass.dropStudentFromClass(
												classID, stud1.email);
									} catch (Exception e) {
										fail("Exception " + e.getMessage());
										latch.countDown();
									}
								}
								latch.countDown();
							}
						}));
					}

					for (Thread thr : threads) {
						thr.start();
					}
					latch.await();

					SchoolClass cl = SchoolClass.searchByID(classID);
					assertEquals(10, cl.spaceLeft);
					assertTrue(SchoolClass.classRoll(classID).isEmpty());
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test adding enroll and drop students in class with multiple concurrent
	// threads. add and drop each student multiple times.
	// number of students > classSize
	@Test
	public void addStudentsToClassOverLimit() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {

					ClassTest.initCourses();
					List<User> students = ClassTest.generateStudents(15);
					ClassTest.insertUsersIntoDatabase(students);
					int classID = ClassTest.singleClass(10).getID();

					List<Thread> threads = new ArrayList<Thread>();
					CountDownLatch latch = new CountDownLatch(15);
					for (User stud : students) {
						User stud1 = stud;
						threads.add(new Thread(new Runnable() {

							@Override
							public void run() {
								for (int i = 0; i < 100; i++) {
									try {
										SchoolClass.addStudentToClass(classID,
												stud1.email);
										SchoolClass.dropStudentFromClass(
												classID, stud1.email);
									} catch (Exception e) {
										latch.countDown();
										fail("Exception " + e.getMessage());

									}
								}
								try {
									SchoolClass.addStudentToClass(classID,
											stud1.email);

								} catch (Exception e) {
									latch.countDown();
									fail("Exception " + e.getMessage());

								}
								latch.countDown();
							}
						}));
					}

					for (Thread thr : threads) {
						thr.start();
					}
					latch.await();

					SchoolClass cl = SchoolClass.searchByID(classID);
					assertEquals(0, cl.spaceLeft);
					assertTrue(SchoolClass.classRoll(classID).size() == 10);

					assertTrue(isAllDifferent(SchoolClass.classRoll(classID)));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// test adding enroll and drop students in class with multiple concurrent
	// threads. add and drop each student multiple times.
	// number of students > classSize
	@Test
	public void addStudentsToClassOverLimit1() {
		running(fakeApplication(), new Runnable() {
			@Override
			public void run() {
				try {

					ClassTest.initCourses();
					List<User> students = ClassTest.generateStudents(150);
					ClassTest.insertUsersIntoDatabase(students);
					int classID = ClassTest.singleClass(30).getID();

					List<Thread> threads = new ArrayList<Thread>();
					CountDownLatch latch = new CountDownLatch(150);
					for (User stud : students) {
						User stud1 = stud;
						threads.add(new Thread(new Runnable() {

							@Override
							public void run() {
								for (int i = 0; i < 100; i++) {
									try {
										SchoolClass.addStudentToClass(classID,
												stud1.email);
										SchoolClass.dropStudentFromClass(
												classID, stud1.email);
									} catch (Exception e) {
										latch.countDown();
										fail("Exception " + e.getMessage());

									}
								}
								try {
									SchoolClass.addStudentToClass(classID,
											stud1.email);

								} catch (Exception e) {
									latch.countDown();
									fail("Exception " + e.getMessage());

								}
								latch.countDown();
							}
						}));
					}

					for (Thread thr : threads) {
						thr.start();
					}
					latch.await();

					SchoolClass cl = SchoolClass.searchByID(classID);
					assertEquals(0, cl.spaceLeft);
					assertTrue(SchoolClass.classRoll(classID).size() == 30);

					assertTrue(isAllDifferent(SchoolClass.classRoll(classID)));
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}

			}
		});
	}

	// return true if no duplicate users are present in a students list.
	private static boolean isAllDifferent(List<User> students) {
		HashSet<String> set = new HashSet<>();
		for (User stud : students) {
			if (!set.add(stud.email))
				return false;
		}

		return true;
	}
}
