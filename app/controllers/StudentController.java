package controllers;

import models.StudentProfile;
import models.User.UserType;
import play.mvc.Controller;
import play.mvc.Result;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import JDBC.CourseDB;
import JDBC.CourseDB.noSuchCourseException;
import JDBC.StudentProfileJDBC;
import models.Course;
import models.Department;
import models.Major;
import models.Semester;
import models.Semester.Term;
import models.StudentProfile.gradCheckResult;
import models.User;
import play.data.Form;
import static scala.collection.JavaConversions.*;
import views.html.*;
import views.html.helper.form;

public class StudentController extends Controller {
	/**
	 * 
	 * render list of current classes of logged in student and their schedules.
	 */
	public Result currentClasses() {
		String userType = session("userType");
		String email = session("email");
		if (email == null || userType == null
				|| !userType.equals(UserType.STUDENT.toString())) {
			return unauthorized(message.render("please log in as a Student"));
		}
		try {
			StudentProfile profile = StudentProfile.getStudentHistory(email);
			return ok(StudMyClasses.render(asScalaBuffer(profile
					.currentClasses())));
		} catch (Exception e) {
			return internalServerError(message.render("internal server error"));
		}

	}

	/**
	 * 
	 * render student history if logged account is student.
	 */
	public Result history() {
		User user = Application.getLoggedUser();
		String userType = session("userType");
		String email = session("email");
		if (email == null || userType == null
				|| !userType.equals(UserType.STUDENT.toString())) {
			return unauthorized(message.render("Please log in as a Student."));
		}
		try {
			StudentProfile profile = StudentProfile.getStudentHistory(email);
			return ok(StudentHistory.render(profile, "Your History", user));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(message.render("Internal Server Error"));
		}

	}

	/**
	 * 
	 * @param email
	 *            render transcript of a student identified by email. logged in
	 *            user must be a teacher or an admin.
	 */
	public Result transcipt(String email) {
		User user = Application.getLoggedUser();

		if (user == null
				|| (user.userType != UserType.TEACHER && user.userType != UserType.ADMIN)) {
			return unauthorized(message
					.render("You are not authorized to views this page. Please log in as an administratoror or a teacher."));
		}

		try {
			StudentProfile profile = StudentProfile.getStudentHistory(email);
			if (profile == null) {
				return badRequest(message.render("Not found."));
			}

			return ok(StudentHistory
					.render(profile, "Student Transcript", user));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(message.render("internal server error"));
		}
	}

	/**
	 * 
	 * perform graduation check.
	 * 
	 * @return JSon string of result.
	 * 
	 */
	public Result checkGraduation() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized("Please log in to system as administrator");
		}

		String email = Form.form().bindFromRequest().get("email");

		try {
			StudentProfile profile = StudentProfile.getStudentHistory(email);
			StudentProfile.gradCheckResult result = profile.graduationCheck();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.createArrayNode();
			JsonNodeFactory factory = JsonNodeFactory.instance;
			ObjectNode resJson = factory.objectNode();

			if (result.result) {
				resJson.put("result", true);
				resJson.put("message", "Student Satisfies all requirements");
				return ok(resJson.toString());
			} else {
				resJson.put("result", false);

				if (result.status == gradCheckResult.NO_MAJOR_SPECIFIED) {

					resJson.put("message",
							"There is no major specified for student");
					return ok(resJson.toString());
				}
				if (result.status == gradCheckResult.COURSE_REQUIREMENTS_FAILED) {

					resJson.put("message",
							"Student has not finished all the required courses for student's major");
					return ok(resJson.toString());
				}

				resJson.put("message",
						"Student does not have enough units completed for graduation");
				return ok(resJson.toString());
			}
		} catch (Exception e) {
			return internalServerError("Internal server error");
		}
	}

	/**
	 * 
	 * assign major to student.
	 */
	public Result addStudentToMajor() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to system as administrator"));
		}

		try {
			String email = Form.form().bindFromRequest().get("email");
			int major = Integer.parseInt(Form.form().bindFromRequest()
					.get("major"));
			Major.addStudentToMajor(email, major);
			return ok(message.render("major successfully assigned"));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(message.render("Could not assign major"));
		}
	}

	/**
	 * Request parameters: email - email of a student. update student's status
	 * to "graduated". redirect to student history page.
	 */
	public Result graduate() {
		try {
			String email = Form.form().bindFromRequest().get("email");
			StudentProfileJDBC.graduate(email);
			return redirect(controllers.routes.StudentController
					.transcipt(email));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(message.render("Could not assign major"));
		}
	}

	/**
	 * render page for assigning major to a student.
	 * 
	 * @return
	 */
	public Result renderAssignMajor() {
		User user = Application.getLoggedUser();
		String userType = session("userType");

		if (userType == null || !userType.equals(UserType.ADMIN.toString())) {
			return unauthorized("please log in as a Administrator");
		}
		String email = Form.form().bindFromRequest().get("email");
		return ok(AssignMajor.render(email));
	}

	/**
	 * 
	 * @param email
	 *            render student transcript in printable format.
	 */
	public Result printableTranscript(String email) {
		User user = Application.getLoggedUser();
		String userType = session("userType");

		if (userType == null || !userType.equals(UserType.ADMIN.toString())) {
			return unauthorized(message.render("Please log in as a Administrator"));
		}

		try {
			StudentProfile profile = StudentProfile.getStudentHistory(email);
			if (profile == null) {
				return badRequest(message.render("Not found"));
			}

			return ok(StudentHistory_Plain
					.render(profile, "Student Transcript"));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(message.render("internal server error"));
		}
	}

}
