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

/**
 * Refresh the course grade summary chart for the site. 
 * If the schema is provided it is sent and the course grades will be recalculated based on transient data.
 * Otherwise it will use the persistent data.
 * @param siteId
 * @param schemaJson the JSON of the grading schema
 * @returns
 */
function renderChart(siteId, schemaJson) {
	
	var url = "/direct/gbng/course-grades.json?siteId="+siteId;
	if(schemaJson) {
		url += "&schema="+schemaJson;
	}
	
	$.ajax({
		url : url,
      	dataType : "json",
       	async : true,
		cache: false,
	   	success : function(data) {
	   		_renderChart(data.dataset);
		}
	});
}

/**
 * Refresh the course grade summary chart for the site based on the provided schema.
 * This delegates to the REST service which calculates the course grades based on transient data.
 * @param siteId
 * @param schemaJson the JSON of the grading schema
 * @returns
 */
function refreshChart(siteId, schemaJson) {
	
	$.ajax({
		url : "/direct/gbng/course-grades.json?siteId="+siteId+"&schema="+schemaJson,
      	dataType : "json",
       	async : true,
		cache: false,
	   	success : function(data) {
	   		_renderChart(data.dataset);
		}
	});
}

/**
 * Render the course grade summary chart from the given dataset.
 * @param dataset
 * @returns
 */
function _renderChart(dataset) {

	var data = $.map(dataset, function(value, index) {
		return value;
	});
	var labels = $.map(dataset, function(value, index) {
		return index;
	});
	
	//clear data. If ChartJS implements a better way, change this.
	$("#gradingSchemaChart").replaceWith('<canvas id="gradingSchemaChart"></canvas>');

	var ctx = $("#gradingSchemaChart");
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


/**************************************************************************************
 * Initialise
 */
$(function() {
  sakai.gradebookng = {
    settings: new GradebookSettings($("#gradebookSettings"))
  };
      
});