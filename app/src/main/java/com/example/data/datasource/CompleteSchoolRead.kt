package com.example.data.datasource

internal data class SchoolPage<T>(val rows: List<T>, val remainingCount: Long?)

/**
 * Walk the server's unique ID ordering, even when its row cap is below our page size.
 * Missing counts, changing cardinality, repeated pages and safety limits fail closed;
 * a partially loaded list is never returned as a complete school snapshot.
 * Separate HTTP pages are not a database-wide point-in-time transaction.
 */
internal suspend fun <T> readCompleteRows(
  pageSize: Int = 200,
  maxRows: Int = 100_000,
  id: (T) -> String,
  fetch: suspend (after: String?, limit: Int) -> SchoolPage<T>
): List<T> {
  require(pageSize > 0 && maxRows > 0)
  val result = mutableListOf<T>()
  val seen = mutableSetOf<String>()
  var cursor: String? = null
  var total: Long? = null
  while (true) {
    val page = fetch(cursor, pageSize)
    val remaining = checkNotNull(page.remainingCount) { "School data completeness could not be verified. Retry when connected." }
    if (total == null) total = remaining
    check(total in 0..maxRows.toLong()) { "School data exceeds the supported snapshot limit. No partial totals were loaded." }
    check(remaining == total - result.size) { "School data changed during pagination. Reload before using totals." }
    check(page.rows.size <= pageSize && page.rows.size.toLong() <= remaining) { "Invalid school data page." }
    check(page.rows.isNotEmpty() || remaining == 0L) { "School data ended before the verified row count." }
    for (row in page.rows) {
      check(id(row).isNotBlank() && seen.add(id(row))) { "School data page repeated an identity. Reload before using totals." }
      result.add(row)
    }
    if (result.size.toLong() == total) {
      if (total != 0L) {
        check(fetch(null, 1).remainingCount == total) { "School data changed before the snapshot completed." }
      }
      return result
    }
    cursor = id(page.rows.last())
  }
}
