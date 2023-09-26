const colorCovered = [144, 238, 144, 0.3];
const colorNotCovered = [255, 182, 193, 0.3];
const colorHoverHighlight = [245, 222, 179, 1];

function setCoverageBackground(span) {
  let backgroundColor = null;
  if (span.hasClass('c')) {
    backgroundColor = colorCovered;
  } else if (span.hasClass('nc')) {
    backgroundColor = colorNotCovered;
  }
  setColor(span, backgroundColor)
}

function setColor(elems, color) {
  if (color == null) {
    elems.css({'background-color': ''});
  } else {
    let [r, g, b, a] = color
    elems.css({'background-color': `rgba(${r},${g},${b},${a})`});
  }
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
    setColor($('span.' + lastClass), colorHoverHighlight);
  }
});

$("span").mouseleave(function () {
  $('span.c,span.nc').each(function () {
    setCoverageBackground($(this))
  });
});