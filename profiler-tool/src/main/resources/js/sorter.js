// Adds click-sorting functionality on column headers of all sortable tables using jQuery.
$(document).ready(function () {
  $('table.sortable th').click(function () {
    sortBy($(this));
  });
});

/**
 * Sorts the rows of a table by the values in the given column.
 *
 * By default, the sorting is ascending, but if the column is a metric, it is sorted in descending order first.
 * If the column is already sorted, the order is simply inverted.
 * @param colHeader the header of the column to sort by
 */
function sortBy(colHeader) {
  const table = colHeader.parents('table').eq(0);
  let currentSortOrder = getCurrentSortOrder(colHeader);
  let newSortOrder;
  let dataRows = table.find('tr:gt(0)').toArray(); // rows without header
  if (currentSortOrder !== null) { // invert order without sorting
    dataRows = dataRows.reverse();
    newSortOrder = currentSortOrder === 'desc' ? 'asc' : 'desc';
  } else { // sort new by column
    newSortOrder = 'asc';
    const colIndex = colHeader.index();
    dataRows = dataRows.sort(newRowComparator(colIndex));
    // if the column is a metric, sort in descending order first (but allow the user to toggle)
    if (colHeader.hasClass('metric')) {
      dataRows = dataRows.reverse();
      newSortOrder = 'desc';
    }
  }
  table.append(dataRows); // redefine order of rows
  table.find('th').removeClass('asc desc'); // remove sorting classes from all column headers
  colHeader.addClass(newSortOrder);
}

/**
 * Returns the current sort order of the given column header.
 * @param col the column header to get the sort order from
 * @returns {string|null} the current sort order ('asc' or 'desc') or null if the column is not sorted
 */
function getCurrentSortOrder(col) {
  return col.hasClass('desc') ? 'desc' : col.hasClass('asc') ? 'asc' : null;
}

/**
 * Returns a comparator function for sorting table rows by the values in the given column (in ascending order).
 * If the values in the column are numeric, they are sorted numerically, otherwise lexicographically.
 * @param index the index of the column to sort by
 * @returns {function(*, *): number|number} a comparator function that compares two rows
 */
function newRowComparator(index) {
  return function (a, b) {
    const valA = getCellValue(a, index);
    const valB = getCellValue(b, index);
    return $.isNumeric(valA) && $.isNumeric(valB) ? valA - valB : valA.toString().localeCompare(valB);
  };
}

/**
 * Returns the value of the cell in the given row at the given index.
 * @param row the row to get the value from
 * @param index the index of the column to get the value from
 * @returns {*|jQuery} the value of the cell
 */
function getCellValue(row, index) {
  return $(row).children('td').eq(index).text();
}
