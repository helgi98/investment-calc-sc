package org.helgi.investment.config

import pureconfig.*
import pureconfig.generic.derivation.default.*

case class DbConfig(driver: String,
                    url: String,
                    user: String,
                    password: String,
                    pool: Int)

case class ServerConfig(host: String = "localhost", port: Int = 8080)

case class FmpConfig(apiUrl: String, apiKey: String)

case class IntegrationConfig(fmp: FmpConfig)

case class AppConfig(server: ServerConfig, db: DbConfig, integration: IntegrationConfig) derives ConfigReader