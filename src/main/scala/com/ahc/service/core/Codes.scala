package com.ahc.service.core

import com.ahc.service.domain.code._
import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.*

trait Codes[F[_]] {
    def all(): F[List[Code]]
    def find(code: String): F[Option[Code]]
}

class LiveCodes[F[_]: MonadCancelThrow ] private (xa: Transactor[F]) extends Codes[F] {

    override def all(): F[List[Code]] = 
        sql"""
            SELECT id, chapter, sub_chapter, code, descr FROM code
        """
        .query[Code]
        .to[List]
        .transact(xa)

    override def find(code: String): F[Option[Code]] = 
        sql"""
            SELECT id, chapter, sub_chapter, code, descr FROM code WHERE code = $code
        """
        .query[Code]
        .option
        .transact(xa)
}

object LiveCodes {

    // id, chapter, sub_chapter, code, descr
    given codeRead: Read[Code] = Read[
        (Int, String, Option[String], String, Option[String])].map {
                case (
                id: Int,
                chapter: String,
                sub_chapter: Option[String] @unchecked,
                code: String,
                descr: Option[String] @unchecked) => Code(
                    id = id,
                    chapter = chapter,
                    sub_chapter = sub_chapter,
                    code = code,
                    descr = descr
                )
            }

    def apply[F[_] : MonadCancelThrow](xa: Transactor[F]): F[LiveCodes[F]] = new LiveCodes[F](xa).pure[F]
}