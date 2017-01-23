package JDBC;

import helpers.Hasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.User;
import models.User.UserType;
import play.db.DB;

public class UserDB {
	private static final String USER_TABLE = "user";

	/**
	 * 
	 * @param user
	 *            insert user into database.
	 * @throws SQLException
	 */
	public static boolean insertUser(User user) throws SQLException {
		if (user == null || user.email == null || user.firstName == null
				|| user.lastName == null || user.password == null
				|| user.userType == null) {
			throw new NullPointerException();
		}

		Connection connect = DB.getConnection();

		try {
			PreparedStatement stmt = connect
					.prepareStatement("insert into user values"
							+ "(?, ?, ?, ?,?);");

			stmt.setString(1, user.email.toLowerCase());
			stmt.setString(2, user.firstName);
			stmt.setString(3, user.lastName);

			stmt.setInt(4, user.userType.getID());
			stmt.setString(5, Hasher.hash(user.password));

			stmt.execute();

		} catch (Exception e) {
			return false;
		} finally {
			try {
				connect.close();
			} catch (Exception e) {

			}
		}

		return true;

	}

	//
	/**
	 * 
	 * @param email
	 * @param password
	 * @return return ACTIVE user with matching email and password.
	 * @throws SQLException
	 */
	public static User getUser(String email, String password)
			throws SQLException {

		if (email == null)
			throw new NullPointerException("email can't be null");
		if (password == null)
			throw new NullPointerException("password can't be null");
		if (isUserDeactivated(email))
			return null;

		Connection connect = DB.getConnection();

		try {
			User result = null;

			PreparedStatement stmt = connect
					.prepareStatement("select * from user where email = ? and password = ?");
			stmt.setString(1, email.toLowerCase());

			stmt.setString(2, Hasher.hash(password));
			if (stmt.execute()) {

				ResultSet resultSet = stmt.getResultSet();
				if (resultSet.next()) {
					return rowToUser(resultSet);
				}
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {

			connect.close();
		}
	}

	/**
	 * 
	 * @param email
	 * @return user object whose email fully matchers email param. return null
	 *         if no user with email exists in database.
	 */
	public static User getUser(String email) throws SQLException {
		if (email == null)
			throw new NullPointerException("email can't be null");

		boolean isDeactivated = false;
		if (isUserDeactivated(email))
			isDeactivated = true;

		Connection connect = DB.getConnection();

		try {
			User result = null;
			PreparedStatement stmt = connect
					.prepareStatement("select * from user where email = ?");
			stmt.setString(1, email.toLowerCase());

			if (stmt.execute()) {
				ResultSet resultSet = stmt.getResultSet();

				if (resultSet.next()) {
					User res = rowToUser(resultSet);
					if (res != null) {
						res.isDeactivated = isDeactivated;
					}
					return res;
				}
			}

			return result;
		} catch (Exception e) {
			return null;
		} finally {

			try {
				connect.close();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * 
	 * @return list of all users in the system.
	 * @throws SQLException
	 */
	public static List<User> allUsers() throws SQLException {
		Connection connect = DB.getConnection();

		try {
			ArrayList<User> res = new ArrayList<>();
			ResultSet resultSet = connect
					.createStatement()
					.executeQuery(
							"select * from User where user.email NOT IN (select * from removed_users)");
			while (resultSet.next()) {
				User user = rowToUser(resultSet);
				// if(isUserDeactivated(user.email)) continue;
				res.add(user);
			}

			return res;
		} finally {
			connect.close();
		}

	}

	/**
	 * 
	 * @param email
	 * @return return list of all users whose email contain substring equal to
	 *         param email.
	 * @throws SQLException
	 */
	public static List<User> searchByEmail(String email) throws SQLException {
		Connection connect = DB.getConnection();

		try {
			ArrayList<User> res = new ArrayList<>();
			PreparedStatement stmt = connect
					.prepareStatement("Select * from user where email like ? "
							+ "AND user.email NOT IN (select * from removed_users)");
			stmt.setString(1, "%" + email + "%");
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				User user = rowToUser(resultSet);
				// if (isUserDeactivated(user.email))
				// continue;
				res.add(user);
			}

			return res;
		} finally {
			connect.close();
		}

	}

	/**
	 * 
	 * @param email
	 * @param firstName
	 * @param lastName
	 * @return return active all users whose emails and names contain param
	 *         email, param lastname and param firstname as substrings.
	 * @throws SQLException
	 */
	public static List<User> searchByEmailAndName(String email,
			String firstName, String lastName) throws SQLException {
		Connection connect = DB.getConnection();

		try {
			ArrayList<User> res = new ArrayList<>();
			PreparedStatement stmt = connect
					.prepareStatement("Select * from user where lower(email) like ? AND lower(FirstName) like ? AND lower(lastName) like ?"
							+ "AND user.email NOT IN (select * from removed_users)");
			stmt.setString(1, "%" + email.toLowerCase() + "%");
			stmt.setString(2, "%" + firstName.toLowerCase() + "%");
			stmt.setString(3, "%" + lastName.toLowerCase() + "%");
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				User user = rowToUser(resultSet);
				//
				res.add(user);
			}

			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			connect.close();
		}

	}

	/**
	 * 
	 * @param email
	 * @param firstName
	 * @param lastName
	 * @return return all deactivated users whose emails and names contain param
	 *         email, param lastname and param firstname as substrings.
	 * @throws SQLException
	 */
	public static List<User> searchDeactivated(String email, String firstName,
			String lastName) throws SQLException {
		Connection connect = DB.getConnection();

		try {

			ArrayList<User> res = new ArrayList<>();
			PreparedStatement stmt = connect
					.prepareStatement("Select * from user where lower(email) like ? AND lower(FirstName) like ? AND lower(lastName) like ?"
							+ " AND user.email IN (select * from removed_users)");
			stmt.setString(1, "%" + email.toLowerCase() + "%");
			stmt.setString(2, "%" + firstName.toLowerCase() + "%");
			stmt.setString(3, "%" + lastName.toLowerCase() + "%");
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				User user = rowToUser(resultSet);
				//
				res.add(user);
			}

			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			connect.close();
		}

	}

	public static void deactivateUser(String email) throws SQLException {
		if (email == null)
			throw new NullPointerException();

		Connection connect = DB.getConnection();

		try {
			PreparedStatement stmt = connect
					.prepareStatement("insert into removed_users values"
							+ "(?);");

			stmt.setString(1, email.toLowerCase());

			stmt.execute();

		} catch (Exception e) {
			throw e;
		} finally {
			connect.close();

		}
	}

	public static int restoreUser(String email) throws SQLException {
		if (email == null)
			throw new NullPointerException();

		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("delete from removed_users where email= ?");
			stmt.setString(1, email.toLowerCase());
			return stmt.executeUpdate();
		} finally {
			connect.close();
		}
	}

	public static boolean isUserDeactivated(String email) throws SQLException {
		if (email == null)
			throw new NullPointerException();
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect
					.prepareStatement("select * from removed_users where email = ?");
			stmt.setString(1, email.toLowerCase());

			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			connect.close();
		}
	}

	public static boolean changePassword(String email, String oldPass,
			String newPass) {
		Connection connect = DB.getConnection();
		try {

			PreparedStatement stmt = connect
					.prepareStatement("update user set password = ? where email =? and password = ?");

			stmt.setString(1, Hasher.hash(newPass));
			stmt.setString(2, email.toLowerCase());
			stmt.setString(3, Hasher.hash(oldPass));

			int res = stmt.executeUpdate();

			return res > 0;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		} finally {
			try {
				connect.close();
			} catch (Exception e) {

			}
		}
	}

	private static User rowToUser(ResultSet resultSet) throws SQLException {
		User result = new User();

		result.email = resultSet.getString(1);
		result.firstName = resultSet.getString(2);
		result.lastName = resultSet.getString(3);
		result.userType = UserType.getByID(resultSet.getInt(4));

		return result;
	}
}
