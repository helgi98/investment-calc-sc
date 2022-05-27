package org.helgi.investment.route

import cats.*
import cats.effect.{Async, IO}
import cats.implicits.*
import io.circe.generic.auto.*
import org.helgi.investment.service.*
import org.helgi.investment.util.JsonUtil.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.QueryParamDecoderMatcher


object PortfolioRoutes:
  object RiskLevelQueryParamMatcher extends QueryParamDecoderMatcher[Int]("riskLevel")

  def apply[F[_] : Async](portfolioService: PortfolioService[F]): HttpRoutes[F] =
    object dsl extends Http4sDsl[F];
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "investment-portfolio" :? RiskLevelQueryParamMatcher(level) =>
        for
          portfolioOrError <- portfolioService.getPortfolio(level).value
          res <- portfolioOrError.fold({
            case PortfolioError.PortfolioNotFound(msg) => NotFound(msg)
          }, Ok(_))
        yield res
    }


