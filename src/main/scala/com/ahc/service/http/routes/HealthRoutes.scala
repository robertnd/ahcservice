package com.ahc.service.http.routes

import cats.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class HealthRoutes[F[_]: Monad] extends Http4sDsl[F] {

    val healthRoutes: HttpRoutes[F] = HttpRoutes.of[F] { 
        case GET -> Root => Ok("Health OK")
    }

    val routes =  Router("/health" -> healthRoutes)
}

object HealthRoutes {

    def apply[F[_]: Monad] = new HealthRoutes[F]
}
