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
 * Custom chart drawing, to enable us to have rounded corners
 * 
 * Based on code by Jed Trow
 * https://github.com/jedtrow/Chart.js-Rounded-Bar-Charts/blob/master/Chart.roundedBarCharts.js
 * 
 * Updated to remove unnecessary code and only round the top corners
 * 
 * NOTE: This code should be removed as soon as rounded corners are added as a feature to Chartjs. 
 * It is only a stopgap solution.
 */

Chart.elements.Rectangle.prototype.draw = function() {
	
    var ctx = this._chart.ctx;
    var vm = this._view;
    var left, right, top, bottom, signX, signY, borderSkipped, cornerRadius;
    var borderWidth = vm.borderWidth;

    if (!vm.horizontal) {
        // bar
        left = vm.x - vm.width / 2;
        right = vm.x + vm.width / 2;
        top = vm.y;
        bottom = vm.base;
        signX = 1;
        signY = bottom > top? 1: -1;
        borderSkipped = vm.borderSkipped || 'bottom';
    } else {
        // horizontal bar
        left = vm.base;
        right = vm.x;
        top = vm.y - vm.height / 2;
        bottom = vm.y + vm.height / 2;
        signX = right > left? 1: -1;
        signY = 1;
        borderSkipped = vm.borderSkipped || 'left';
    }

    // Canvas doesn't allow us to stroke inside the width so we can
    // adjust the sizes to fit if we're setting a stroke on the line
    if (borderWidth) {
        // borderWidth should be less than bar width and bar height.
        var barSize = Math.min(Math.abs(left - right), Math.abs(top - bottom));
        borderWidth = borderWidth > barSize? barSize: borderWidth;
        var halfStroke = borderWidth / 2;
        // Adjust borderWidth when bar top position is near vm.base (zero)
        var borderLeft = left + (borderSkipped !== 'left'? halfStroke * signX: 0);
        var borderRight = right + (borderSkipped !== 'right'? -halfStroke * signX: 0);
        var borderTop = top + (borderSkipped !== 'top'? halfStroke * signY: 0);
        var borderBottom = bottom + (borderSkipped !== 'bottom'? -halfStroke * signY: 0);
        // not become a vertical line?
        if (borderLeft !== borderRight) {
            top = borderTop;
            bottom = borderBottom;
        }
        // not become a horizontal line?
        if (borderTop !== borderBottom) {
            left = borderLeft;
            right = borderRight;
        }
    }

    width = right - left;
    height = bottom - top;
    
    // Set the corner radius to be half the width or height (whichever is smaller)
    if (this._chart.config.options.roundedCorners) {
    	cornerRadius = (Math.abs(height) < Math.abs(width)) ? Math.floor(Math.abs(height)/2) : Math.floor(Math.abs(width)/2);
    } else {
    	cornerRadius = 0;
    }

    // Draw the rectangle
    ctx.beginPath();
    ctx.fillStyle = vm.backgroundColor;
    ctx.strokeStyle = vm.borderColor;
    ctx.lineWidth = borderWidth;
    var x = left, y = top;
    if (!vm.horizontal) {
    	ctx.moveTo(x, y + height);
    	ctx.lineTo(x, y + cornerRadius);
        ctx.quadraticCurveTo(x, y, x + cornerRadius, y);
        ctx.lineTo(x + width - cornerRadius, y);
        ctx.quadraticCurveTo(x + width, y, x + width, y + cornerRadius);
        ctx.lineTo(x + width, y + height);
        ctx.lineTo(x, y + height);
    } else {
    	ctx.moveTo(x, y);
    	ctx.lineTo(x + width - cornerRadius, y);
    	ctx.quadraticCurveTo(x + width, y, x + width, y + cornerRadius);
    	ctx.lineTo(x + width, y + height - cornerRadius);
    	ctx.quadraticCurveTo(x + width, y + height, x + width - cornerRadius, y + height);
    	ctx.lineTo(x, y + height);
    	ctx.lineTo(x, y);
    }
    ctx.fill();
    if (borderWidth) {
        ctx.stroke();
    }
};

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
			roundedCorners: true,
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