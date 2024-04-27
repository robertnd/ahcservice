package com.ahc.service.domain
object code {
  case class Code(
      id: Int, chapter: String, sub_chapter: Option[String], code: String, descr: Option[String])

  object Code {
        val empty: Code = Code(0, "", None, "", None) 

        def make(chapter: String, sub_chapter: Option[String], code: String, descr: Option[String]) = 
          Code(0, chapter, sub_chapter, code, descr)
  }
}