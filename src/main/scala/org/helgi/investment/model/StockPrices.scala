package org.helgi.investment.model

import java.time.LocalDate

case class StockPrice(date: LocalDate, price: BigDecimal)

case class StockPrices(asset: String, prices: List[StockPrice])
