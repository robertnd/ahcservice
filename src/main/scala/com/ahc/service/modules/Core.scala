package com.ahc.service.modules


import com.ahc.service.core.Codes
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import com.ahc.service.core.LiveCodes
import doobie.util.transactor.Transactor
import com.ahc.service.core.Codes

final class Core[F[_]] private (val codes: Codes[F])

object Core {

  def make[F[_]: Async](xa: Transactor[F]): Resource[F, Core[F]] =
    Resource
      .eval(LiveCodes[F](xa))
      .map(codes => new Core(codes))
}
