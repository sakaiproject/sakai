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