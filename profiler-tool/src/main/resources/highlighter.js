const colorCovered = 'rgba(144, 238, 144, 0.3)';
const colorNotCovered = 'rgba(255, 182, 193, 0.3)';
const colorHoverHighlight = 'rgba(245, 222, 179, 1)';

function setCoverageBackground(span) {
  let backgroundColor = '';
  if (span.hasClass('c')) {
    backgroundColor = colorCovered;
  } else if (span.hasClass('nc')) {
    backgroundColor = colorNotCovered;
  }
  span.css({'background-color': backgroundColor})
}

$('span.c,span.nc').each(function () {
  setCoverageBackground($(this))
});

$("span").mouseenter(function () {
  const classes = $(this).attr('class');
  if (classes == null) return;
  const classList = classes.split(/\s+/);
  const lastClass = classList.slice(-1);
  if (lastClass != null) {
    $('span.' + lastClass).css({'background-color': colorHoverHighlight});
  }
});

$("span").mouseleave(function () {
  $('span.c,span.nc').each(function () {
    setCoverageBackground($(this))
  });
});