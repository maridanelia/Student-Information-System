(function() {
	var OK = 200;
	var NOT_FOUND = 404;
	var INTERNAL_ERROR = 500;
	var BAD_REQUEST = 400;
	var UNAUTHORIZED = 401;

	var editEnabledTX = false;
	var divTX = $('div[name=textbooks]');
	var addBookForm = divTX.find('form[name=add]');
	var modifyButtonTX = divTX.find('input[name=modify]');
	var doneButtonTX = divTX.find('input[name=done]');
	var removeButtonsTX = divTX.find('ul').find('input[type=button]');
	var id = $('input[name=classID]').val();

	var enableEdit = function() {
		editEnabledTX = true;
		addBookForm.removeClass('hidden');
		doneButtonTX.removeClass('hidden');
		modifyButtonTX.attr("disabled", true);
		for (var i = 0; i < removeButtonsTX.length; i++) {
			removeButtonsTX.eq(i).removeClass("hidden");
		}
	};

	var disableEdit = function() {
		editEnabledTX = false;
		addBookForm.addClass('hidden');
		doneButtonTX.addClass('hidden');
		modifyButtonTX.attr("disabled", false);
		for (var i = 0; i < removeButtonsTX.length; i++) {
			removeButtonsTX.eq(i).addClass("hidden");
		}
	};

	// add functionality to buttons on the page
	(function() {
		modifyButtonTX.click(function() {
			enableEdit();
		});

		doneButtonTX.click(function() {
			disableEdit();
			clearTextBookForm();
		});

		addBookForm.on('submit', function(e) {
			addTextBook(e);
		});

		var textbookListItems = divTX.find('li');
		for (var i = 0; i < textbookListItems.length; i++) {
			var listItem = textbookListItems.eq(i);
			var button = listItem.find('input');
			button.click(function() {
				removeTextBook($(this).parent());
			});
		}
	}());

	var addTextBook = function(e) {
		e.preventDefault();
		addBookForm.find($('div[name=error]')).remove();
		var textBookName = addBookForm.find('input[name=textbookName]').val();
		console.log(textBookName);
		if (textBookName === undefined || textBookName === null
				|| textBookName === '') {
			addBookForm
					.append(errorMessage("Please enter name of the textbook"));
			return;
		}
		var request = new XMLHttpRequest();
		request.onreadystatechange = function() {

			if (request.readyState === 4) {

				if (request.status == OK) {

					var newBook = $('<li></li>');

					newBook.append(textBookName);

					var textBookButton = $('<input type="button" value="remove">');

					textBookButton.attr('id', textBookName);

					textBookButton.click(function() {
						removeTextBook(textBookButton.parent());
					});
					newBook.append(textBookButton);
					divTX.find('ul').append(newBook);

					removeButtonsTX = divTX.find('ul').find('input[type=button]');
					clearTextBookForm();
					return;
				}
				if (request.status === BAD_REQUEST||request.status === UNAUTHORIZED) {
					console.log(request.responseText);
					
					addBookForm.append(errorMessage(request.responseText));
				}
				if (request.status === INTERNAL_ERROR) {
					addBookForm.append(errorMessage("internal server error"));
					return;
				}
			}

		}
		
		request.open("POST", '/addtextbook', true);
		var params = "id=" + id;
		var params = params + "&textbook=" + textBookName;
		request.setRequestHeader("Content-type",
				"application/x-www-form-urlencoded");
		request.setRequestHeader("Content-length", params.length);
		request.send(params);
	};

	var errorMessage = function(errortext) {
		var result = $('<div name="error" class ="error"></div>');
		result.append(errortext);
		return result;
	};

	var clearTextBookForm = function() {
		addBookForm[0].reset();
		divTX.find('div[name=error]').remove();
	};
	// text list item, jqeury object.
	var removeTextBook = function(textbook) {
		console.log("remove " + textbook);

		var textBookName = textbook.find('input[type=button]').attr('id');

		var request = new XMLHttpRequest();
		request.onreadystatechange = function() {

			if (request.readyState === 4) {

				if (request.status == OK) {
					console.log("removed" + textbook.html());

					textbook.remove();
					return;
				}
				if (request.status === BAD_REQUEST) {
					addBookForm.append(errorMessage(request.responseText));
				}
				if (request.status === INTERNAL_ERROR) {
					addBookForm.append(errorMessage("internal server error"));
					return;
				}
			}

		}
		request.open("POST", '/removetextbook', true);
		var params = "id=" + id;
		var params = params + "&textbook=" + textBookName;
		request.setRequestHeader("Content-type",
				"application/x-www-form-urlencoded");
		request.setRequestHeader("Content-length", params.length);
		request.send(params);
	};
})();
