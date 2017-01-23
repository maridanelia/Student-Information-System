/**
 * 
 */
var OK = 200;
var NOT_FOUND = 400;
var INTERNAL_ERROR = 500;
var form = $('form[name = gradCheckForm]');
var formMessage = form.find('div[name=info]');

form.on('submit', function(e){
	e.preventDefault();
	var request = new XMLHttpRequest();
	request.onreadystatechange = function(){
		if(request.readyState === 4){
			if(request.status === OK){
				response = JSON.parse(request.responseText);
				console.log(response);
				formMessage.empty();
				if(response.result === true){
					formMessage.append(response.message);
					formMessage.addClass("message");
				}	else {
					formMessage.append(response.message);
					formMessage.addClass("error");
				}
				
				return;
			}
			if(request.status === INTERNAL_ERROR){
				formMessage.append("Internal Server Error");
				formMessage.addClass("error");
			}
		}
	}
	var params = "email="+form.attr("email");
	request.open("POST","/gradCheck");
	request.setRequestHeader("Content-type",
	"application/x-www-form-urlencoded");
	request.setRequestHeader("Content-length", params.length);
	request.send(params);
});