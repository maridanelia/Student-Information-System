package controllers;

import java.util.List;

import org.apache.http.protocol.HTTP;

import static scala.collection.JavaConversions.*;
import JDBC.UserDB;
import models.LoginModel;
import models.User;
import models.User.DuplicateUserException;
import models.User.UserType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import views.html.*;
import views.html.helper.form;

public class userManager extends Controller {
	//Form<User> userForm = Form.form(User.class);

	/**
	 * renders a form for entering new user into system.
	 */
	public Result newUser() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		Form<User> userForm = Form.form(User.class);
		return ok(newUser.render(userForm));
	}

	/**
	 * 
	 * renders list of all active users available in the system. if an email and name of the user was passed in
	 * the renders list of users whose email matches passed parameter (either
	 * fully or partially)
	 * 
	 * if no email was passed, renders all users in the system.
	 */
	public Result viewAllUsers() {
		
		List<User> allUsers;
		String email = Form.form().bindFromRequest().get("email");
		String firstName = Form.form().bindFromRequest().get("firstName");
		String lastName = Form.form().bindFromRequest().get("lastName");
		try {
			if (email == null) {
				email="";
			} 
			if(firstName == null){
				firstName="";
			}
			if(lastName==null){
				lastName="";
			}
			
			allUsers = User.searchByEmailAndName(email, firstName, lastName);
			
			return ok(users.render(asScalaBuffer(allUsers),"Active Users of Student Information System"));
		} catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}

	}
	
	public Result viewInactiveUsers(){
		List<User> allUsers;
		String email = Form.form().bindFromRequest().get("email");
		String firstName = Form.form().bindFromRequest().get("firstName");
		String lastName = Form.form().bindFromRequest().get("lastName");
		try {
			if (email == null) {
				email="";
			} 
			if(firstName == null){
				firstName="";
			}
			if(lastName==null){
				lastName="";
			}
			
			allUsers = User.searchdeactivatedUsers(email, firstName, lastName);
			
			return ok(users.render(asScalaBuffer(allUsers),"Deactivated Users of Student Information System"));
		} catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}
	}
	/**
	 * 
	 * add user to database.
	 */
	public Result addUser() throws Exception {
		User userLogged = Application.getLoggedUser();
		if (userLogged == null || userLogged.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		Form<User> userForm = Form.form(User.class);
		userForm.errors().clear();
		userForm = userForm.bindFromRequest();
		
		if (userForm.hasErrors()) {
			
			return ok(newUser.render(userForm));
		}
		User user = userForm.get();

		try {
			if (user.insertIntoDatabase()) {
				return ok(addUserResult.render("User successfully added"));
			} else {
				//todo change "ok" result type to "internalseervererror" type
				return ok(addUserResult.render("Failed to add user"));
			}
		} catch (DuplicateUserException e) {
			return ok(addUserResult.render(e.getMessage()));
		}

	}

	/**
	 * 
	 * @param email
	 * renders user profile of user based on email parameter.
	 */
	public Result displayUser(String email) {
		
		User userLogged = Application.getLoggedUser();
		if(userLogged == null){
			return unauthorized(message.render("Please log in to the system"));
		}
		try {
			User user = UserDB.getUser(email);
			
			if (user == null)
				return notFound(message.render("User not found"));
			return ok(userProfile.render(user, userLogged));
		} catch (Exception e) {
			return notFound(message.render("Internal Server Error"));
		}
	}
	/**
	 * changes password of user based on email, old password and new password specified in request.
	 * @return a message view notifying if change was successful.
	 * 
	 */
	public Result changePassword(){
		String email = Form.form().bindFromRequest()
				.get("email");
		
		String oldPass = Form.form().bindFromRequest()
				.get("old");
		String newPass = Form.form().bindFromRequest()
				.get("new");
		String confirm = Form.form().bindFromRequest()
				.get("confirm");
		if(!confirm.equals(newPass)){
			return badRequest(message.render("New password and confirm new password fields don't match."));
		}
		if(newPass.length() < 6){
			return badRequest(message.render("Password should be at least 6 characters long."));
		}
		if(UserDB.changePassword(email, oldPass, newPass)){
			return ok(message.render("Password succesfully changed."));
		}	
		return badRequest(message.render("Could not change password"));
	}

	/**
	 * marks user as deactivated based on email parameter retrieved from request. 
	 * 
	 * @return
	 */
	public Result deactivateUser() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		String email = Form.form().bindFromRequest().get("email");
		try {
			UserDB.deactivateUser(email);

		} catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}
		return redirect("/allUsers");
	}
	
	public Result restoreUser(){
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		String email = Form.form().bindFromRequest().get("email");
		
		try{
			if(User.restoreUser(email)){
				return ok(message.render("User restored"));
			}	else {
				return ok(message.render("User with email" + email+ "was not found among deactivated users"));
			}
		}	catch (Exception e){
			e.printStackTrace();
			return internalServerError(message.render("Could not restore user"));
		}
	}
}
