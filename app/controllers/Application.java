package controllers;

import static scala.collection.JavaConversions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import JDBC.ClassDB;
import JDBC.UserDB;
import models.LoginModel;
import models.SchoolClass;
import models.User;
import models.User.UserType;
import play.*;
import play.data.Form;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {
	//Form<LoginModel> loginForm = Form.form(LoginModel.class);

	/**
	 * 
	 * @return view for user home page if user is logged in. return login page
	 *         otherwise.
	 */
	public Result index() {
		User user = Application.getLoggedUser();
		Form<LoginModel> loginForm = Form.form(LoginModel.class);
		if (user == null) {
			return ok(login.render(loginForm));
		}

		if (user.userType == UserType.ADMIN)
			return ok(adminHome.render());
		if (user.userType == UserType.STUDENT)
			return ok(studentHome.render());
		if (user.userType == UserType.TEACHER) {
			return teacherHome(false, user.email);
		}

		
		session().clear();
		return ok(login.render(loginForm));
	}

	/**
	 * 
	 * @return teacher home page displaying classes currently taught (current
	 *         semester only) if logged user is teacher.
	 */
	public Result currentClasses() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.TEACHER) {
			return unauthorized(message.render("Please log in to system"));
		}

		return teacherHome(false, user.email);
	}

	/**
	 * 
	 * @return teacher home page listing all classes assigned to user (current,
	 *         past and future) is logged user is teacher.
	 */
	public Result allClasses() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.TEACHER) {
			return unauthorized(message.render("Please log in to system"));
		}

		return teacherHome(true, user.email);
	}

	/**
	 * 
	 * @param displayAll
	 *            - true if home page should include all classes taught by a
	 *            teacher. (current, past and future). False if result should
	 *            include only classes taught in current semester.
	 * @param teacher
	 *            email.
	 * @return teacher home page.
	 */
	private Result teacherHome(boolean displayAll, String email) {
		try {
			List<SchoolClass> classes = ClassDB.teacherClasses(email);
			Collections.sort(classes, new Comparator<SchoolClass>() {
				public int compare(SchoolClass a, SchoolClass b) {
					if (a.semester.year == b.semester.year) {
						return b.semester.term.getID()
								- a.semester.term.getID();
					}
					return b.semester.year - a.semester.year;
				}
			});

			return ok(teacherHome.render(asScalaBuffer(classes), displayAll));
		} catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}

	}

	/**
	 * 
	 * log in action. Logs in user into system if "email" and "password" passed
	 * in request match an existing user in database.
	 */
	public Result login() {
		Form<LoginModel> loginForm = Form.form(LoginModel.class);
		Form<LoginModel> loginForm1 = loginForm.bindFromRequest();

		if (!loginForm1.hasErrors()) {
			session().clear();

			String email = loginForm1.get().email;
			try {
				User user = UserDB.getUser(email);

				session().put("email", email);
				session().put("firstName", user.firstName);
				session().put("lastName", user.lastName);
				session().put("userType", user.userType.toString());

			} catch (Exception e) {
				return internalServerError(message
						.render("Internal Server Error"));
			}
			return index();
		}
		return redirect("/");
	}

	/**
	 * logs out user.
	 * 
	 * @return home page.
	 */
	public Result logout() {
		session().clear();
		return index();
	}

	/**
	 * 
	 * @return currently logged in user; return null if user is not logged in.
	 */
	public static User getLoggedUser() {
		String userType = session().get("userType");
		if (userType == null)
			return null;
		User result = new User();
		result.userType = UserType.valueOf(userType);
		result.email = session().get("email");
		result.firstName = session().get("firstName");
		result.lastName = session().get("lastName");
		return result;
	}

	/**
	 * 
	 * @return view for currently logged in user account.
	 */
	public Result myAccount() {
		User user = Application.getLoggedUser();
		if (user == null) {
			return redirect("/");
		}

		return redirect(controllers.routes.userManager.displayUser(user.email));
	}
}
