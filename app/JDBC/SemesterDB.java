package JDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import play.db.DB;
import models.Semester;
import models.Semester.Term;

public class SemesterDB {
	/**
	 * insert semester into database.
	 * 
	 * @param semester
	 * @throws SQLException
	 */
	public static void insertSemester(Semester semester) throws SQLException {
		if (semester == null || semester.startDate == null
				|| semester.endDate == null) {
			throw new NullPointerException();
		}
		Connection connect = DB.getConnection();
		try {
			PreparedStatement stmt = connect.prepareStatement(
					"insert into semesters values(null,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, semester.year);
			stmt.setInt(2, semester.term.getID());
			stmt.setLong(3, semester.startDate.getTime());
			stmt.setLong(4, semester.endDate.getTime());
			stmt.executeUpdate();
			if (semester.availableForEnrolment) {

				ResultSet key = stmt.getGeneratedKeys();
				key.next();
				int id = key.getInt(1);
				enableEnrolment(id, connect);
			}
			// todo insert active semester

		} finally {
			connect.close();
		}
	}

	/**
	 * 
	 * @return list of all semesters in the databse.
	 * @throws SQLException
	 */
	public static List<Semester> allSemesters() throws SQLException {
		ArrayList<Semester> result;
		Connection connect = DB.getConnection();
		try {
			result = new ArrayList<>();
			ResultSet resultSet = connect
					.createStatement()
					.executeQuery(
							"select * from semesters left join activeSemesters on semesters.semesterID = activeSemesters.semesterID");
			while (resultSet.next()) {
				result.add(rowToSemester(resultSet));
			}
			return result;
		} finally {
			connect.close();
		}

	}

	/**
	 * 
	 * @param id
	 * @return semester with given id.
	 * @throws SQLException
	 */
	public static Semester findByID(int id) throws SQLException {
		Connection connect = DB.getConnection();
		try {

			ResultSet resultSet = connect
					.createStatement()
					.executeQuery(
							"select * from semesters left join activeSemesters on semesters.semesterID = activeSemesters.semesterID where semesters.semesterID ="
									+ id);

			if (resultSet.next()) {
				return rowToSemester(resultSet);
			}
			return null;
		} finally {
			connect.close();
		}
	}

	public  static void setEnrollmentStatus(int id, boolean status) throws SQLException{
		Connection connect = DB.getConnection();
		try{
			if(status){
				enableEnrolment(id, connect);
			}	else {
				disableEnrollment(id, connect);
			}
		}	catch (Exception e){
			
		}  finally{
			connect.close();
		}
	}

	private static Semester rowToSemester(ResultSet resultSet)
			throws SQLException {
		Semester result = new Semester(resultSet.getInt(1));
		result.year = resultSet.getInt(2);
		result.term = Term.getByID(resultSet.getInt(3));

		result.startDate = new Date(resultSet.getLong(4));
		result.endDate = new Date(resultSet.getLong(5));

		if (resultSet.getObject(6) != null) {
			result.availableForEnrolment = true;
		}

		return result;
	}
	private static boolean enableEnrolment(int id, Connection connect) {
		
		try {
			PreparedStatement stmt = connect
					.prepareStatement("insert into activeSemesters values(?)");
			stmt.setInt(1, id);
			stmt.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean disableEnrollment(int id, Connection connect) {
		
		try {
			PreparedStatement stmt = connect
					.prepareStatement("delete from activeSemesters where semesterID = "+id);
			
			System.out.println(stmt.executeUpdate());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
}
