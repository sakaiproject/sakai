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
				backgroundColor: '#15597e',
				borderWidth: 0
			}]
		},
		options: {
			title: {
				display: true,
				text: 'Course Grade Distribution',
				fontSize: 18,
				fontStyle: 'bold'
			},
			legend: {
				display: false
			},
			scales: {
				xAxes: [{
					ticks: {
						beginAtZero:true,
						stepSize: 1,
						fontStyle: 'bold'
					},
					scaleLabel: {
						display: true,
						labelString: 'Number of Students',
						fontSize: 14,
						fontStyle: 'bold'
					}
				}],
				yAxes: [{
					ticks: {
						fontStyle: 'bold'
					},
					scaleLabel: {
						display: true,
						labelString: 'Course Grade',
						fontSize: 14,
						fontStyle: 'bold'
					}
				}]
			},
			tooltips: {
				displayColors: false,
				callbacks: {
			        title: function(tooltipItem, data) {
			          return tooltipItem[0].yLabel + ': ' + tooltipItem[0].xLabel;
			        },
			        label: function() {}
				}
			}
		}
	});
}