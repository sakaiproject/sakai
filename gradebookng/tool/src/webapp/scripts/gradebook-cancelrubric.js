document.getElementById("cancelrubric").addEventListener("click", e => {

  const rubricGrading = document.getElementsByTagName("sakai-rubric-grading").item(0);
  rubricGrading && rubricGrading.cancel();
});
