package org.helgi.investment.integration

import cats.data.EitherT
import cats.effect.Async
import cats.implicits.*
import io.circe.generic.auto.*
import org.helgi.investment.config.FmpConfig
import org.helgi.investment.util.JsonUtil.*
import org.http4s.Status.Successful
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{EntityDecoder, Request, Uri}

import java.time.LocalDate

case class FMPStockPrice(date: LocalDate, close: BigDecimal)

case class FMPStockPriceResponse(symbol: String, historical: List[FMPStockPrice])

trait FmpApiClient[F[_]]:
  def getStockPrices(assetName: String, from: LocalDate, to: LocalDate): EitherT[F, String, FMPStockPriceResponse]

object FmpApiClient:
  def apply[F[_] : Async](client: Client[F], config: FmpConfig): FmpApiClient[F] =
    new FmpApiClient[F] :
      override def getStockPrices(assetName: String, from: LocalDate, to: LocalDate): EitherT[F, String, FMPStockPriceResponse] =
        val url = Uri.fromString(config.apiUrl).toOption.get / "historical-price-full" / assetName +?
          ("serietype" -> "line") +? ("apikey" -> config.apiKey)

        EitherT(client.run(Request[F](uri = url)).use {
          case Successful(resp) =>
            implicitly[EntityDecoder[F, FMPStockPriceResponse]].decode(resp, strict = false)
              .leftMap(_ => "Unexpected fmp data").value
          case failed => Left("Couldn't fetch fmp data").pure[F]
        })