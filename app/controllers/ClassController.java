package controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.h2.engine.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
import com.sun.javafx.scene.control.behavior.TextBinding;
import com.fasterxml.jackson.core.type.TypeReference;

import JDBC.ClassDB;
import JDBC.CourseDB;
import JDBC.SemesterDB;
import models.Course;
import models.Department;
import models.SchoolClass;
import models.SchoolClass.Grade;
import models.SchoolClass.ScheduleItem;
import models.SchoolClass.Weekday;
import models.Semester;
import models.StudentProfile;
import models.User;
import models.User.UserType;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import static scala.collection.JavaConversions.*;
import views.html.*;
import views.html.helper.form;

public class ClassController extends Controller {
	//Form<SchoolClass> classForm = Form.form(SchoolClass.class);

	/**
	 * 
	 * modify class size for class with id specified in request.
	 */
	public Result modifySize() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message
					.render("Please log in to the system as administrator"));
		}
		
		try {
			Integer size = Integer.parseInt(Form.form().bindFromRequest()
					.get("size"));
			Integer classID = Integer.parseInt(Form.form().bindFromRequest()
					.get("id"));
			if (size < 0) {
				return badRequest(message
						.render("Class Size can't be negative"));
			}

			SchoolClass.addOrUpdateClassSize(classID, size);
			return redirect(controllers.routes.ClassController
					.classDetails(classID));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(message.render("Could not update size"));
		}
	}

	/**
	 * request parameters: "id": class ID "email": teacher email of teacher to
	 * be assigned to class. Assign new teacher to class with id specified in
	 * request.
	 */
	public Result modifyTeacher() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message
					.render("Please log in to the system as administrator"));
		}

		try {
			Integer classID = Integer.parseInt(Form.form().bindFromRequest()
					.get("id"));
			String teacherEmail = Form.form().bindFromRequest().get("email");

			User teacher = User.getUserbyEmail(teacherEmail);
			if (teacher == null || teacher.userType != UserType.TEACHER) {
				return badRequest(message
						.render("Could not find teacher with E-mail "
								+ teacherEmail));
			}
			SchoolClass.addOrUpdateTeacher(classID, teacher.email);
			return redirect(controllers.routes.ClassController
					.classDetails(classID));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(message
					.render("Could not update teacher information."));
		}
	}

	/**
	 * request parameters: "id": class ID "location": String describing new
	 * location to be assigned to class. Assign new location to class with id
	 * specified in request.
	 */
	public Result modifyLocation() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message
					.render("Please log in to the system as administrator"));
		}

		try {
			Integer classID = Integer.parseInt(Form.form().bindFromRequest()
					.get("id"));
			String location = Form.form().bindFromRequest().get("location");

			SchoolClass.addOrUpdateLocation(classID, location);
			return redirect(controllers.routes.ClassController
					.classDetails(classID));
		} catch (Exception e) {
			return internalServerError(message
					.render("Could not update teacher information."));
		}
	}

	/**
	 * 
	 * @return view for creating a new class
	 */
	public Result newClassAction() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to system"));
		}
		Form<SchoolClass> classForm = Form.form(SchoolClass.class);
		classForm.errors().clear();
		try {
			List<Semester> allSemesters = Semester.allSemesters();

			Collections.sort(allSemesters, new Comparator<Semester>() {
				public int compare(Semester a, Semester b) {
					if (a.year == b.year) {
						return b.term.getID() - a.term.getID();
					} else {
						return b.year - a.year;
					}
				}
			});
			return ok(newClass.render(classForm,
					asScalaBuffer(Department.allDepartments()),
					asScalaBuffer(allSemesters)));
		} catch (Exception e) {		
			return internalServerError(message.render("Internal Server Error"));
		}

	}

	/**
	 * add new schoolClass to the system. If request parameters do not satisfy
	 * validation criteria, redirect to new class view. If addition is
	 * successfull, redirect to the page displaying details of the new class.
	 * 
	 */
	public Result addClass() throws Exception {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to system"));
		}
		Form<SchoolClass> classForm = Form.form(SchoolClass.class);
		classForm.errors().clear();

		try {
			SchoolClass newClass = processAddRequest(classForm);
			if (classForm.hasErrors()) {
				List<Semester> allSemesters = Semester.allSemesters();
				return ok(views.html.newClass.render(classForm,
						asScalaBuffer(Department.allDepartments()),
						asScalaBuffer(allSemesters)));
			}
			int id = newClass.insertIntoDatabase();
			UserType userType = UserType.valueOf(session().get("userType"));
			return redirect(routes.ClassController.classDetails(id));
			//return ok(classDetails.render(SchoolClass.searchByID(id), userType));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}
	/**
	 * 
	 * delete class.
	 */
	public Result deleteClass(){
		try{
			int classID = Integer.parseInt(Form.form().bindFromRequest().get("id"));
			
			if(SchoolClass.removeClass(classID)){
				return ok(message.render("Class successfully deleted"));
			}	else {
				return ok(message.render("Class could not be deleted"));
			}
		}	catch (IllegalArgumentException e){
			return badRequest(message.render(e.getMessage()));
		}	catch (Exception e) {
			return internalServerError(message.render("Internal Server Error"));
		}
	}
	/**
	 * 
	 * @param id
	 *            - id of requested class.
	 * @return view for class details of requested class.
	 * 
	 */
	public Result classDetails(int id) throws Exception {

		SchoolClass cl = ClassDB.getClassFull(id);
		if(cl == null){
			return notFound(message.render("Not Found"));
		}
		String user = session().get("userType");
		if (cl.spaceLeft < 0) {
			cl.spaceLeft = 0;
		}
		if (user == null)
			return unauthorized(message.render("Please log into system."));
		UserType userType = UserType.valueOf(session().get("userType"));
		
		return ok(classDetails.render(cl, userType));

	}

	/**
	 * request parameters: "id" - id of a class to which textbook is added;
	 * "textbook" - string title of a textbook to be added. add textbook to the
	 * class.
	 */
	public Result addTextBook() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized("Please log in to system");
		}
		DynamicForm requestData = Form.form().bindFromRequest();
		try {

			int classID = Integer.parseInt(requestData.get("id"));
			String textBook = requestData.get("textbook");

			if (textBook == null || textBook.length() == 0) {
				return badRequest("Textbook title cannot be empty");
			}
			SchoolClass.addTextBook(classID, textBook);
			return ok("Textbook added successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError("internal server error");
		}
	}

	/**
	 * /** request parameters: "id" - id of a class to which textbook is added;
	 * "textbook" - string title of a textbook to be added. Remove textbook from
	 * the class.
	 */

	public Result removeTextBook() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized("Please log in to system");
		}
		DynamicForm requestData = Form.form().bindFromRequest();
		try {

			int classID = Integer.parseInt(requestData.get("id"));
			String textBook = requestData.get("textbook");

			if (textBook == null || textBook.length() == 0) {
				return badRequest("Textbook title cannot be empty");
			}
			SchoolClass.removeTextbook(classID, textBook);
			return ok();
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError(e.getMessage());
		}
	}

	/**
	 * request parameters: "id" - id of a class to which schedule is added;
	 * "Weekday" - day of the week of schedule item. "start" - time start of the
	 * class. "end" - time specifying end of the class. Add schedule item to the
	 * class schedule.
	 * 
	 * @return unique database id of the schedule item that is added to the
	 *         class if request is successful.
	 */
	public Result addScheduleItem() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized("Please log in to system");
		}
		DynamicForm requestData = Form.form().bindFromRequest();
		try {
			int classID = Integer.parseInt(requestData.get("id"));
			String classDay = requestData.get("weekday");
			String start = requestData.get("start");
			String end = requestData.get("end");
			ScheduleItem item = new ScheduleItem(Weekday.valueOf(classDay),
					start, end);
			int res = SchoolClass.insertScheduleItem(classID, item);

			return ok("" + res);
		} catch (Exception e) {
			
			return internalServerError(message.render("Internal Server Error"));
		}
	}

	/**
	 * request parameters: "id" - unique id of schedule item. remove schedule
	 * item from class schedule.
	 */
	public Result removeScheduleItem() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to system"));
		}
		DynamicForm requestData = Form.form().bindFromRequest();
		try {

			int id = Integer.parseInt(requestData.get("id"));
			if (SchoolClass.removeScheduleItem(id)) {
				return ok();
			}
			return notFound();
		} catch (Exception e) {
			
			return internalServerError(message.render("Internal Server Error"));
		}
	}

	/**
	 * 
	 * @param semesterID
	 * @param dept
	 *            - department
	 * @return view of all classes in semester with semesterID and department
	 *         dept.
	 * 
	 */
	public Result classesInDepartment(int semesterID, String dept)
			throws Exception {
		List<SchoolClass> classList = SchoolClass.search(semesterID, dept);
		String result = " zis is rezult  for semester "
				+ SemesterDB.findByID(semesterID) + " " + dept;

		for (SchoolClass s : classList) {
			result = result + s.course.courseName + "   ";
		}
		return ok(Classes.render(null, null, asScalaBuffer(classList),
				SemesterDB.findByID(semesterID), dept));
	}

	/**
	 * 
	 * render view all classes view.
	 */
	public Result allClasses() throws Exception {
		return ok(Classes.render(asScalaBuffer(Semester.allSemesters()),
				asScalaBuffer(Department.allDepartments()), null, null, null));
	}

	public Result classList() throws Exception {
		DynamicForm requestData = Form.form().bindFromRequest();
		String semester = requestData.get("semester");
		String department = requestData.get("department");
		return redirect(routes.ClassController.classesInDepartment(
				Integer.parseInt(semester), department));
	}

	/**
	 * add student that is logged in the system to class with id specified in
	 * request.
	 * 
	 */
	public Result addStudent() {

		User user = Application.getLoggedUser();

		if (user == null || user.userType != UserType.STUDENT)
			return unauthorized(message
					.render("please log in with student account"));
		DynamicForm requestData = Form.form().bindFromRequest();

		try {

			int classID = Integer.parseInt(requestData.get("id"));
			SchoolClass cl = SchoolClass.searchByID(classID);
			if (cl == null) {
				return badRequest(message.render("Class not found"));
			}

			if (!cl.semester.availableForEnrolment) {
				return ok(message
						.render("Could not enroll. Enrollment for the class is closed"));
			}

			List<Course> prerequisites = cl.course.prerequisites;
			StudentProfile profile = StudentProfile
					.getStudentHistory(user.email);
			for (Course prereq : prerequisites) {
				if (profile.hasCompletedCourse(prereq.getCourseID())) {
					continue;
				} else {
					return badRequest(message
							.render("Prerequisite requirements for this class have not been satisfied"));
				}
			}
			if (SchoolClass.addStudentToClass(classID, user.email)) {
				return ok(message.render("Successfully added to the class"));
			} else {
				return internalServerError(message
						.render("Could not add to the class"));
			}
		} catch (Exception e) {
			// e.printStackTrace();
			return internalServerError("Internal Server Error ");
		}

	}

	/**
	 * Drop student that is logged in the system from class with id specified in
	 * request.
	 * 
	 */
	public Result dropStudent() {
		User user = Application.getLoggedUser();

		if (user == null || user.userType != UserType.STUDENT)
			return unauthorized(message
					.render("please log in with student account"));
		DynamicForm requestData = Form.form().bindFromRequest();

		try {

			int classID = Integer.parseInt(requestData.get("id"));

			SchoolClass cl = SchoolClass.searchByID(classID);
			if (cl == null) {
				return badRequest(message.render("Class not found"));
			}

			if (!cl.semester.availableForEnrolment) {
				return ok(message
						.render("Could not drop from class. Enrollment period for the class is closed"));
			}
			if (SchoolClass.dropStudentFromClass(classID, user.email)) {
				return ok(message.render("successfully dropped from class"));
			} else {
				return internalServerError("Could not drop from the class");
			}
		} catch (Exception e) {
			// e.printStackTrace();
			return internalServerError("Internal Server Error ");
		}
	}

	/**
	 * request parameters: "id" - id of a from which student is dropped. "email"
	 * - email of a student who is to be dropped.
	 * 
	 * if logged user is an administrator, drop requested student from requested
	 * class.
	 */
	public Result dropStudentAdmin() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN)
			return unauthorized(message.render("please log in as administator"));

		try {
			DynamicForm requestData = Form.form().bindFromRequest();
			int classID = Integer.parseInt(requestData.get("id"));
			String email = requestData.get("email");
			if (SchoolClass.dropStudentFromClass(classID, email)) {
				return redirect(routes.ClassController.classRoll(classID));
			} else {
				return internalServerError("Could not drop from the class");
			}
		} catch (Exception e) {
			return internalServerError("Internal Server Error ");
		}

	}

	/**
	 * 
	 * @param classID
	 * @return view of class roll of class with id classID.
	 */
	public Result classRoll(int classID) throws Exception {
		User user = Application.getLoggedUser();
		if (user == null) {
			return unauthorized(message.render("Please log in to system"));
		}
		SchoolClass cl = SchoolClass.searchByID(classID);
		if(cl == null){
			return notFound(message.render("Not Found"));
		}
		
		return ok(classRoll.render(cl, asScalaBuffer(cl.getClassRoll()),
				cl.getGrades(), Application.getLoggedUser().userType));
	}

	/**
	 * request parameters: "id" - unique id of a class. "grades" - json object
	 * specifying graes for the class. grade class.
	 */
	public Result gradeClass() throws Exception {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.TEACHER) {
			return unauthorized(message.render("Please log in to system"));
		}

		JsonNode json = request().body().asJson();
		String idStr = json.path("id").toString();
		int id = Integer.parseInt(idStr.substring(1, idStr.length() - 1));

		JsonNode gradesJson = json.path("grades");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, SchoolClass.Grade> grades = mapper.readValue(
				gradesJson.toString(),
				new TypeReference<Map<String, SchoolClass.Grade>>() {
				});
		
		try {
			SchoolClass cl = SchoolClass.searchByID(id);
			if (cl.teacher == null
					|| !cl.teacher.email.toLowerCase().equals(
							user.email.toLowerCase())) {
				return unauthorized("You are not authorized to grade students in this class");
			}
			SchoolClass.updateGrades(id, grades);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ok();
	}

	/*
	 * parse size of the class from request and set newClass.classSize parameter
	 * to result. If size value in request is invalid, add error to errors list.
	 */
	private boolean parseSize(DynamicForm requestData,
			List<ValidationError> errors, SchoolClass newClass) {
		String sizeStr = requestData.get("size");
		if (sizeStr == null || sizeStr.length() == 0) {
			errors.add(new ValidationError("Class Size",
					"Class size limit is required input"));
			return false;
		}

		try {
			int size = Integer.parseInt(sizeStr);

			if (size < 0) {
				errors.add(new ValidationError("Class Size",
						"class size cannot be negative"));
				return false;
			}
			newClass.classSize = size;
			return true;
		} catch (Exception e) {
			errors.add(new ValidationError("Class Size",
					"incorrect number format for class size"));
			return false;
		}

	}

	/*
	 * parse size of the class from request and set newClass.teacher parameter
	 * to result. 
	 * If teacher email value in request is invalid or is not found in database, add error to
	 * errors list.
	 */
	private boolean parseTeacher(DynamicForm requestData,
			List<ValidationError> errors, SchoolClass newClass)
			throws Exception {
		String teacherEamil = requestData.get("teacher");
		if (teacherEamil != null && teacherEamil.length() != 0) {
			User teacher = User.getUserbyEmail(teacherEamil);
			if (teacher == null || teacher.userType != UserType.TEACHER) {
				errors.add(new ValidationError("Teacher",
						"Teacher with E-mail " + teacherEamil + " not found"));
				return false;
			}
			newClass.teacher = teacher;
			return true;

		}
		return true;
	}

	/*
	 * parse course information from request and set newClass.course parameter
	 * to result based on department and courseNumber specified in request.
	 *  
	 * if course in request is invalid or is not found in database, add error to errors list.
	 */
	private boolean parseCourse(DynamicForm requestData,
			List<ValidationError> errors, SchoolClass newClass)
			throws Exception {
		String department = requestData.get("department");
		int courseNumber;

		if (department == null || department.length() == 0) {
			errors.add(new ValidationError("Course",
					"course department not specified"));
			return false;
		}

		try {
			courseNumber = Integer.parseInt(requestData.get("courseNumber"));
		} catch (Exception e) {
			errors.add(new ValidationError("Course Number",
					"incorrect number format for course number"));
			return false;
		}
		Course course = Course.findCourse(department, courseNumber);
		if (course == null) {
			errors.add(new ValidationError("course", "course " + department
					+ courseNumber + " not found"));
			return false;
		}

		newClass.course = course;
		return true;
	}
	/*
	 * parse location from request.
	 * if location is not specified, add error to errors list.
	 */
	private void parseLocation(DynamicForm requestData,
			List<ValidationError> errors, SchoolClass newClass) {
		String location = requestData.get("location");
		if (location != null && location.length() > 0) {
			newClass.location = location;
		}
	}
	/*
	 * return new schoolClass based on request.
	 */
	private SchoolClass processAddRequest(Form<SchoolClass> classForm) throws Exception {
		SchoolClass newClass = new SchoolClass();

		List<ValidationError> errors = new ArrayList<ValidationError>();
		DynamicForm requestData = Form.form().bindFromRequest();
		classForm.errors().put("Class", errors);

		parseCourse(requestData, errors, newClass);
		parseSize(requestData, errors, newClass);
		parseTeacher(requestData, errors, newClass);
		parseLocation(requestData, errors, newClass);

		if(requestData
				.get("semester") == null){
			errors.add(new ValidationError("Semester",
					"Semester can't be empty"));
		}	else {
			newClass.semester = new Semester(Integer.parseInt(requestData
					.get("semester")));
		}
		

		if (errors.size() == 0) {
			classForm.errors().clear();
		}

		return newClass;
	}

}
