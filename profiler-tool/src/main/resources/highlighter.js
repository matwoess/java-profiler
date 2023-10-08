const colorCovered = `rgba(144, 238, 144, 0.3)`;
const colorNotCovered = `rgba(255, 182, 193, 0.3)`;
const colorHoverHighlight = `rgba(245, 222, 179, 0.8)`;
const colorHoverRegionHighlight = `rgba(245, 222, 130, 1)`;
const isCovered = 'c';
const isNotCovered = 'nc';

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
  if (preLastClass != null && preLastClass !== isCovered && preLastClass !== isNotCovered) {
    setColor($('span.' + preLastClass), colorHoverHighlight);
  }
  if (lastClass != null) {
    if (lastClass.startsWith('r')) {
      setColor($('span.' + lastClass), colorHoverRegionHighlight);
    } else {
      setColor($('span.' + lastClass), colorHoverHighlight);
    }
  }
}

resetColors();
$('span')
  .mouseenter(highlightSelection)
  .mouseleave(resetColors);
