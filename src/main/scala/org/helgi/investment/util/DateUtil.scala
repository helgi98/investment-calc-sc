package org.helgi.investment.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

implicit val localDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

object DateUtil:
  extension (date: LocalDate)
    def firstDayOfMonth: LocalDate = date.withDayOfMonth(1)