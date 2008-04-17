The functionality of adding prompt for questions during the site creation process is added according to SAK-12912.

/****************** Steps for uploading the question definitions **************************/

1. Admin user needs to use the Admin Sites tool to create a project site named "setupQuestionsAdmin" with Resources tool enabled.

2. In the Resources tool, create a folder named "config";

3. Upload the question definition xml file with name "questions.xml" into the config folder.

The first time Sakai invokes the Worsite Setup tool after the file is in place, the question definitions are loaded and are ready for use for Site Creation.

/******************** template for questions.xml ***************************/

The template the questions.xml is as follows:

<?xml version="1.0" encoding="UTF-8" ?> 
	 	<SiteSetupQuestions>
			<site type="project">
				<header>Please answer the following to help us understand how CTools is being used for this project site.</header> 
				<url><a href="http://www.google.com" target="_blank">More info</a></url>
				<question required="true" multiple_answers="false">
	  				<q>In what capacity are you creating this site?</q> 
	 				<answer>Student</answer> 
	  				<answer>Faculty</answer> 
	  				<answer>Staff</answer> 
	  			</question>
				<question required="false" multiple_answers="false">
	  				<q>The primary use for this project site will be:</q> 
	  				<answer>Learning"</answer> 
	 				<answer>Research"</answer> 
	  				<answer>Administrative</answer> 
	  				<answer>Personal</answer> 
	  				<answer>Student group/organization</answer> 
	  				<answer fillin_blank="true">Other</answer> 
	  			</question>
  			</site>
			<site type="course">
				<header>Please answer the following to help us understand how CTools is being used for this course site.</header> 
				<question required="true" multiple_answers="false">
	  				<q>The primary use for this project site will be:</q> 
	  				<answer>Student</answer> 
	  				<answer>Faculty</answer> 
	  				<answer>Staff</answer> 
	  			</question>
	  		</site>
	  	</SiteSetupQuestions>