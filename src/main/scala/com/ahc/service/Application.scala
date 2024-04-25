package com.ahc.service

import cats._
import cats.implicits._
import cats.effect._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import com.ahc.service.http.routes
import pureconfig.ConfigSource
import com.ahc.service.config.*
import com.ahc.service.config.syntax.*
import pureconfig.error.ConfigReaderException
import com.ahc.service.http.HttpApi
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.std.Console
import com.ahc.service.modules.*
import org.http4s.server.middleware.CORS

object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  val console: Console[IO] = Console[IO]

  val configSource = ConfigSource.default.load[EmberConfig]

  override def run: IO[Unit] = ConfigSource.default.loadF[IO, AppConfig].flatMap { case AppConfig(dbConfig, emberConfig) => 
        val appResource = for {
            xa <- DB.makeDBResource[IO](dbConfig)
            core <- Core.make[IO](xa)
            httpApi <- HttpApi[IO](core)
            server <- EmberServerBuilder
            .default[IO]
            .withHost(emberConfig.host)
            .withPort(emberConfig.port)
            .withHttpApp(CORS.policy.withAllowOriginAll(httpApi.endpoints.orNotFound)) 
            .build
        } yield server

        appResource.use(_ => IO.uncancelable(_ => IO.println("AHC Server Started ...") *> IO.never))
  }
}
