package com.ahc.service.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default._
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import pureconfig.error.CannotConvert

final case class DbConfig(
    noOfThreads: Int, driver: String, dbUrl: String, username: String, password: String) derives ConfigReader
