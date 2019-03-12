
/**
 * GradebookNG Chart JS.
 * 
 * Renders charts using the supplied {@link org.sakaiproject.gradebookng.tool.model.GbChartData} object (serialised to JSON).
 * Draws charts using ChartJS.
 */

/**
 * Render the chart from the given dataset.
 * @param gbChartData the data for the chart. Must be a GbChartData object serialised to JSON.
 * @returns
 */
function renderChart(gbChartData) {
	
	var chartData = JSON.parse(gbChartData);
		
	var dataset = chartData.dataset;
	var chartTitle = chartData.chartTitle;
	var xAxisLabel = chartData.xAxisLabel;
	var yAxisLabel = chartData.yAxisLabel;
	var chartType = chartData.chartType;
	var chartId = chartData.chartId;
	var studentGradeRange = chartData.studentGradeRange;
	var backgroundColour = [];

	var data = $.map(dataset, function(value, index) {
		return value;
	});
	var labels = $.map(dataset, function(value, index) {
		return index;
	});
	
	//If this chart is being viewed by a student, display the bar that 
	//includes their mark in a different colour
	if (studentGradeRange != null) {
		var index = labels.indexOf(studentGradeRange);
		for (i = 0; i < labels.length; i++) {
			backgroundColour.push('#15597e');
		}
		backgroundColour[index] = '#5bc0de'; // this is the highlight colour for the student's grade
	} else {
		backgroundColour = '#15597e';
	}
	
	//clear data. If ChartJS implements a better way, change this.
	$('#'+chartId).replaceWith('<canvas id="'+chartId+'"></canvas>');

	var ctx = $('#'+chartId);
	var myChart = new Chart(ctx, {
		type: chartType,
		data: {
			labels: labels,
			datasets: [{
				data: data,
				backgroundColor: backgroundColour,
				borderWidth: 0
			}]
		},
		options: {
			title: {
				display: true,
				text: chartTitle,
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
						labelString: xAxisLabel,
						fontSize: 14,
						fontStyle: 'bold'
					}
				}],
				yAxes: [{
					ticks: {
						beginAtZero:true,
						stepSize: 1,
						fontStyle: 'bold'
					},
					scaleLabel: {
						display: true,
						labelString: yAxisLabel,
						fontSize: 14,
						fontStyle: 'bold'
					}
				}]
			},
			tooltips: {
				displayColors: false,
				callbacks: {
					title: function(tooltipItem, data) {
						switch(chartType) {
						case 'bar':
							return tooltipItem[0].yLabel + ' student(s): ' + tooltipItem[0].xLabel;
							break;
						case 'horizontalBar':
							return tooltipItem[0].xLabel + ' student(s): ' + tooltipItem[0].yLabel;
						}

					},
					label: function() {},
					afterTitle: function(tooltipItem) {
						switch(chartType) {
						case 'bar': 
							if (studentGradeRange != null && studentGradeRange == tooltipItem[0].xLabel) {
								return 'Your grade';
							}
							break;
						case 'horizontalBar': 
							if (studentGradeRange != null && studentGradeRange == tooltipItem[0].yLabel) {
								return 'Your grade';
							}
						}
					}
				}
			}
		}
	});
}