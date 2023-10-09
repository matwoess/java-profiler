const colorCovered = `rgba(144, 238, 144, 0.3)`;
const colorNotCovered = `rgba(255, 182, 193, 0.3)`;
const colorHoverHighlight = `rgba(245, 222, 179, 0.8)`;
const colorHoverRegionHighlight = `rgba(245, 222, 130, 1)`;
const isCovered = 'c';
const isNotCovered = 'nc';
const blockPrefix = 'b';
const regionPrefix = 'r';

function setCoverageBackground(span) {
  let backgroundColor = null;
  if (span.hasClass(isCovered)) {
    backgroundColor = colorCovered;
  } else if (span.hasClass(isNotCovered)) {
    backgroundColor = colorNotCovered;
  }
  setColor(span, backgroundColor)
}

function setColor(elems, color) {
  if (color == null) {
    elems.css({'background-color': ''});
  } else {
    elems.css({'background-color': color});
  }
}

function resetColors() {
  $('span.c,span.nc').each(function () {
    setCoverageBackground($(this));
  });
}

function highlightSelection() {
  const classes = $(this).attr('class');
  if (classes == null) return;
  const classList = classes.split(/\s+/);
  const preLastClass = String(classList.slice(-2));
  const lastClass = String(classList.slice(-1));
  if (lastClass == null || preLastClass == null) {
    return;
  }
  if (lastClass.startsWith(regionPrefix)) {
    if (preLastClass.startsWith(blockPrefix)) {
      setColor($('span.' + preLastClass), colorHoverHighlight);
    }
    setColor($('span.' + lastClass), colorHoverRegionHighlight);
  }
  else if (lastClass.startsWith(blockPrefix)) {
    setColor($('span.' + lastClass), colorHoverHighlight);
  }
}

resetColors();
$('span')
  .mouseenter(highlightSelection)
  .mouseleave(resetColors);
