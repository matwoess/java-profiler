const colorCovered = `rgba(144, 238, 144, 0.3)`;
const colorNotCovered = `rgba(255, 182, 193, 0.3)`;
const colorHoverHighlight = `rgba(245, 222, 179, 0.8)`;
const colorHoverRegionHighlight = `rgba(245, 222, 130, 1)`;

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
    elems.css({'background-color': color});
  }
}

$('span.c,span.nc').each(function () {
  setCoverageBackground($(this))
});

$("span").mouseenter(function () {
  const classes = $(this).attr('class');
  if (classes == null) return;
  const classList = classes.split(/\s+/);
  const lastBlock = classList.slice(-2);
  const lastBlockRegion = classList.slice(-1);
  if (lastBlock != null) {
    setColor($('span.' + lastBlock), colorHoverHighlight);
  }
  if (lastBlockRegion != null) {
    setColor($('span.' + lastBlockRegion), colorHoverRegionHighlight);
  }
}).mouseleave(function () {
  $('span.c,span.nc').each(function () {
    setCoverageBackground($(this))
  });
});