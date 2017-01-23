/**
 * 
 */
var OK = 200;
var NOT_FOUND = 404;
var INTERNAL_ERROR = 500;
var BAD_REQUEST = 400;
var UNAUTHORIZED=401;

var gradingEnabled = false;

var gradeButton = $('input[name = grade]');
var doneButton = $('input[name=done]');
var gradeFields = $('select[name=grade]');

var gradeFields = $('select[name = grade]');

gradeButton.click(function() {
	gradingEnabled = true;
	doneButton.removeClass("hidden");
	gradeFields.removeClass("hidden");
});

doneButton.click(function() {
	gradingEnabled = false;
	doneButton.addClass("hidden");
	gradeFields.addClass("hidden");
	var grades = getChangedGrades();
	sendGrades(grades);
});
var sendGrades = function(grades){
	if(jQuery.isEmptyObject(grades)) {

		
		return;
	}
	var request = new XMLHttpRequest();
	
	request.onreadystatechange = function(){
		console.log(request.readyState);
		console.log(request.status);
		if(request.readyState === 4){
			if(request.status === OK){
				console.log("everything is good");
				window.location.reload();
			}	else {
				if(request.status == UNAUTHORIZED){
					$('div[name = message]').append($("<p> You are not authorized to grade students in this class.</p>"));
					$('div[name = message]').removeClass("hidden");
				}
			}
		}
	}
	
	
	request.open("POST", '/grade', true);
	
	
	request.setRequestHeader("Content-type",
	"application/json");
	var id = $('div[name=classID]').attr('classID');
	var body = {};
	body["id"] = id;
	
	console.log(JSON.stringify(grades));
	console.log(grades);
	body["grades"] = grades;
	console.log(body);
	body = JSON.stringify(body);
	console.log(body);
	request.setRequestHeader("Content-length", body.length);
	request.send(body);
}
var getChangedGrades = function() {
	var gradeChanges = {};
	for (var i = 0; i < gradeFields.length; i++) {
		
		var initGrade = gradeFields.eq(i).attr('initial');
		var newGrade = gradeFields.eq(i).val();
		console.log(initGrade);
		console.log(newGrade)
		if (initGrade !== undefined && initGrade !== newGrade) {
			gradeChanges[gradeFields.eq(i).attr('student')] = newGrade;
		}	

	}
	
	console.log(gradeChanges);
	return gradeChanges;
}