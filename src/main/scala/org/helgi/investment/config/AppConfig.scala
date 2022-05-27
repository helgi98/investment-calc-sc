package org.helgi.investment.config

import pureconfig.*
import pureconfig.generic.derivation.default.*


case class AppConfig(db: DbConfig) derives ConfigReader