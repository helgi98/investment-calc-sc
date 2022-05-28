package org.helgi.investment.service

import cats.*
import cats.data.EitherT
import cats.effect.Async
import cats.implicits.*
import org.helgi.investment.model.StockPrices
import org.helgi.investment.model.response.*
import org.helgi.investment.repository.*
import org.helgi.investment.service.PortfolioError.*

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum PortfolioError extends Exception :
  case PortfolioNotFound(msg: String)
  case ValidationError(msg: String)

trait PortfolioService[F[_]]:
  def getPortfolio(riskLevel: Int): EitherT[F, PortfolioError, PortfolioDTO]

  def evaluatePortfolio(from: LocalDate,
                        to: LocalDate,
                        contribution: BigDecimal,
                        riskLevel: Int): EitherT[F, PortfolioError, PortfolioEvaluationDTO]


object PortfolioService:
  def apply[F[_] : Async](portfolioRepo: PortfolioRepo[F], stockPricesRepo: StockPricesRepo[F]): PortfolioService[F] =
    new PortfolioService[F] :
      override def getPortfolio(riskLevel: Int): EitherT[F, PortfolioError, PortfolioDTO] =
        EitherT(portfolioRepo.findByRiskLevel(riskLevel).map {
          _.headOption match
            case Some(portfolio) => Right(PortfolioDTO(portfolio.riskToleranceLowerBound, portfolio.riskToleranceUpperBound,
              portfolio.assets.map(pa => PortfolioAssetDTO(pa.assetName, pa.weight))))
            case None => Left(PortfolioNotFound(f"'$riskLevel' level portfolio not found"))
        })

      override def evaluatePortfolio(from: LocalDate,
                                     to: LocalDate,
                                     contribution: BigDecimal,
                                     riskLevel: Int): EitherT[F, PortfolioError, PortfolioEvaluationDTO] =
        if (from.isAfter(to)) {
          return EitherT.leftT(ValidationError("[From] date should not be after [To] date"))
        }

        for
          portfolio <- getPortfolio(riskLevel)
          assetNames = portfolio.assets.map(_.assetName)
          stockPrices <- EitherT.right(stockPricesRepo.getStockPrices(assetNames, from, to))
        yield evaluatePortfolio(portfolio.assets, stockPrices, contribution, from, to)

      def evaluatePortfolio(assets: List[PortfolioAssetDTO],
                            stockPrices: List[StockPrices],
                            contribution: BigDecimal,
                            from: LocalDate,
                            to: LocalDate): PortfolioEvaluationDTO =
        val stockPricesMap = stockPrices.map(
          sp => sp.asset -> sp.prices.groupMapReduce(_.date.withDayOfMonth(1))(identity)
          ((x, y) => if (x.date.isBefore(y.date)) x else y)
        ).toMap

        val portfolioValue = assets.filter(stockPricesMap contains _.assetName)
          .map { it =>
            val assetContribution = contribution * it.weight
            val shares = stockPricesMap(it.assetName)
              .map((_, priceData) => assetContribution / priceData.price)
              .sum
            val lastPrice = stockPricesMap(it.assetName).maxBy((d, _) => d)._2.price
            shares * lastPrice
          }.sum

        val totalContribution = contribution * investmentPeriods(from.withDayOfMonth(1), to.withDayOfMonth(1))

        PortfolioEvaluationDTO(portfolioValue, totalContribution)

      def investmentPeriods(from: LocalDate, to: LocalDate): Long =
        ChronoUnit.MONTHS.between(from.withDayOfMonth(1), to.withDayOfMonth(1)) + 1
