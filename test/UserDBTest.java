import java.sql.SQLException;
import java.util.List;

import models.User;
import models.User.UserType;

import org.junit.Test;

import play.test.*;
import JDBC.UserDB;
import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class UserDBTest {
	@Test
	public void testInsertSameMailTwise() {
		// test that two users with same email can't be inserted twise.
		User user1 = userWithNoNullfields();
		User user2 = userWithNoNullfields();
		user2.userType = UserType.TEACHER;
		user2.password = "pass1";
		user2.lastName = "name2";
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {

					assertTrue(UserDB.insertUser(user1));
					assertFalse(UserDB.insertUser(user2));

				} catch (Exception e) {
					fail("Exception thrown");
				}
			}
		});
	}

	@Test
	public void testGetDefaultUsersNoPassword() {

		running(Helpers.fakeApplication(), new Runnable() {

			@Override
			public void run() {
				try {
					// test retreiving user profiles with email
					User user = UserDB.getUser("teaCHer@mail.com");
					assertNotNull(user);
					assertEquals("teacher@mail.com", user.email);

					user = UserDB.getUser("admin@mail.com");
					assertNotNull(user);
					assertEquals("admin@mail.com", user.email);

				} catch (Exception e) {
					fail("exception thrown");
				}
			}
		});

	}

	@Test
	public void testGetNonExistentEmailNoPassword() {
		running(fakeApplication(), new Runnable() {

			@Override
			public void run() {
				try {
					// test retreiving non-existent user.
					User user = UserDB.getUser("nonexist@mail.com");
					assertNull(user);
				} catch (Exception e) {
					fail("Exception thrown");
				}

			}
		});
	}

	// test retrieve user with email = null throws nullpointer exception;
	@Test
	public void testGetUserNullInputs_1() {
		running(fakeApplication(), new Runnable() {
			public void run() {

				try {
					UserDB.getUser(null);
					fail("nullPointerException not thrown");
				} catch (NullPointerException e) {
					assert (true);
				} catch (Exception e) {
					fail("incorrect exception thrown");
				}

			}
		});
	}

	// test getUSer(String email, String password)
	// test that nullpointer exception is thrown when trying to retrieve user
	// with null email.
	@Test
	public void testGetUserNullInputs_2() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					UserDB.getUser(null, "pass");

					fail("nullPointerException not thrown");
				} catch (NullPointerException e) {
					assert (true);
				} catch (Exception e) {
					fail("incorrect exception thrown");
				}

				try {

					UserDB.getUser("mail", null);
					fail("nullPointerException not thrown");
				} catch (NullPointerException e) {
					assert (true);
				} catch (Exception e) {
					fail("incorrect exception thrown");
				}

			}
		});
	}

	// test inserting users with various fields set to null.
	@Test
	public void testInsertUSerWithNullFields() {
		User user;

		user = userWithNoNullfields();
		user.email = null;
		insertNullException(user);

		user = userWithNoNullfields();
		user.password = null;
		insertNullException(user);

		user = userWithNoNullfields();
		user.userType = null;
		insertNullException(user);

		user = userWithNoNullfields();
		user.lastName = null;
		insertNullException(user);

		user = userWithNoNullfields();
		user.firstName = null;
		insertNullException(user);
	}

	// test insert user into database.
	@Test
	public void testInsert() {
		User user = userWithNoNullfields();
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					assertNull(UserDB.getUser(user.email));

					assertTrue(UserDB.insertUser(user));

					User result = UserDB.getUser(user.email);
					user.password = null;
					assertTrue(user.equals(result));

					// test that getUser is case insensitive.
					result = UserDB.getUser(user.email.toUpperCase());
					user.password = null;
					assertTrue(user.equals(result));

					// test that getUser is case insensitive.
					result = UserDB.getUser(user.email.toLowerCase());
					user.password = null;
					assertTrue(user.equals(result));
				} catch (Exception e) {
					fail("Exception thrown");
				}
			}
		});
	}

	// test retrieving user using password and email
	@Test
	public void testPassword() {
		User user = userWithNoNullfields();
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					user.password = "PassWord";
					assertTrue(user.insertIntoDatabase());

					// test that password is case sensitive.
					assertNull(UserDB.getUser(user.email, "password"));
					assertNotNull(UserDB.getUser(user.email, "PassWord"));
				} catch (Exception e) {
					fail("Exception thrown");
				}
			}
		});
	}

	@Test
	public void testAllUserListByInsert() {
		User user = userWithNoNullfields();

		running(fakeApplication(), new Runnable() {
			public void run() {
				try {

					assertFalse(listContainsUser(user, UserDB.allUsers()));
					assertTrue(UserDB.insertUser(user));
					user.password = null;
					assertTrue(listContainsUser(user, UserDB.allUsers()));
				} catch (SQLException e) {
					fail("SQL exception");
				}
			}
		});
	}

	// test deactivate user.
	@Test
	public void testAllUserListByDeactivate() {
		User user = userWithNoNullfields();

		running(fakeApplication(), new Runnable() {
			public void run() {
				try {

					assertFalse(listContainsUser(user, UserDB.allUsers()));
					assertTrue(UserDB.insertUser(user));
					UserDB.deactivateUser(user.email);
					assertFalse(listContainsUser(user, UserDB.allUsers()));
				} catch (SQLException e) {
					fail("SQL exception");
				}
			}
		});
	}

	@Test
	public void deactivateUserTest() {
		User user = userWithNoNullfields();
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					assertTrue(UserDB.insertUser(user));
					UserDB.deactivateUser(user.email);
					assertTrue(UserDB.isUserDeactivated(user.email));
					assertTrue(UserDB.getUser(user.email).isDeactivated);
				} catch (Exception e) {
					fail("exception thrown");
				}
			}
		});
	}

	// test deactivate user.
	@Test
	public void getDeactivateUser() {
		User user = userWithNoNullfields();
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					assertTrue(UserDB.insertUser(user));

					UserDB.deactivateUser(user.email);
					User userFromDB = UserDB.getUser(user.email);
					assertNotNull(userFromDB);

					assertTrue(userFromDB.isDeactivated);
					// deactivated user can't be retrieved using password (this
					// smethod is mainly used for log-in purposes).
					assertNull(UserDB.getUser(user.email, user.password));
				} catch (Exception e) {
					fail("exception thrown");
				}
			}
		});
	}

	// test deactivate user is case insensitive.
	@Test
	public void getDeactivateUserDifferentCase() {
		User user = userWithNoNullfields();
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					user.email = "MaIlStud@mail.com";
					assertTrue(UserDB.insertUser(user));

					user.email = "Mailstud@Mail.com";
					UserDB.deactivateUser(user.email);

					User userFromDB = UserDB.getUser(user.email);
					assertNotNull(userFromDB);
					assertTrue(userFromDB.isDeactivated);
					assertNull(UserDB.getUser(user.email, user.password));
				} catch (Exception e) {
					fail("exception thrown");
				}
			}
		});
	}

	// test restore user
	@Test
	public void restoreUser() {
		User user = userWithNoNullfields();
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					assertTrue(UserDB.insertUser(user));

					UserDB.deactivateUser(user.email);
					UserDB.restoreUser(user.email);

					User userFromDB = UserDB.getUser(user.email);

					assertNotNull(userFromDB);
					assertFalse(userFromDB.isDeactivated);
					assertNotNull(UserDB.getUser(user.email, user.password));
				} catch (Exception e) {
					fail("exception thrown");
				}
			}
		});
	}

	// test search for active users.
	@Test
	public void testSearchActive() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					(new User("AbC@mail.com", "Password", UserType.STUDENT,
							"abC", "Abc")).insertIntoDatabase();
					(new User("dAbCf@mail.com", "Password", UserType.STUDENT,
							"dabCf", "dAbcf")).insertIntoDatabase();
					(new User("te@mail.com", "Password", UserType.STUDENT,
							"te", "Te")).insertIntoDatabase();

					assertEquals(2, User.searchByEmailAndName("a", "bc", "")
							.size());

					assertEquals(2, User.searchByEmailAndName("", "bc", "")
							.size());

					assertEquals(2, User.searchByEmailAndName("b", "", "b")
							.size());

					assertEquals(1, User.searchByEmailAndName("f", "", "f")
							.size());
					assertEquals(1, User.searchByEmailAndName("", "F", "")
							.size());
					assertEquals(1, User.searchByEmailAndName("", "", "F")
							.size());

					assertEquals(1, User.searchByEmailAndName("TE", "Te", "tE")
							.size());

					// all users including defaults users added at application
					// startup.
					assertEquals(6, User.searchByEmailAndName("", "", "")
							.size());
					assertEquals(6, User.searchByEmailAndName("Mai", "", "")
							.size());

				} catch (Exception e) {
					fail("exception thrown");
				}
			}
		});
	}

	// test search when with deactivated users.
	@Test
	public void testSearchDeactivated() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					(new User("AbC@mail.com", "Password", UserType.STUDENT,
							"abC", "Abc")).insertIntoDatabase();
					(new User("dAbCf@mail.com", "Password", UserType.STUDENT,
							"dabCf", "dAbcf")).insertIntoDatabase();
					(new User("te@mail.com", "Password", UserType.STUDENT,
							"te", "Te")).insertIntoDatabase();

					UserDB.deactivateUser("abc@mail.com");
					UserDB.deactivateUser("dAbCf@mail.com".toLowerCase());
					UserDB.deactivateUser("Te@mail.com");

					// check that deactivated users are not among active users.
					assertEquals(0, User.searchByEmailAndName("a", "bc", "")
							.size());
					assertEquals(0, User.searchByEmailAndName("", "bc", "")
							.size());
					assertEquals(0, User.searchByEmailAndName("b", "", "b")
							.size());
					assertEquals(0, User.searchByEmailAndName("f", "", "f")
							.size());
					assertEquals(0, User.searchByEmailAndName("", "F", "")
							.size());
					assertEquals(0, User.searchByEmailAndName("", "", "F")
							.size());
					assertEquals(0, User.searchByEmailAndName("TE", "Te", "tE")
							.size());

					// check that deactivated users are found among inactive
					// users.
					assertEquals(2, User.searchdeactivatedUsers("a", "bc", "")
							.size());
					assertEquals(2, User.searchdeactivatedUsers("", "bc", "")
							.size());
					assertEquals(2, User.searchdeactivatedUsers("b", "", "b")
							.size());
					assertEquals(1, User.searchdeactivatedUsers("f", "", "f")
							.size());
					assertEquals(1, User.searchdeactivatedUsers("", "F", "")
							.size());
					assertEquals(1, User.searchdeactivatedUsers("", "", "F")
							.size());
					assertEquals(1,
							User.searchdeactivatedUsers("TE", "Te", "tE")
									.size());

					// all users including defaults users added at application
					// startup.
					assertEquals(3, User.searchByEmailAndName("", "", "")
							.size());
					assertEquals(3, User.searchByEmailAndName("Mai", "", "")
							.size());

				} catch (Exception e) {
					fail("exception thrown");
				}
			}
		});
	}

	// test that restore users is case insensitive
	@Test
	public void restoreUserDifferentCase() {
		User user = userWithNoNullfields();
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					user.email = "MaIlStud@mail.com";
					assertTrue(UserDB.insertUser(user));

					user.email = "MaiLStUd@mail.com";
					UserDB.deactivateUser(user.email);
					user.email = "MaiLStUd@MaiL.Com";
					UserDB.restoreUser(user.email);

					User userFromDB = UserDB.getUser(user.email);

					assertNotNull(userFromDB);
					assertFalse(userFromDB.isDeactivated);
					assertNotNull(UserDB.getUser(user.email, user.password));
				} catch (Exception e) {
					fail("exception thrown");
				}
			}
		});
	}

	// return a user object with all fields filled with non-null parameters.
	private User userWithNoNullfields() {
		User user = new User();
		user.email = "email@mail.com";
		user.password = "pass";
		user.firstName = "mari";
		user.lastName = "tester";
		user.userType = UserType.ADMIN;
		return user;
	}

	private boolean listContainsUser(User user, List<User> list) {

		for (User u : list) {
			if (u.equals(user)) {
				return true;
			}
		}

		return false;
	}

	private void insertNullException(User user) {

		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					UserDB.insertUser(user);

					fail("Null pointer exception Exception not thrown for user "
							+ user.toString());
				} catch (NullPointerException e) {
					assert (true);
				} catch (Exception e) {
					fail("incorrect exception thrown");
				}
			}
		});

	}

}
