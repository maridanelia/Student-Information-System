package controllers;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;









import models.Semester;
import models.User;
import models.User.UserType;
import play.data.Form;
import play.data.format.Formatters;
import play.mvc.Controller;
import play.mvc.Result;
import static scala.collection.JavaConversions.*;
import views.html.*;
import views.html.helper.form;

import java.util.Locale;

import JDBC.SemesterDB;

public class SemesterManagment extends Controller {
	//private Form<Semester> semesterForm = Form.form(Semester.class);

	/**
	 * 
	 * Add semester to the database.
	 * redirect to new Semester page if request data is invalid.
	 */
	public Result addSemester() {
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		Form<Semester> semesterForm = Form.form(Semester.class);
		Formatters.register(Date.class, new Formatters.SimpleFormatter<Date>() {
			@Override
			public Date parse(String input, Locale arg1) {
				
				try {
					
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//				
					return format.parse(input);
				} catch (Exception e) {
					return null;
				}
			}

			@Override
			public String print(Date date, Locale arg1) {
				
				return date.toString();
			}
		});
		semesterForm = Form.form(Semester.class).bindFromRequest();
		if (semesterForm.hasErrors()) {
			return redirect("/newSemester");
		}		
		Semester semester = semesterForm.get();
		try{
			semester.insertIntoDatabase();
			return ok(message.render("Semester Successfully added"));
		}	catch (Exception e){
			e.printStackTrace();
			return internalServerError(message.render("Could not add semester"));
		}
	}

	/**
	 * 
	 * render page for creating a new semester.
	 */
	public Result semesterForm() {
		Form<Semester> semesterForm = Form.form(Semester.class);
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		return ok(newSemester.render(semesterForm));
	}
	
	/**
	 * 
	 * render view to display all semesters existing in the system.
	 */
	public Result allSemesters(){
		User user = Application.getLoggedUser();
		if (user == null || user.userType != UserType.ADMIN) {
			return unauthorized(message.render("Please log in to the system as administrator"));
		}
		try{
			List<Semester> sems = SemesterDB.allSemesters();
			
			return ok(viewSemesters.render(asScalaBuffer(sems)));
			
		}	catch (Exception e){			
			return internalServerError(message.render("Internal Server Error"));
		}
	}
	
	public Result closeEnrollment(){
		
		try{
			int id = Integer.parseInt(Form.form().bindFromRequest().get("id"));
			SemesterDB.setEnrollmentStatus(id, false);
			return redirect(routes.SemesterManagment.allSemesters());
		}	catch (Exception e){
			return internalServerError(message.render("Internal Server Error"));
		} 
	}
	
	public Result openEnrollment(){
		
		try{
			int id = Integer.parseInt(Form.form().bindFromRequest().get("id"));
			SemesterDB.setEnrollmentStatus(id, true);
			return redirect(routes.SemesterManagment.allSemesters());
		}	catch (Exception e){
			return internalServerError(message.render("Internal Server Error"));
		} 
	}
}
