function selectionAuthor(className, anchor){
	var x1, x2, y1, y2;
	
	var anchorJObj = $('#'+anchor);
			
	var div = document.createElement('div');
	div.className = className.selectionClass;
	anchorJObj.append(div);
	
	var divJObj = $(div);
	
	var span = document.createElement('span');
	span.className = className.textClass;
	divJObj.append(span);
	
	var spanObj = $(span);

	
	this.setText = function(text) {
		spanObj.html(text);
	}	
	
	this.setCoords = function(coords){
		if(coords != null && coords.x1 != null && coords.y1 != null && coords.x2 != null && coords.y2 != null)
		{			
			x1 = coords.x1;
			x2 = coords.x2;
			y1 = coords.y1;
			y2 = coords.y2;
			
			move();
		}
	}
	
	function move(){
		// Calculate the div select rectancle for positive and negative values
		var TOP = parseInt(Math.max(0, (y1 < y2) ? y1 : y2));
		var LEFT = parseInt(Math.max(0, (x1 < x2) ? x1 : x2));
		var WIDTH = (x1 < x2) ? x2 - x1 : x1 - x2;
		var HEIGHT = (y1 < y2) ? y2 - y1 : y1 - y2;
			
		// Use CSS to place your select div
		divJObj.css({
			position: 'absolute',
			zIndex: 5000,
			left: LEFT,
			top: TOP,
			width: WIDTH,
			height: HEIGHT
		});
		divJObj.show();
	}
}
(function() {
// Hot Spot grading: lay out badges around their anchors without overlapping each other.
function placeImageMapBadges(containerId) {
	var container = document.getElementById(containerId);
	if (!container) {
		return;
	}
	var containerRect = container.getBoundingClientRect();
	var image = container.querySelector('img');
	var imageRect = image ? image.getBoundingClientRect() : containerRect;
	var bounds = {
		left: imageRect.left - containerRect.left,
		top: imageRect.top - containerRect.top,
		right: imageRect.left - containerRect.left + imageRect.width,
		bottom: imageRect.top - containerRect.top + imageRect.height
	};

	container.querySelectorAll('.imageMapBadgeLeader').forEach(function(line) {
		line.remove();
	});

	var labels = [];
	container.querySelectorAll('.selectiondiv').forEach(function(region) {
		var badge = region.querySelector('.textContainer');
		if (!badge || badge.offsetWidth === 0) {
			return;
		}
		var rect = region.getBoundingClientRect();
		var anchorX = rect.left - containerRect.left + rect.width / 2;
		var anchorY = rect.top - containerRect.top + rect.height / 2;
		labels.push({badge: badge, parent: region, kind: 'area', anchorX: anchorX, anchorY: anchorY,
			obstacle: boxAround(anchorX, anchorY, 3)});
	});
	container.querySelectorAll('.pointerClass, .pointerClass_selected').forEach(function(pointer) {
		var badge = pointer.querySelector('.selectionText');
		if (!badge || badge.offsetWidth === 0) {
			return;
		}
		var rect = pointer.getBoundingClientRect();
		var anchorX = rect.left - containerRect.left + rect.width / 2;
		var anchorY = rect.top - containerRect.top + rect.height / 2;
		labels.push({badge: badge, parent: pointer, kind: 'marker', anchorX: anchorX, anchorY: anchorY,
			obstacle: boxAround(anchorX, anchorY, 5)});
	});

	labels.forEach(function(label) {
		label.badge.style.transform = 'none';
		label.badge.style.right = 'auto';
		label.badge.style.left = '0px';
		label.badge.style.top = '0px';
		label.width = label.badge.offsetWidth;
		label.height = label.badge.offsetHeight;
	});

	var occupied = labels.map(function(label) { return label.obstacle; });
	labels.forEach(function(label) {
		var box = findImageMapBadgeBox(label, occupied, bounds);
		var parentRect = label.parent.getBoundingClientRect();
		label.badge.style.left = (box.left - (parentRect.left - containerRect.left)) + 'px';
		label.badge.style.top = (box.top - (parentRect.top - containerRect.top)) + 'px';
		occupied.push(box);
		addImageMapBadgeLeader(container, label, box);
	});
}

function boxAround(x, y, half) {
	return {left: x - half, top: y - half, right: x + half, bottom: y + half};
}

function findImageMapBadgeBox(label, occupied, bounds) {
	var gap = 2;
	var width = label.width;
	var height = label.height;
	var directions = label.kind === 'area'
		? [[0, -1], [1, 0], [0, 1], [-1, 0], [1, -1], [1, 1], [-1, 1], [-1, -1]]
		: [[1, 0], [0, -1], [-1, 0], [0, 1], [1, -1], [1, 1], [-1, 1], [-1, -1]];
	for (var ring = 0; ring < 8; ring++) {
		for (var d = 0; d < directions.length; d++) {
			var direction = directions[d];
			var left = label.anchorX - width / 2
				+ direction[0] * (width / 2 + gap + ring * (width * 0.75 + gap));
			var top = label.anchorY - height / 2
				+ direction[1] * (height / 2 + gap + ring * (height * 0.75 + gap));
			left = Math.max(bounds.left, Math.min(left, bounds.right - width));
			top = Math.max(bounds.top, Math.min(top, bounds.bottom - height));
			var box = {left: left, top: top, right: left + width, bottom: top + height};
			if (!occupied.some(function(other) { return boxesOverlap(box, other); })) {
				return box;
			}
		}
	}
	var left = Math.max(bounds.left, Math.min(label.anchorX - width / 2, bounds.right - width));
	var top = Math.max(bounds.top, Math.min(label.anchorY - height / 2, bounds.bottom - height));
	return {left: left, top: top, right: left + width, bottom: top + height};
}

function boxesOverlap(first, second) {
	return first.left < second.right && first.right > second.left
		&& first.top < second.bottom && first.bottom > second.top;
}

function addImageMapBadgeLeader(container, label, box) {
	var endX = Math.max(box.left, Math.min(label.anchorX, box.right));
	var endY = Math.max(box.top, Math.min(label.anchorY, box.bottom));
	var dx = endX - label.anchorX;
	var dy = endY - label.anchorY;
	var length = Math.sqrt(dx * dx + dy * dy);
	if (length < 8) {
		return;
	}
	var line = document.createElement('div');
	line.className = 'imageMapBadgeLeader';
	line.style.left = label.anchorX + 'px';
	line.style.top = label.anchorY + 'px';
	line.style.width = length + 'px';
	line.style.transform = 'rotate(' + Math.atan2(dy, dx) + 'rad)';
	container.appendChild(line);
}

window.placeImageMapBadges = placeImageMapBadges;
})();
