
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
	
	var data = $.map(dataset, function(value, index) {
		return value;
	});
	var labels = $.map(dataset, function(value, index) {
		return index;
	});
	
	//clear data. If ChartJS implements a better way, change this.
	$('#'+chartId).replaceWith('<canvas id="'+chartId+'"></canvas>');

	var ctx = $('#'+chartId);
	var myChart = new Chart(ctx, {
		type: chartType,
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
			          return tooltipItem[0].yLabel + ': ' + tooltipItem[0].xLabel;
			        },
			        label: function() {}
				}
			}
		}
	});
}
