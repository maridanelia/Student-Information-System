/**
 * 
 */
var OK = 200;
var NOT_FOUND = 404;
var INTERNAL_ERROR = 500;
var BAD_REQUEST = 400;
var UNAUTHORIZED = 401;

var editEnabled = false;
var div = $('div[name=schedule]');
var addItem = div.find('form[name=addItem]');
var modifyButton = div.find('input[name=modify]');
var doneButton = div.find('input[name=done]');
var removeButtons = div.find('ul').find('input[type=button]');
var id = $('input[name=classID]').val();

var modDetails = $('input[name = modDetails]');

var modDetailsDone = $('input[name = detailsDone]');

modDetails.click(function (e){
			
		modDetailsDone.removeClass("hidden");
		$('form[name=teacherForm]').removeClass("hidden");
		$('form[name=locationForm]').removeClass("hidden");
		$('form[name=sizeForm]').removeClass("hidden");
	
});
modDetailsDone.click(function(e){
	modDetailsDone.addClass("hidden");
	$('form[name=teacherForm]').addClass("hidden");
	$('form[name=locationForm]').addClass("hidden");
	$('form[name=sizeForm]').addClass("hidden");
});

var enableEdit = function(){
	editEnabled = true;
	addItem.removeClass('hidden');
	doneButton.removeClass('hidden');	
	modifyButton.attr("disabled",true);
	for (var i = 0; i < removeButtons.length; i++) {
		removeButtons.eq(i).removeClass("hidden");
	}
};

var disableEdit = function(){
	editEnabled = false;
	addItem.addClass('hidden');
	doneButton.addClass('hidden');
	modifyButton.attr("disabled",false);
	for (var i = 0; i < removeButtons.length; i++) {
		removeButtons.eq(i).addClass("hidden");
	}
};

var errorMessage = function(errortext){
	var result = $('<div name="error" class ="error"></div>');
	result.append(errortext);
	return result;
};

var clearAddDiv = function(){
	addItem[0].reset();
	div.find('div[name=error]').remove();
};


(function(){
	modifyButton.click(function(){
		enableEdit();
	});
	
	doneButton.click(function(){
		disableEdit();
		clearAddDiv();
	});
	
	div.find('input[name=add]').click(function(e){
		addItemAction(e);
	});
	var items = div.find('li');
	for (var i = 0; i < items.length; i++) {
		var listItem = items.eq(i);
		var button = listItem.find('input');
		button.click(function(){			
			removeItem($(this).parent());
		});
	}
}());

var addItemAction = function(e){
	e.preventDefault();
	
	console.log("doing shmt");
	var day = addItem.find('select').val();
	var start = addItem.find('input[name=startTime]').val();
	var end = addItem.find('input[name=endTime]').val();
	
	if(!start){ 
		console.log("!start");
		addItem.append(errorMessage("Please enter start time for the class"));
		return;
	}
	if(!end){
		console.log("!end");
		addItem.append(errorMessage("Please enter end time for the class"));
		return;
	}
	div.find('div[name=error]').remove();
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {

		if (request.readyState === 4) {
			console.log(request.status );
			if(request.status == OK){
				
				var newItem =$('<li></li>');
				
				newItem.append(day+" "+start+"-"+end);
				
				var itemButton = $('<input type="button" value="remove">');
				
				itemButton.attr('id',request.responseText);
				
				itemButton.click(function(){
					removeItem($(this).parent());
				});
				newItem.append(itemButton);
				div.find('ul').append(newItem);
				
				removeButtons = div.find('ul').find('input[type=button]');
				clearAddDiv();
				return;
			}
			if(request.status === BAD_REQUEST||request.status === UNAUTHORIZED){
				console.log(request.responseText);
				addItem.append(errorMessage(request.responseText));
			}
			if(request.status === INTERNAL_ERROR){
				addItem.append(errorMessage("internal server error"));
				return;
			}
		}

	}
	request.open("POST", '/addScheduleItem', true);
	var params = "id=" + id;
	params = params+"&weekday="+day;
	params = params+"&start="+start;
	params = params+"&end="+end;
	
	request.setRequestHeader("Content-type",
	"application/x-www-form-urlencoded");
	request.setRequestHeader("Content-length", params.length);
	request.send(params);
};
var removeItem = function(item){
	
	var itemID = item.find('input[type=button]').attr('id');

	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {

		if (request.readyState === 4) {
			
			if(request.status == OK){
				
				
				item.remove();
				return;
			}
			if(request.status === BAD_REQUEST){
				addItem.append(errorMessage(request.responseText));
			}
			if(request.status === INTERNAL_ERROR){
				addItem.append(errorMessage("internal server error"));
				return;
			}
			
		}

	}
	request.open("POST", '/removeScheduleItem', true);
	var params = "id=" + itemID;
	request.setRequestHeader("Content-type",
	"application/x-www-form-urlencoded");
	request.setRequestHeader("Content-length", params.length);
	request.send(params);
};





