package com.ahc.service.http.validation

import org.http4s.circe.CirceEntityCodec.*
import io.circe.generic.auto.*
import cats.* 
import cats.implicits.* 
import cats.data.* 
import cats.data.Validated.*
import org.http4s.* 
import org.http4s.implicits.*
import validators.*
import org.typelevel.log4cats.Logger
import com.ahc.service.logging.syntax.*
import com.ahc.service.http.responses.FailureResponse
import org.http4s.dsl.Http4sDsl

object syntax {

    def validateEntity[A](entity: A)(using validator: Validator[A]): ValidationResult[A] = validator.validate(entity)

    trait HttpValidationDsl[F[_] : MonadThrow : Logger] extends Http4sDsl[F] {
        extension (req: Request[F]) {
            def validate[A : Validator ](ifValid: A => F[Response[F]])(using EntityDecoder[F,A]): F[Response[F]] = 
                req
                    .as[A]
                    .logError(e => s"Parse failed: ${e}")
                    .map(validateEntity)
                    .flatMap {
                        case Valid(entity) => ifValid(entity)
                        case Invalid(errors) => BadRequest(FailureResponse(errors.toList.map(_.errorMessage).mkString(", ")))
                    }
        }
    }
}