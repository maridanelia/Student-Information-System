package models;

import play.data.validation.Constraints.Required;
import JDBC.UserDB;

public class LoginModel {
	@Required
	public String email;

	@Required
	public String password;

	//
	public String validate() {

		try {
			if (UserDB.getUser(email, password) != null) {
				return null;

			}
		} catch (Exception e) {
			return "unable to login";
		}
		return "invalid Username/Password";
	}
}
