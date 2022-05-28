package org.helgi.investment.route

import cats.effect.Async
import cats.implicits.*
import io.circe.generic.auto.*
import org.helgi.investment.service.*
import org.helgi.investment.util.*
import org.helgi.investment.util.JsonUtil.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.{QueryParamDecoderMatcher, ValidatingQueryParamDecoderMatcher}
import org.http4s.{HttpRoutes, Response}

import java.time.LocalDate


object PortfolioRoutes:
  object riskLevelQP extends QueryParamDecoderMatcher[Int]("riskLevel")

  object contributionQP extends ValidatingQueryParamDecoderMatcher[BigDecimal]("contribution")(positiveDecoder(0))

  object fromQP extends ValidatingQueryParamDecoderMatcher[LocalDate]("from")(pastOrPresentDateDecoder)

  object toQP extends ValidatingQueryParamDecoderMatcher[LocalDate]("to")(pastOrPresentDateDecoder)

  def apply[F[_] : Async](portfolioService: PortfolioService[F]): HttpRoutes[F] =
    object dsl extends Http4sDsl[F];
    import dsl.*

    def handlePortfolioError(pe: PortfolioError): F[Response[F]] = pe match
      case PortfolioError.PortfolioNotFound(msg) => NotFound(msg)
      case PortfolioError.ValidationError(msg) => BadRequest(msg)


    HttpRoutes.of {
      case GET -> Root / "investment-portfolio" :? riskLevelQP(level) =>
        for
          portfolioOrError <- portfolioService.getPortfolio(level).value
          res <- portfolioOrError.fold(handlePortfolioError, Ok(_))
        yield res

      case GET -> Root / "investment-portfolio" / "evaluate" :?
        fromQP(from) +& toQP(to) +& contributionQP(contribution) +& riskLevelQP(level) =>
        (from, to, contribution).tupled.fold(
          errs => BadRequest(errs.map(_.sanitized).toList.mkString(";\n")),
          (from, to, contribution) => {
            for
              resOrError <- portfolioService.evaluatePortfolio(from, to, contribution, level).value
              res <- resOrError.fold(handlePortfolioError, Ok(_))
            yield res
          })
    }



