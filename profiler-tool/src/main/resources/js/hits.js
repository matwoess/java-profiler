function addLineHits() {
  const codeRows = document.querySelectorAll('tr');
  codeRows.forEach(row => {
    const codeCell = row.querySelector('td.code');
    if (!codeCell) return;
    const hitsCell = document.createElement('td');
    hitsCell.classList.add('hits');
    // insert hits-span for each region-span in the code-cell
    const spans = codeCell.querySelectorAll('span.r');
    spans.forEach(span => {
      const hitCount = span.title.match(/(\d+) hit/);
      if (hitCount) {
        const newSpan = span.cloneNode(true);
        newSpan.removeAttribute('title');
        newSpan.textContent = hitCount[1];
        hitsCell.appendChild(newSpan);
      }
    });
    // add as column before the code-cell
    row.insertBefore(hitsCell, codeCell);
  });
}

document.addEventListener("DOMContentLoaded", addLineHits);