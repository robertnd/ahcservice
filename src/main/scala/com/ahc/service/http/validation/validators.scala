package com.ahc.service.http.validation

import com.ahc.service.domain.code.Code
import cats.data.Validated.*
import cats.data.*
import cats.implicits.*
import java.net.URL
import scala.util.*

object validators {

    sealed trait ValidationFailure(val errorMessage: String)
    case class EmptyField(fieldName: String) extends ValidationFailure(s"'${fieldName}' is empty")
    case class InvalidURL(fieldName: String) extends ValidationFailure(s"'${fieldName}' is not a valid URL")

    type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

    trait Validator[A] {
       def validate(value: A): ValidationResult[A]
    }

    def validatedRequired[A](field: A, fieldName: String)(required: A => Boolean): ValidationResult[A] = 
        if (required(field)) field.validNel else EmptyField(fieldName).invalidNel

    def validateUrl(field: String, fieldName: String): ValidationResult[String] =
        Try(URL(field).toURI()) match {
            case Success(_) => field.validNel
            case Failure(exception) => InvalidURL(fieldName).invalidNel
        }

    given codeValidator: Validator[Code] = (codeVal: Code) => {
        val Code(
            id, chapter, sub_chapter, code, descr
        ) = codeVal

        val validChapter = validatedRequired(chapter, "chapter")(_.nonEmpty)
        val validCode = validatedRequired(code, "code")(_.nonEmpty)

        (validChapter, sub_chapter.validNel, validCode,descr.validNel).mapN(Code.make)
    }

}
