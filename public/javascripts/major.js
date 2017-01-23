/**
 * 
 */
var modifyActive = false;
var deleteMajorForm = $("form[name = delete]");
var deleteMajorButton = deleteMajorForm.find("input");
var addCourse = $("li[name =addCourse]");
var removeCourse = $("form[name = removeCourse]");

var modifyButton = $("input[name = modify]");

modifyButton.click(function(){
	if(modifyActive === true){
		modifyActive = false;
		deleteMajorForm.addClass("hidden");
		addCourse.addClass("hidden");
		removeCourse.addClass("hidden");
		deleteMajorButton.val("");
		
	}	else {
		modifyActive = true;
		deleteMajorForm.removeClass("hidden");
		addCourse.removeClass("hidden");
		removeCourse.removeClass("hidden");
		deleteMajorButton.val("Delete Major");
		
	}
})
