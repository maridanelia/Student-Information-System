package controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import JDBC.CourseDB;
import JDBC.CourseDB.noSuchCourseException;
import models.Course;
import models.Department;
import models.SchoolClass;
import models.Semester;
import models.Semester.Term;
import models.User.UserType;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.collection.mutable.HashMap;
import static scala.collection.JavaConversions.*;
import views.html.*;
import views.html.helper.form;

public class CourseController extends Controller {
	//private Form<Course> courseForm = Form.form(Course.class);

	/**
	 * renders form for entering new course into system
	 * 
	 * @return
	 */
	public Result newCourse() throws Exception {
		Form<Course> courseForm = Form.form(Course.class);
		courseForm.errors().clear();
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		return ok(newCourse.render(courseForm,
				asScalaBuffer(Department.allDepartments())));
	}

	/**
	 * adds course retreived from request to database
	 * 
	 * @return
	 */
	public Result addCourse() throws Exception{
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		Form<Course> courseForm = Form.form(Course.class);
		courseForm.errors().clear();
		courseForm = courseForm.bindFromRequest();

	
		if (courseForm.hasErrors()) {
			
			return ok(newCourse.render(courseForm,
					asScalaBuffer(Department.allDepartments())));
		}
		Course course = courseForm.get();
		try {
			course.insertIntoDatabase();
			

			return redirect(routes.CourseController.courseDetail(course.departmentID, course.courseNumber));

		} catch (Exception e) {
			return internalServerError(message.render("could not add course"));
		}

	}

	public Result deleteCourse(){
		
		try{
			int courseID = Integer.parseInt(Form.form().bindFromRequest().get("id"));
			
			if(Course.deleteCourse(courseID)){
				return ok(message.render("Course successfully deleted"));
			}	else {
				return ok(message.render("Course could not be deleted"));
			}
		}	catch (IllegalArgumentException e){
			return badRequest(message.render(e.getMessage()));
		}	catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}
	}
	/**
	 * 
	 * @return view of all course available in system.
	 */
	public Result viewAllCourses() {
		try {
			List<Course> allCourses = Course.allCourses();
			return ok(courses.render(asScalaBuffer(allCourses)));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(message.render("Internal Server Error"));
		}

	}
	
	public Result viewCoursesByDepartment(String departmentID) {
		try{
			return ok(courses.render(asScalaBuffer(Course.coursesByDept(departmentID))));
		} catch(Exception e){
			return internalServerError(message.render("Internal Server Error"));
		}
	}
	public Result searchCoursesByDepartment() {
		String dept = Form.form().bindFromRequest().get("dept");
		if(dept == null){
			return redirect(routes.CourseController.viewAllCourses());
		}
		return redirect(routes.CourseController.viewCoursesByDepartment(dept));
	}
	/**
	 * 
	 * @param departmentID
	 * @param courseNumber
	 * @return return view of course details with specified department and courseNumber.
	 */
	public Result courseDetail(String departmentID, int courseNumber) {
		
		try {
			Course course = Course.findCourse(departmentID, courseNumber);
			
			if(course == null){
				return notFound(message.render("Not found"));
			}			

			return ok(courseDetails.render(course, Application.getLoggedUser()));
		} catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}

	}
	/**
	 * request parameters
	 * 	"id" - unique id of course for which prerequisite is added.
	 * 	"prepreqDept" - department string id of prerequisite to be added.
	 * 	"prereqNumber" - course number of prerequisite to be added.
	 * add prerequisite to course if course with prereqNumber and prepreqDept exists in the database.
	 */
	public Result addPrerequisite() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		String dept = Form.form().bindFromRequest().get("prereqDept");
		if (dept == null || dept.equals("")) {
			return internalServerError("no department specified");
		}

		String courseNumber = Form.form().bindFromRequest().get("prereqNumber");
		if (courseNumber == null || courseNumber.equals("")) {
			return internalServerError("no course number specified");
		}
		String idStr = Form.form().bindFromRequest().get("prereqNumber");

		
		if (dept.length() > Department.MAX_ID_LENGTH) {
			return badRequest("length of department length should not be more than "
					+ Department.MAX_ID_LENGTH);
		}

		try {
			int id = Integer.parseInt(Form.form().bindFromRequest().get("id"));

			int prereqID = Course.addPrerequisiteToDatabase(id, dept,
					Integer.parseInt(courseNumber));

			return ok("" + prereqID);
		} catch (noSuchCourseException e) {
			return badRequest("course " + dept + courseNumber + " not found");
		} catch (Exception e) {
			return internalServerError("error");
		}

	}

	/**
	 * request parameters
	 * 	"id" - unique course id.
	 * 	"description" - new description for course.
	 * 	"units" - new number of units for course.
	 * modify course details.
	 */
	public Result modifyCourse() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		String description = Form.form().bindFromRequest().get("description");
		String units = Form.form().bindFromRequest().get("units");
		int id = Integer.parseInt(Form.form().bindFromRequest().get("id"));

		try {
			if (description != null) {
				Course.updateDescription(id, description);
			}
			if (units != null) {

				Course.updateUnits(id, Integer.parseInt(units));
			}
		} catch (Exception e) {
			return internalServerError();
		}
		return ok();
	}
	/**
	 * remove prerequisite from course.
	 * 
	 */
	public Result removePrerequisite() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		try {
			int courseID = Integer.parseInt(Form.form().bindFromRequest()
					.get("id"));
			int prereqID = Integer.parseInt(Form.form().bindFromRequest()
					.get("prereqID"));
			Course.removePrerequisite(courseID, prereqID);
			return ok();
		} catch (Exception e) {
			return internalServerError();
		}

	}

	
}
