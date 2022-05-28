package org.helgi.investment.model.response

case class PortfolioAssetDTO(assetName: String, weight: Double):
  require(weight > 0 && weight <= 1,
    "Weight should be in range (0, 1]")


case class PortfolioDTO(riskToleranceLowerBound: Int,
                        riskToleranceUpperBound: Int,
                        assets: List[PortfolioAssetDTO]):
  require(riskToleranceUpperBound >= riskToleranceLowerBound,
    "Upper bound should be greater than or equal to lower bound")


case class PortfolioEvaluationDTO(portfolioValue: BigDecimal,
                                  contributions: BigDecimal)
