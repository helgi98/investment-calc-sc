package org.helgi.investment.integration

import cats.effect.Async
import io.circe.generic.auto.*
import org.helgi.investment.config.FmpConfig
import org.helgi.investment.util.JsonUtil.*
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

import java.time.LocalDate

case class FMPStockPrice(date: LocalDate, close: BigDecimal)

case class FMPStockPriceResponse(symbol: String, historical: List[FMPStockPrice])

trait FmpApiClient[F[_]]:
  def getStockPrices(assetName: String, from: LocalDate, to: LocalDate): F[FMPStockPriceResponse]


object FmpApiClient:
  def apply[F[_] : Async](client: Client[F], config: FmpConfig): FmpApiClient[F] =
    new FmpApiClient[F] :
      override def getStockPrices(assetName: String, from: LocalDate, to: LocalDate): F[FMPStockPriceResponse] =
        val url = (Uri.fromString(config.apiUrl).toOption.get / "historical-price-full" / assetName)
          .withQueryParam("serietype", "line")
          .withQueryParam("apikey", config.apiKey)

        client.expect[FMPStockPriceResponse](url)

