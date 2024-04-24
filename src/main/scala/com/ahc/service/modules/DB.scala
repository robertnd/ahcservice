package com.ahc.service.modules

import com.ahc.service.config.DbConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import cats.effect.* 
import cats.effect.implicits.*
import cats.implicits.*

object DB {

    def makeDBResource[F[_] : Async](config: DbConfig): Resource[F, HikariTransactor[F]] = for {
        ec <- ExecutionContexts.fixedThreadPool[F](config.noOfThreads)
        xa <- HikariTransactor.newHikariTransactor[F](
        config.driver,
        config.dbUrl,
        config.username,
        config.password,
        ec
        )
    } yield xa
}