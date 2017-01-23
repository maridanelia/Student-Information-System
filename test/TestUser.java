import models.User;
import models.User.UserType;

import org.junit.Test;

import play.test.*;
import JDBC.UserDB;
import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class TestUser {
	@Test
	public void testEqualsEqualUsers(){
		User user1 = userWithNonNullfields();
		User user2 = userWithNonNullfields();
		assertTrue(user1.equals(user2));
		assertTrue(user2.equals(user1));
	}
	@Test 
	public void tetsDifferentFirstName(){
		User user1 = userWithNonNullfields();
		User user2 = userWithNonNullfields();
		
		user2.firstName = "othername";
		
		assertFalse(user1.equals(user2));
		assertFalse(user2.equals(user1));
	}
	
	@Test 
	public void tetsDifferentLastName(){
		User user1 = userWithNonNullfields();
		User user2 = userWithNonNullfields();
		
		user2.lastName = "othername";
		
		assertFalse(user1.equals(user2));
		assertFalse(user2.equals(user1));
	}
//	
	@Test 
	public void tetsDifferentPassword(){
		User user1 = userWithNonNullfields();
		User user2 = userWithNonNullfields();
		
		user2.lastName = "othername";
		
		assertFalse(user1.equals(user2));
		assertFalse(user2.equals(user1));
	}
	
	@Test
	public void testUserTypeEnumGetID(){
		assertEquals(1, UserType.TEACHER.getID());
		assertEquals(2, UserType.STUDENT.getID());;
		assertEquals(3, UserType.ADMIN.getID());;
	}
	
	@Test
	public void testUserTypeGetByID(){
		assertEquals(UserType.TEACHER, UserType.getByID(1));
		assertEquals(UserType.STUDENT, UserType.getByID(2));
		assertEquals(UserType.ADMIN, UserType.getByID(3));
	}
	public User userWithNonNullfields() {
		User user = new User();
		user.email = "email@mail.com";
		user.password = "pass";
		user.firstName = "mari";
		user.lastName = "tester";
		user.userType = UserType.ADMIN;
		return user;
	}
}
