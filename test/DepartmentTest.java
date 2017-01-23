import static org.junit.Assert.*;
import static play.test.Helpers.*;

import java.util.List;

import models.Department;

import org.junit.Test;

import JDBC.CourseDB;

public class DepartmentTest {
	@Test
	public void testAllDepartments() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				try {
					List<Department> departments = CourseDB.getAllDepartments();
					assertTrue(departments.size() == 9);
					assertTrue(contains(new Department("CPSC",
							"Computer Science"), departments));
					assertTrue(contains(new Department("ACCT", "Accounting"),
							departments));
					assertTrue(contains(new Department("ANTH", "Anthropology"),
							departments));
				} catch (Exception e) {
					e.printStackTrace();
					fail("database exception");
				}
			}
		});
	}

	private boolean contains(Department department, List<Department> list) {
		for (Department d : list) {
			if (department == null)
				continue;
			if (stringEquals(d.departmentID, department.departmentID)
					&& stringEquals(d.departmentName, department.departmentName))
				return true;
		}

		return false;
	}

	private boolean stringEquals(String s1, String s2) {
		if (s1 == null) {
			return s2 == null;
		}

		return s1.equals(s2);
	}
}
