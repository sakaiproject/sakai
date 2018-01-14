/**************************************************************************************
 *                    Gradebook Settings Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookSettings to encapsulate all the settings page features 
 */
function GradebookSettings($container) {
  this.$container = $container;

  this.categories = new GradebookCategorySettings($container.find("#settingsCategories"));
};


/**************************************************************************************
 * A GradebookCategorySettings to encapsulate all the category settings features 
 */
function GradebookCategorySettings($container) {
  this.$container = $container;
  this.$table = this.$container.find("table");

  // only if categories are enabled
  if (this.$table.length > 0) {
    this.setupSortableCategories();
    this.setupKeyboardSupport();
  }
}


GradebookCategorySettings.prototype.setupSortableCategories = function() {
  var self = this;

  // setup jQuery sortable on rows
  self.$table.find("tbody").sortable({
      handle: ".gb-category-sort-handle",
      helper: function(e, ui) {
                ui.children().each(function() {
                  $(this).width($(this).width());
                });
                return ui;
              },
      placeholder: "gb-category-sort-placeholder",
      update: $.proxy(self.updateCategoryOrders, self)
    });
};


GradebookCategorySettings.prototype.setupKeyboardSupport = function() {
  var self = this;

  self.$table.on("keydown", ":text", function(event) {
    // add new row upon return
    if (event.keyCode == 13) {
      event.preventDefault();
      event.stopPropagation();

      self.$container.find(".btn-add-category").trigger("click");
    }
  });
};


GradebookCategorySettings.prototype.focusLastRow = function() {
  // get the first input#text in the last row of the table
  var $input = this.$table.find(".gb-category-row:last :text:first");
  // attempt to set focus
  $input.focus();
  // Wicket may try to set focus on the input last focused before form submission
  // so set this manually to our desired input
  Wicket.Focus.setFocusOnId($input.attr("id"));
}


GradebookCategorySettings.prototype.updateCategoryOrders = function() {
  this.$table.find("tbody tr.gb-category-row").each(function(i, el) {
    $(el).find(".gb-category-order-field").val(i).trigger("orderupdate.sakai");
  });
};

function renderGraph() {
	$.get("/direct/gbng/course-grades.json?siteId=a470fed6-2a9d-48d3-8cb9-dde5fb900603", function(data, status){
		renderChart(data.dataset);
	});

	function renderChart(dataset) {

		var data = $.map(dataset, function(value, index) {
			return value;
		});
		var labels = $.map(dataset, function(value, index) {
			return index;
		});

		var ctx = document.getElementById("gradingSchemaChart");
		var myChart = new Chart(ctx, {
			type: 'horizontalBar',
			data: {
				labels: labels,
				datasets: [{
					data: data,
					backgroundColor: 'rgb(64, 120, 209)',
					borderWidth: 0,
				}]
			},
			options: {
				title: {
					display: true,
					text: 'Course Grade Distribution'
				},
				legend: {
					display: false
				},
				scales: {
					xAxes: [{
						ticks: {
							beginAtZero:true,
							stepSize: 1
						},
						scaleLabel: {
							display: true,
							labelString: 'Number of Students'
						}
					}],
					yAxes: [{
						scaleLabel: {
							display: true,
							labelString: 'Course Grade'
						}
					}]
				},
			}
		});

	}
}


/**************************************************************************************
 * Let's initialize our GradebookSettings 
 */
$(function() {
  sakai.gradebookng = {
    settings: new GradebookSettings($("#gradebookSettings"))
  };
  
  renderGraph();
  
});