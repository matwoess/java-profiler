const coveredBlockColor = `rgba(144, 238, 144, 0.2)`;
const coveredRegionColor = `rgba(144, 255, 144, 0.4)`;
const notCoveredBlockColor = `rgba(255, 182, 193, 0.2)`;
const notCoveredRegionColor = `rgba(255, 162, 173, 0.4)`;
const hoverBlockColor = `rgba(245, 222, 109, 0.4)`;
const hoverRegionColor = `rgba(245, 222, 110, 1)`;
const minusBlockColor = `rgba(215, 192, 149, 0.5)`;
const isCovered = 'c';
const isNotCovered = 'nc';
const blockPrefix = 'b';
const regionPrefix = 'r';
const minusPrefix = 'm';

function setCoverageBackground(span) {
  let backgroundColor = null;
  if (span.hasClass(isCovered)) {
    backgroundColor = coveredBlockColor;
  } else if (span.hasClass(isNotCovered)) {
    backgroundColor = notCoveredBlockColor;
  }
  if (span.attr('class').indexOf(regionPrefix) !== -1) {
    if (span.hasClass(isCovered)) {
      backgroundColor = coveredRegionColor;
    } else if (span.hasClass(isNotCovered)) {
      backgroundColor = notCoveredRegionColor;
    }
  }
  setColor(span, backgroundColor)
  setFontWeight(span, null)
}

function setColor(elems, color) {
  if (color == null) {
    elems.css({'background-color': ''});
  } else {
    elems.css({'background-color': color});
  }
}

function setFontWeight(elems, weight) {
  if (weight == null) {
    elems.css({'font-weight': ''});
  } else {
    elems.css({'font-weight': weight});
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
      setColor($('span.' + preLastClass), hoverBlockColor);
    }
    const hoverRegionSpans = $('span.' + lastClass);
    setColor(hoverRegionSpans, hoverRegionColor);
    setFontWeight(hoverRegionSpans, 'bold');
  } else if (lastClass.startsWith(blockPrefix)) {
    setColor($('span.' + lastClass), hoverBlockColor);
  }
  classList.forEach(function (cls) {
    if (cls.startsWith(minusPrefix)) {
      const blockClass = cls.replace(minusPrefix, blockPrefix);
      const minusBlockSpans = $('span.' + blockClass);
      setColor(minusBlockSpans, minusBlockColor);
      setFontWeight(minusBlockSpans, 'bold');
    }
  })
}

resetColors();
$('span')
  .mouseenter(highlightSelection)
  .mouseleave(resetColors);
