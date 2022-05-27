package org.helgi.investment.service

import cats.*
import cats.data.EitherT
import cats.effect.Async
import cats.implicits.*
import org.helgi.investment.model.response.*
import org.helgi.investment.repository.*

trait PortfolioService[F[_]]:
  def getPortfolio(riskLevel: Int): EitherT[F, String, PortfolioDTO]

  def evaluatePortfolio: EitherT[F, String, PortfolioEvaluationDTO]


object PortfolioService:
  def apply[F[_] : Async](portfolioRepo: PortfolioRepo[F]): PortfolioService[F] =
    new PortfolioService[F] :
      override def getPortfolio(riskLevel: Int): EitherT[F, String, PortfolioDTO] =
        EitherT(portfolioRepo.findByRiskLevel(riskLevel).map {
          _.headOption match {
            case Some(portfolio) => Right(PortfolioDTO(portfolio.riskToleranceLowerBound, portfolio.riskToleranceUpperBound,
              portfolio.assets.map(pa => PortfolioAssetDTO(pa.assetName, pa.weight))))
            case None => Left("Portfolio not found")
          }
        })

      override def evaluatePortfolio: EitherT[F, String, PortfolioEvaluationDTO] = ???