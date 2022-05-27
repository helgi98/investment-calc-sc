package org.helgi.investment.repository

import cats.effect.kernel.Concurrent
import cats.effect.{Async, IO}
import cats.implicits.*
import doobie.Read
import doobie.implicits.*
import doobie.util.transactor.Transactor

import scala.deriving.Mirror

case class PortfolioAsset(assetName: String, weight: Double)

case class Portfolio(riskToleranceLowerBound: Int,
                     riskToleranceUpperBound: Int,
                     assets: List[PortfolioAsset])

trait PortfolioRepo[F[_]]:
  def findByRiskLevel(riskLevel: Int): F[List[Portfolio]]


object PortfolioRepo:

  def apply[F[_] : Async](xa: Transactor[F]): PortfolioRepo[F] =
    new PortfolioRepo[F] :
      override def findByRiskLevel(riskLevel: Int): F[List[Portfolio]] =
        PortfolioRepo.byRiskLevelQuery(riskLevel).to[List].transact(xa).map {
          _.groupBy(_.pId).view.mapValues(rs => {
            Portfolio(rs.head.lowerBound, rs.head.upperBound,
              rs.map(r => PortfolioAsset(r.asset, r.weight)))
          }).values.toList
        }

  import Reader.*

  // search portfolio
  case class PortfolioRecord(pId: Int, lowerBound: Int, upperBound: Int, asset: String, weight: Double)

  def byRiskLevelQuery(level: Int): doobie.Query0[PortfolioRecord] =
    sql"""
         |SELECT p.id, p.risk_lower_bound, p.risk_upper_bound, a.name, pa.weight
         |FROM portfolio p INNER JOIN portfolio_asset pa on INNER JOIN asset a
         |WHERE $level between p.risk_lower_bound and p.risk_upper_bound
       """.stripMargin
      .query[PortfolioRecord]


  object Reader:

    import doobie.implicits.javatimedrivernative.*

    implicit val readPortfolio: Read[PortfolioRecord] =
      Read[(Int, Int, Int, String, Double)]
        .map(summon[Mirror.Of[PortfolioRecord]].fromProduct(_))
