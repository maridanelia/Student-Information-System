package models;

import java.sql.SQLException;
import java.util.List;

import JDBC.CourseDB;

public class Department {
	public static final int MAX_ID_LENGTH = 4;
	public String departmentID;
	public String departmentName;
	public List<Course> course;

	public Department() {

	}

	public Department(String departmentID, String departmentName) {
		this.departmentID = departmentID;
		this.departmentName = departmentName;
	}

	/**
	 * 
	 * @return list of all departments in thy database.
	 * @throws Exception
	 */
	public static List<Department> allDepartments() throws Exception {
		try {
			return CourseDB.getAllDepartments();
		} catch (SQLException e) {
			throw new Exception("Database exception");
		}
	}
}
