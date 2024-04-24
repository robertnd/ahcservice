package com.ahc.service.config

import pureconfig.ConfigSource
import pureconfig.ConfigReader
import cats.MonadThrow
import cats.implicits.*
import pureconfig.error.ConfigReaderException
import scala.reflect.ClassTag

object syntax {

    extension (source: ConfigSource) {
        def loadF[F[_], A: ClassTag](using reader: ConfigReader[A], F: MonadThrow[F]): F[A] = 
            F.pure(source.load[A]).flatMap {
                case Left(errors) => F.raiseError[A](ConfigReaderException(errors))
                case Right(value) => F.pure(value)
            }
    }
}