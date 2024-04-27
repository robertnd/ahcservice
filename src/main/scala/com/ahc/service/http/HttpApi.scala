package com.ahc.service.http

import cats.effect.*
import cats.implicits.*
import cats.effect.implicits.*
import com.ahc.service.http.routes.*
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import cats.effect.std.Console
import com.ahc.service.core.Codes
import com.ahc.service.modules.*
import doobie.util.transactor.Transactor

class HttpApi[F[_]: Concurrent: Logger: Console] private (core: Core[F]) {

  private val healthRoutes = HealthRoutes[F].routes
  private val codeRoutes   = CodeRoutes.make[F](core.codes)
  val endpoints = Router(
    "/api" -> (healthRoutes <+> codeRoutes.routes)
  )
}

object HttpApi {
  def apply[F[_]: Concurrent: Logger: Console](core: Core[F]): Resource[F, HttpApi[F]] =
    Resource.pure(new HttpApi[F](core))
}
