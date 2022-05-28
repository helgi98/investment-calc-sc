package org.helgi.investment.config

case class FmpConfig(apiUrl: String, apiKey: String)

case class IntegrationConfig(fmp: FmpConfig)
