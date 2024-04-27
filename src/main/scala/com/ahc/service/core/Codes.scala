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
import org.typelevel.log4cats.Logger
import cats.effect.std.Console

trait Codes[F[_]] {
  def all(): F[List[Code]]
  def find(code: String): F[Option[Code]]
//   def findByTag(tag: String): F[List[Code]]
  def findByTags(tags: String): F[List[Code]]
}

class LiveCodes[F[_]: MonadCancelThrow] private (xa: Transactor[F]) extends Codes[F] {

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

  import com.ahc.service.logging.syntax._
  def findByTag(tag: String): F[List[Code]] = {
    val columns            = fr"SELECT id, chapter, sub_chapter, code, descr FROM code "
    val where              = fr"WHERE code LIKE ${"%" + tag + "%"} "
    val chapterORClause    = fr"OR chapter LIKE ${"%" + tag + "%"} "
    val subChapterORClause = fr"OR sub_chapter LIKE ${"%" + tag + "%"} "
    val descORClause       = fr"OR descr LIKE ${"%" + tag + "%"} "
    val statement = columns ++ where ++ chapterORClause ++ subChapterORClause ++ descORClause
    val action    = statement.query[Code].to[List]
    action.transact(xa)
  }

    //(using F: MonadCancelThrow[F])
  override def findByTags(tags: String): F[List[Code]] = {
    val F = implicitly[MonadCancelThrow[F]] 
    val s        = tags.trim().split("\\+")
    val tagLists = s.map(tag => findByTag(tag))
    tagLists.foldLeft(F.pure(List.empty[Code]))((Facc, item) =>
      for {
        acc     <- Facc
        newItem <- item
      } yield acc ++ newItem
    )
  }
}

object LiveCodes {
  // id, chapter, sub_chapter, code, descr
  given codeRead: Read[Code] = Read[(Int, String, Option[String], String, Option[String])].map {
    case (
          id: Int,
          chapter: String,
          sub_chapter: Option[String] @unchecked,
          code: String,
          descr: Option[String] @unchecked
        ) =>
      Code(
        id = id,
        chapter = chapter,
        sub_chapter = sub_chapter,
        code = code,
        descr = descr
      )
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): F[LiveCodes[F]] =
    new LiveCodes[F](xa).pure[F]
}
