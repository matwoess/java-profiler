var colorCovered = 'rgba(144, 238, 144, 0.3)';
var colorNotCovered = 'rgba(255, 182, 193, 0.3)';
var colorHoverHighlight = 'rgba(245, 222, 179, 1)';

var setCoverageBackground = function (span){
	var backgroundColor = '';
	if (span.hasClass('c')) {
		backgroundColor = colorCovered;
	} else if (span.hasClass('nc')) {
		backgroundColor = colorNotCovered;
	}
	span.css({'background-color': backgroundColor})
}

$('span.c,span.nc').each(function () {setCoverageBackground($(this))});

$("span").mouseenter(function (){
		var classes = $(this).attr('class');
		if (classes == null) return;
		var classList = classes.split(/\s+/);
		var lastClass = classList.slice(-1);
		if (lastClass != null) {
			$('span.'+lastClass).css({ 'background-color': colorHoverHighlight });
		}
});

$("span").mouseleave(function (){
		$('span.c,span.nc').each(function () {setCoverageBackground($(this))});
});