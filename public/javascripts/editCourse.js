/**
 * 
 */
var OK = 200;
var NOT_FOUND = 400;
var INTERNAL_ERROR = 500;
var UNAUTHORIZED=401;

var prereqFormEnabled = false;
var inputs = $('#addPrereqForm');
var prereqList = $('ul');
var id = $('input[name=courseIDcur]').val();

var editEnabled = false;

var initialDescription = $('textarea[name=description]').val();
var initialUnits = $('input[name=units]').val();

// add functionality to edit button. enables editing course description and
// course units.
$('input[name = edit]').click(function() {
	enableEdit();
});
// adds functionality to remove prerequisite to removePreerquisite buttons.
(function() {
	var prereqItems = $('li');
	// iterate through preerquisite list items, each containing text for course
	// code and button for removal.
	// add functionality to remove prerequisite to each button.
	for (var i = 0; i < prereqItems.length; i++) {
		var prereqItem = prereqItems.eq(i);
		
		var prereqButton = prereqItem.find('input');
		
		prereqButton.click(function() {
			removePrereq($(this).parent());
		});
	}
})(

);

var removePrereq = function(prereqID) {

}
// add functionality to "done" button. if course description or units were
// modified on web-page, send corresponding update request to server. finish
// editing by disabling description and unit fields.
$('input[name = doneEdit]').click(
		function() {
			disableEdit();
			var newDesc = null;
			var newUnits = null;

			if (initialDescription !== $('textarea[name=description]').val()) {
				newDesc = $('textarea[name=description]').val();
			}

			if (initialUnits !== $('input[name=units]').val()) {
				newUnits = $('input[name=units]').val();
			}

			var request = new XMLHttpRequest();
			request.onreadystatechange = function() {

				if (request.readyState === 4 && request.status === OK) {
					initialDescription = newDesc;
					initialUnits = newUnits;
					return;
				}

			}

			request.open("POST", '/modifyCourse', true);
			var params = "id=" + id;

			if (newDesc !== null) {
				params = params + "&description=" + newDesc;
			}
			if (newUnits !== null) {
				params = params + "&units=" + newUnits;
			}

			request.setRequestHeader("Content-type",
					"application/x-www-form-urlencoded");
			request.setRequestHeader("Content-length", params.length);
			request.send(params);

		});
// add functionality to addPrereq button. enables form for adding new courses
// and enables removing of existing prerequites. if prerequisite modification is
// already enabled, does nothing.

$('input[name=addPrereq]').click(function() {

	if (prereqFormEnabled)
		return;
	inputs.off('submit');
	inputs.on('submit', onAddClick);
	enablePrereqForm();
	
	
	$('input[name = cancelPrereq]').click(disablePrereqForm);

});

// send request to add prerequisite to current course.
var onAddClick = function(e) {
	
	e.preventDefault();
	$('.error').remove();
	var department = $('input[name=departmentID]').val();
	var courseNumber = $('input[name=courseNumber]').val();

	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {

		if (request.readyState === 4) {
			
			if (request.status === OK) {
				
				var newPrereq = $('<li> </li>');
				newPrereq.append(department.toUpperCase() + courseNumber);
				
				
				
				var removeButton = $('<input type="button" name="removePrereq"  value = "remove" class="hidden" id='
						+ request.responseText + '>');
				removeButton.click(function() {
				
					removePrereq(newPrereq);
				});
				
				
				if (editEnabled) {
					removeButton.removeClass("hidden");
				}

				newPrereq.append(removeButton);

				prereqList.append(newPrereq);
				disablePrereqForm();
				return;
			}

			if (request.status === NOT_FOUND) {
				inputs.append($('<div class = "error">' + request.responseText
						+ '</div>'));

				return;
			}
			if (request.status === UNAUTHORIZED) {
				inputs.append($('<div class = "error">' + "You are not authorized to assign prerequisites to this class. " +
						"Please log in as an administrator."
						+ '</div>'));

				return;
			}
			if (request.status === INTERNAL_ERROR) {

				inputs.append($('<div class = "error">' + request.responseText
						+ '</div>'));

				return;
			}
			disablePrereqForm();
			return;
		}

	}

	request.open("POST", '/addPrereq', true);
	var params = 'prereqDept=' + department;
	params = params + "&prereqNumber=" + courseNumber;
	params = params + "&id=" + id;
	request.setRequestHeader("Content-type",
			"application/x-www-form-urlencoded");
	request.setRequestHeader("Content-length", params.length);
	request.send(params);
}
// sends request to the server to remove prerequisite from this course. removes
// list item from view if reqeust is succesfull.
// takes as
// argument Jquery object for prerequisite list item.
var removePrereq = function(prereqItem) {
	
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (request.readyState === 4) {
			if (request.status === INTERNAL_ERROR) {
				
				return;
			}

			if (request.status === OK) {
				
				prereqItem.remove();
				return;
			}
		}
	}

	request.open("POST", "/removePrereq", true)
	var params = "id=" + id;
	params = params + "&prereqID=" + prereqItem.find('input').attr("id");
	request.setRequestHeader("Content-type",
			"application/x-www-form-urlencoded");
	request.setRequestHeader("Content-length", params.length);
	request.send(params);

}
var enablePrereqForm = function() {
	prereqFormEnabled = true;
	inputs.removeClass("hidden");
}

var disablePrereqForm = function() {

	inputs[0].reset();
	prereqFormEnabled = false;
	inputs.addClass("hidden");
	$('.error').remove();
}

var enableEdit = function() {

	editEnabled = true;
	$('textarea[name = description]').removeAttr("readonly");
	$('input[name = units]').removeAttr("readonly");
	$('input[name = doneEdit]').removeClass("hidden");
	var removeButtons = $('input[name=removePrereq]');
	for (var i = 0; i < removeButtons.length; i++) {
		removeButtons.eq(i).removeClass("hidden");
	}
}

var disableEdit = function() {
	editEnabled = false;
	$('textarea[name = description]').attr("readonly", true);
	$('input[name = units]').attr("readonly", true);
	$('input[name = doneEdit]').addClass("hidden");
	var removeButtons = $('input[name=removePrereq]');
	for (var i = 0; i < removeButtons.length; i++) {
		removeButtons.eq(i).addClass("hidden");
	}
}