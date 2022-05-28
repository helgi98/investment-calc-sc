package org.helgi.investment.repository

import cats.effect.Async
import cats.implicits.*
import doobie.implicits.*
import doobie.util.transactor.Transactor

case class PortfolioAsset(assetName: String, weight: Double)

case class Portfolio(riskToleranceLowerBound: Int,
                     riskToleranceUpperBound: Int,
                     assets: List[PortfolioAsset])

trait PortfolioRepo[F[_]]:
  def findByRiskLevel(riskLevel: Int): F[List[Portfolio]]


object PortfolioRepo:

  def apply[F[_] : Async](ta: Transactor[F]): PortfolioRepo[F] =
    new PortfolioRepo[F] :
      override def findByRiskLevel(riskLevel: Int): F[List[Portfolio]] =
        PortfolioRepo.byRiskLevelQuery(riskLevel).to[List].transact(ta).map {
          _.groupBy(_.pId).view.mapValues(rs => {
            Portfolio(rs.head.lowerBound, rs.head.upperBound,
              rs.map(r => PortfolioAsset(r.asset, r.weight)))
          }).values.toList
        }

  // search portfolio
  case class PortfolioRecord(pId: Int, lowerBound: Int, upperBound: Int, asset: String, weight: Double)

  def byRiskLevelQuery(level: Int): doobie.Query0[PortfolioRecord] =
    sql"""
         |SELECT p.id, p.risk_lower_bound, p.risk_upper_bound, a.name, pa.weight
         |FROM portfolio p INNER JOIN portfolio_asset pa on INNER JOIN asset a
         |WHERE $level between p.risk_lower_bound and p.risk_upper_bound
       """.stripMargin
      .query[PortfolioRecord]

