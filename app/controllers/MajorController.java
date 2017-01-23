package controllers;

import models.Course;
import models.Major;
import models.User;
import models.User.UserType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;
import static scala.collection.JavaConversions.*;

public class MajorController extends Controller {

	/**
	 * render view for creating new major.
	 */
	public Result newMajor() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		Form<Major> majorForm = Form.form(Major.class);
		
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message
					.render("Please log in to the system as administrator"));
		}

		return ok(newMajor.render(majorForm));
	}
	/**
	 * add new major to the system specified in request.
	 * redirect to page displaying new major.
	 */
	public Result addMajor() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		
		Form<Major> majorForm = Form.form(Major.class);
		majorForm = majorForm.bindFromRequest();
		if (majorForm.hasErrors()) {
			return badRequest(newMajor.render(majorForm));
		}
		Major major = majorForm.get();

		int id;

		try {
			id = major.insertIntoDatabase();

			return redirect(controllers.routes.MajorController.majorDetails(id));
		} catch (Exception e) {
			return internalServerError(message
					.render("Could not add to database"));
		}

	}
	/**
	 * render view for major details specified by major id.
	 */
	public Result majorDetails(int id) {
		User user = Application.getLoggedUser();
		if (user == null) {
			return unauthorized(message.render("Please log into the system"));
		}
		try {
			Major major = Major.searchByID(id);
			
			if (major == null) {
				return notFound(message.render("Major not found"));
			}

			return ok(majorDetails.render(major, user.userType));
		} catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}
	}
	/**
	 * 
	 * add requirement course to major.
	 */
	public Result addCourseToMajor(int majID) {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		try {

			String dept = Form.form().bindFromRequest().get("dept");
			String courseNumber = Form.form().bindFromRequest().get("number");
			
			if (Major.addCourseToMajor(majID,
					Course.findCourse(dept, Integer.parseInt(courseNumber))
							.getCourseID())) {
				return redirect(controllers.routes.MajorController
						.majorDetails(majID));
			} else {
				return badRequest(message
						.render("This course is already in the list of requirements for this major!"));
			}

		} catch (Exception e) {
			
			return internalServerError(message
					.render("Could not add course to major"));
		}
	}

	/**
	 * remove requirement course from major.
	 * 
	 */
	public Result removeCourseFromMajor(int majID) {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		try {

			String courseNumber = Form.form().bindFromRequest().get("number");

			if (Major.removeCourseFromMajor(majID,
					Integer.parseInt(courseNumber))) {
				return redirect(controllers.routes.MajorController
						.majorDetails(majID));
			} else {
				return badRequest(message
						.render("Could not remove course from major."));
			}

		} catch (Exception e) {
			
			return internalServerError(message
					.render("Could not remove course from major."));
		}
	}
	/**
	 * delete major with id majID.
	 */
	public Result removeMajor(int majID) {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		try {
			Major.removeMajor(majID);
			return ok(message.render("Succesfully removed major from system."));

		} catch (Exception e) {
			
			return internalServerError(message
					.render("Could not remove major from system."));
		}
	}

	/**
	 * render view for displaying all majors present in the system.
	 *
	 */
	public Result AllMajors() {

		try {
			return ok(Majors.render(asScalaBuffer(Major.getAllMajors())));
			
		} catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}
	}
}
