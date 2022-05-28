package org.helgi.investment.repository

import cats.*
import cats.effect.Async
import cats.implicits.*
import org.helgi.investment.integration.{FMPStockPriceResponse, FmpApiClient}
import org.helgi.investment.model.{StockPrice, StockPrices}

import java.time.LocalDate

trait StockPricesRepo[F[_]]:
  def getStockPrices(assets: List[String], from: LocalDate, to: LocalDate): F[List[StockPrices]]


object StockPricesRepo:
  def fmp[F[_] : Async](fmpApiClient: FmpApiClient[F]): StockPricesRepo[F] =
    new StockPricesRepo[F] :
      override def getStockPrices(assets: List[String], from: LocalDate, to: LocalDate): F[List[StockPrices]] =
        assets.map[F[StockPrices]] { a =>
          fmpApiClient.getStockPrices(a, from, to).map { fmpData =>
            StockPrices(fmpData.symbol, fmpData.historical.map {
              r => StockPrice(r.date, r.close)
            })
          }
        }.sequence

