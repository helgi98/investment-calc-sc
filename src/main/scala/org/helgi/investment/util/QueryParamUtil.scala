package org.helgi.investment.util

import org.http4s.{ParseFailure, QueryParamDecoder}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}

implicit def localDateParamDecoder(implicit formatter: DateTimeFormatter): QueryParamDecoder[LocalDate] =
  QueryParamDecoder[String].map(LocalDate.parse(_, formatter))

def pastOrPresentDateDecoder(using localDateDecoder: QueryParamDecoder[LocalDate]): QueryParamDecoder[LocalDate] =
  localDateDecoder.emap(date => Either.cond(date.isAfter(LocalDate.now()), date, ParseFailure("Date is in future", "")))

implicit val bigDecimalParamDecoder: QueryParamDecoder[BigDecimal] =
  QueryParamDecoder[String].map(BigDecimal.apply)

def positiveDecoder[A](zero: A)(using aDecoder: QueryParamDecoder[A], ordering: Ordering[A]) =
  aDecoder.emap(a => Either.cond(ordering.lteq(a, zero), a, ParseFailure("", "")))
