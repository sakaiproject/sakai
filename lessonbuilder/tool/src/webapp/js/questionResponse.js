/**
 * Answers an inline Lessons "question" item over AJAX so the whole page does not
 * reload. Mirrors the checklist (checklistDisplay.js) and grading (gradingAjax.js)
 * flows: the answer form posts to the RSF UVB endpoint, the server records and
 * grades the response in QuestionResponseBean.getResults(), and the JSON payload it
 * returns is rendered here into the status icon, feedback text and (for polls) the
 * results graph. Participation is still recorded server-side exactly as before.
 */
(function (questionResponse, $, undefined) {

	/**
	 * Wires a question answer form for AJAX submission.
	 * Called once per rendered question via a UIInitBlock in ShowPageProducer.
	 *
	 * @param submitButtonId  id of the "Answer!" submit button
	 * @param questionIdFieldId  id of the hidden question-item-id input
	 * @param answerFieldId  id of the hidden input that carries the answer to the server
	 * @param csrfFieldId  id of the hidden CSRF token input
	 * @param resultBinding  the EL binding read back from the UVB response (questionResponseBean.results)
	 */
	questionResponse.initAnswerForm = function (submitButtonId, questionIdFieldId, answerFieldId, csrfFieldId, resultBinding) {

		const submitButton = document.getElementById(submitButtonId);
		const questionIdField = document.getElementById(questionIdFieldId);
		const answerField = document.getElementById(answerFieldId);
		const csrfField = document.getElementById(csrfFieldId);

		if (!submitButton || !questionIdField || !answerField || !csrfField) {
			console.warn("questionResponse.initAnswerForm not called correctly");
			return;
		}

		const form = questionIdField.form;
		const ajaxUrl = form.action;

		const callback = function (results) {
			let data;
			try {
				data = JSON.parse(results.EL[resultBinding][0]);
			} catch (e) {
				console.error("Could not parse question response", e);
				return;
			}
			renderResult($(submitButton).closest(".questionDiv"), data);
		};

		// setup the function which initiates the AJAX request
		const updater = RSF.getAJAXUpdater([questionIdField, answerField, csrfField], ajaxUrl, [resultBinding], callback);

		submitButton.addEventListener("click", function (e) {
			// don't let the form do a normal (page-reloading) submit
			e.preventDefault();

			const answer = currentAnswer(form);
			if (answer === null || answer === "") {
				return; // nothing selected / entered yet
			}
			answerField.value = answer;
			updater();
		});
	};

	// Reads the student's answer out of the form: the selected multiple-choice
	// option id, or the typed short-answer text.
	function currentAnswer(form) {
		const radio = form.querySelector("input[type=radio]:checked");
		if (radio) {
			return radio.getAttribute("data-answer-id");
		}
		const textarea = form.querySelector("textarea");
		if (textarea) {
			return textarea.value;
		}
		return null;
	}

	// Same status -> icon mapping the server uses in ShowPageProducer.addStatusIcon.
	function iconClassForStatus(status) {
		switch (status) {
			case "COMPLETED": return "fa fa-check";
			case "FAILED": return "fa fa-times";
			case "NEEDSGRADING": return "fa fa-question";
			default: return "";
		}
	}

	function renderResult(qDiv, data) {
		if (data.result !== "success") {
			showError(qDiv, data.message);
			return;
		}

		// status icon
		const statusIcon = qDiv.find(".statusCol span").first();
		if (statusIcon.length) {
			statusIcon.attr("class", iconClassForStatus(data.status));
			statusIcon.attr("title", data.statusNote || "");
		}

		// offscreen (screen reader) status note
		if (data.statusNote) {
			let note = qDiv.find(".questionText").prevAll(".lb-offscreen").first();
			if (!note.length) {
				note = qDiv.find(".lb-offscreen").first();
			}
			if (note.length) {
				note.text(data.statusNote);
			}
		}

		// feedback text (correct / incorrect message); may not exist yet for students
		if (data.feedbackText) {
			feedbackDiv(qDiv).removeClass("text-danger").html(data.feedbackText);
		}

		// stop the student answering again, matching the server-rendered disabled state
		disableForm(qDiv);

		// poll results graph
		if (data.showPoll && Array.isArray(data.poll)) {
			renderPoll(qDiv, data.poll);
		}
	}

	function feedbackDiv(qDiv) {
		let feedback = qDiv.find(".questionStatusText").first();
		if (!feedback.length) {
			feedback = $('<div class="questionStatusText"></div>');
			qDiv.find(".contentCol").first().append(feedback);
		}
		return feedback;
	}

	function showError(qDiv, message) {
		feedbackDiv(qDiv).addClass("text-danger").text(message || "");
	}

	function disableForm(qDiv) {
		qDiv.find(".multipleChoiceForm, .shortanswerForm")
			.find("input, textarea, select")
			.prop("disabled", true);
		// re-enable the "show poll" toggle, which is also a .question-submit
		qDiv.find(".showPollGraph").prop("disabled", false);
	}

	// Draws the poll bar graph inline using the same jqBarGraph plugin and data
	// shape the existing show/hide-poll toggle uses in show-page.js.
	function renderPoll(qDiv, poll) {
		let graph = qDiv.find(".questionPollGraph").first();
		if (!graph.length) {
			graph = $('<div class="questionPollGraph"></div>');
			qDiv.find(".contentCol").first().append(graph);
		}
		const pollData = poll.map(function (entry) {
			return [parseInt(entry.count, 10), entry.letter, "#000000", entry.legend];
		});
		graph.empty().show().jqBarGraph({ data: pollData, height: 100, speed: 1 });
	}

}(window.questionResponse = window.questionResponse || {}, jQuery));
