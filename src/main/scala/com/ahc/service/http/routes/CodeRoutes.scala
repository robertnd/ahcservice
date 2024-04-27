package com.ahc.service.http.routes

import cats.effect.*
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import java.util.UUID
import com.ahc.service.domain.code.*
import com.ahc.service.core.*
import scala.collection.mutable
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import com.ahc.service.http.responses.FailureResponse
import org.typelevel.log4cats.Logger
import cats.effect.std.Console
import com.ahc.service.http.validation.syntax.*

class CodeRoutes[F[_]: Concurrent: Logger: Console] private (codes: Codes[F])
    extends HttpValidationDsl[F] {

  private val allCodeRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    for {
      codesList <- codes.all()
      resp      <- Ok(codesList)
    } yield resp
  }

  // GET /codes/code
  private val findCodeRoute: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / code =>
    codes.find(code) flatMap {
      case Some(code) => Ok(code)
      case None       => NotFound(FailureResponse(s"Code ${code} not found"))
    }
  }

  import com.ahc.service.logging.syntax._
  private val searchByTagRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "search" / tags =>
      for {
        codesList <- codes.findByTags(tags).logError(e => s"findByTag failed: ${e}")
        resp      <- Ok(codesList)
      } yield resp
  }

  val routes = Router(
    "/codes" -> (allCodeRoutes <+> findCodeRoute <+> searchByTagRoute)
  )
}

object CodeRoutes {
  def make[F[_]: Concurrent: Logger: Console](codes: Codes[F]) = new CodeRoutes[F](codes)
}
