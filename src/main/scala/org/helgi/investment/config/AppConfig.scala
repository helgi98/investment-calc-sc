package org.helgi.investment.config

import pureconfig.*
import pureconfig.generic.derivation.default.*


case class AppConfig(server: ServerConfig, db: DbConfig) derives ConfigReader