
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

var GbChart = {};

$(document).ready(function() {
	// need TrimPath to load before parsing templates
	GbChart.templates = {
		chartStudentsGradeMessage: TrimPath.parseTemplate(
			$("#chartStudentsGradeMessage").html().trim().toString()),
		chartYourGradeMessage: TrimPath.parseTemplate(
			$("#chartYourGradeMessage").html().trim().toString()),
	};
});

var myChart;

function renderChart(gbChartData) {
	var chartData = JSON.parse(gbChartData);

	var chartId = chartData.chartId;
	var chartType = chartData.chartType;
	var chartTitle = chartData.chartTitle;
	var xAxisLabel = chartData.xAxisLabel;
	var yAxisLabel = chartData.yAxisLabel;

	var ctx = $('#'+chartId);
	myChart = new Chart(ctx, {
		type: chartType,

		options: {

			title: {
				display: true,
				text: chartTitle,
				fontSize: 18,
				fontStyle: 'bold',
				fontColor: getComputedStyle(document.documentElement).getPropertyValue('--sakai-text-color-1'),
			},
			legend: {
				display: false
			},
			scales: {
				xAxes: [{
					ticks: {
						beginAtZero:true,
						fontStyle: 'bold',
						autoSkip: true,
						maxRotation: 0,
						fontColor: getComputedStyle(document.documentElement).getPropertyValue('--sakai-text-color-1'),
						callback: function(value, index, values) {
						    if (isNaN(value)) {
							return value;
						    }
						    // Display student values only as integers
						    else if (Math.floor(value) === value) {
							return value;
						    }
						}
					},
					scaleLabel: {
						display: true,
						labelString: xAxisLabel,
						fontSize: 14,
						fontFamily: 'Monospace',
						fontStyle: 'bold',
						fontColor: getComputedStyle(document.documentElement).getPropertyValue('--sakai-text-color-1'),
			}
				}],
				yAxes: [{
					ticks: {
						beginAtZero:true,
						fontStyle: 'bold',
						fontFamily: 'Monospace',
						fontColor: getComputedStyle(document.documentElement).getPropertyValue('--sakai-text-color-1'),
						autoskip: true,
						maxRotation: 0,
						callback: function(value, index, values) {
						    if (isNaN(value)) {
							// Include a space to even out the plusses and minuses
							return value + (value.length < 2 ? ' ' : '');
						    }
						    // Display student values only as integers
						    else if (Math.floor(value) === value) {
							return value;
						    }
						}
					},
					scaleLabel: {
						display: true,
						labelString: yAxisLabel,
						fontSize: 14,
						fontStyle: 'bold',
						fontColor: getComputedStyle(document.documentElement).getPropertyValue('--sakai-text-color-1'),
					}
				}]
			},
			tooltips: {
				displayColors: false,
				callbacks: {
					title: function(tooltipItem, data) {
						var chartMessage = GbChart.templates['chartStudentsGradeMessage'].process();
						switch(chartType) {
						case 'bar':
							return chartMessage.replace('{0}', tooltipItem[0].yLabel).replace('{1}', tooltipItem[0].xLabel);
							break;
						case 'horizontalBar':
							return chartMessage.replace('{0}', tooltipItem[0].xLabel).replace('{1}', tooltipItem[0].yLabel);
						}

					},
					label: function() {},
					afterTitle: function(tooltipItem) {
						if (typeof(window.studentGradeRange) !== "undefined") {
							switch(chartType) {
							case 'bar':
								if (window.studentGradeRange != null && window.studentGradeRange == tooltipItem[0].xLabel) {
									return GbChart.templates['chartYourGradeMessage'].process();
								}
								break;
							case 'horizontalBar':
								if (window.studentGradeRange != null && window.studentGradeRange == tooltipItem[0].yLabel) {
									return GbChart.templates['chartYourGradeMessage'].process();
								}
							}
						}
					}
				}
			}
		}
	});
	renderChartData(gbChartData);
}

function renderChartData(gbChartData) {
	var chartData = JSON.parse(gbChartData);

	var dataset = chartData.dataset;
	var chartId = chartData.chartId;
	var studentGradeRange = chartData.studentGradeRange;
	if ( typeof(studentGradeRange) !== undefined ) {
		window.studentGradeRange = studentGradeRange;
	}
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

	myChart.data = {
		labels: labels,
		datasets: [{
			data: data,
			backgroundColor: backgroundColour,
			borderWidth: 0
		}]
	};
	myChart.update();
}
