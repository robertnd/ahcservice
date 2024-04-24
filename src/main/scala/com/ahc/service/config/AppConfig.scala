package com.ahc.service.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default._

final case class AppConfig(
    dbConfig: DbConfig, emberConfig: EmberConfig)  derives ConfigReader
