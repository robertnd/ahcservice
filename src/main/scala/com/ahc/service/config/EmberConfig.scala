package com.ahc.service.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default._
import com.comcast.ip4s.{Host,Port}
import pureconfig.error.CannotConvert

final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {

    given hostReader: ConfigReader[Host] = ConfigReader[String].emap {
        hostStr => Host
            .fromString(hostStr)
            .toRight(CannotConvert(hostStr, Host.getClass.toString(), s"Invalid host string: $hostStr"))
        }
    
    given portReader: ConfigReader[Port] = ConfigReader[Int].emap {
        port => Port
            .fromInt(port) 
            .toRight(CannotConvert(port.toString(), Host.getClass.toString(), s"Invalid host string: $port"))
    }
}
