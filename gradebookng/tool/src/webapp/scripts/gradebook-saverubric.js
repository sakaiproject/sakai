document.getElementById("saverubric").addEventListener("click", e => {

  const rubricGrading = document.getElementsByTagName("sakai-rubric-grading").item(0);
  rubricGrading && rubricGrading.save();
});
