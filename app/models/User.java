package models;

import java.lang.String;
import java.sql.SQLException;
import java.util.List;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import JDBC.UserDB;

public class User {
	public static enum UserType {
		TEACHER(1), STUDENT(2), ADMIN(3);

		private final int typeID;

		private UserType(int ID) {
			typeID = ID;
		}

		public int getID() {
			return this.typeID;
		}

		public static UserType getByID(int ID) {
			for (UserType user : UserType.values()) {
				if (ID == user.getID())
					return user;
			}
			return null;
		}
	}

	public boolean equals(User user) {

		return stringEquals(email, user.email)
				&& stringEquals(password, user.password)
				&& (user.userType == this.userType)
				&& stringEquals(firstName, user.firstName)
				&& stringEquals(lastName, user.lastName);
	}

	private boolean stringEquals(String str1, String str2) {
		if (str1 == null)
			return str2 == null;

		if (str2 == null)
			return false;

		return str1.equals(str2);
	}

	@Required
	@Email
	public String email;

	@Required
	public String password;

	@Required
	public UserType userType;

	@Required
	public String firstName;

	@Required
	public String lastName;

	public boolean isDeactivated = false;

	public User() {

	}

	public User(String email, String password, UserType userType,
			String firstName, String lastName) {
		this.email = email;
		this.password = password;
		this.userType = userType;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String toString() {
		return "email: " + email + " password: " + password + " usertype: "
				+ userType + " firstname: " + firstName + " lastname:"
				+ lastName;
	}

	public class DuplicateUserException extends Exception {
		public DuplicateUserException() {
			super();
		}

		public DuplicateUserException(String message) {
			super(message);
		}
	}

	/**
	 * inserts course into database
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean insertIntoDatabase() throws Exception {
		// todo add password encryption. currently stores password as simple
		// string.
		if (containsNullField())
			throw new NullPointerException();

		if (UserDB.getUser(email) != null) {
			throw new DuplicateUserException("User with E-mail \"" + email
					+ "\" already exists in system!");
		}

		try {
			UserDB.insertUser(this);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// check if any of the fields of this object is null.
	private boolean containsNullField() {
		return email == null || password == null || userType == null
				|| firstName == null || lastName == null;
	}

	/**
	 * returns list of all existing users.
	 */
	public static List<User> getAllUsers() {
		try {
			return UserDB.allUsers();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 * 
	 * @param email
	 * @return list of all users whose email match email param (fully or
	 *         partially).
	 * @throws SQLException
	 */
	public static List<User> searchByEmail(String email) throws SQLException {
		return UserDB.searchByEmail(email);

	}

	public static List<User> searchByEmailAndName(String email,
			String firstName, String lastName) throws SQLException {
		return UserDB.searchByEmailAndName(email, firstName, lastName);

	}

	public static List<User> searchdeactivatedUsers(String email,
			String firstName, String lastName) throws SQLException {
		return UserDB.searchDeactivated(email, firstName, lastName);

	}

	/**
	 * 
	 * @param email
	 * @return user object whose email fully matchers email param (active +
	 *         inactive users). return null if no user with email exists in
	 *         database.
	 */
	public static User getUserbyEmail(String email) throws Exception {
		return UserDB.getUser(email);
	}

	public static boolean restoreUser(String email) throws Exception {
		return UserDB.restoreUser(email) > 0;
	}
}
