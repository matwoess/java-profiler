$("span").mouseenter(function (){
		var classList = $(this).attr('class').split(/\s+/);
		var lastClass = classList.slice(-1);
		if (lastClass != null) {
			$('span.'+lastClass).css({ 'background-color': 'yellow' });
		}
});
$("span").mouseleave(function (){
		var classList = $(this).attr('class').split(/\s+/);
		var lastClass = classList.slice(-1);
		if (lastClass != null) {
			$('span.'+lastClass).css({ 'background-color': '' });
		}
});