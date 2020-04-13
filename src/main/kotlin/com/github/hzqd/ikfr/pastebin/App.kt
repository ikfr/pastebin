package com.github.hzqd.ikfr.pastebin

import com.google.common.hash.Hashing
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import org.redisson.Redisson
import org.redisson.config.Config
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextUInt

object App {
    val redissonClient = Config().let {
        it.useSingleServer()
            .setAddress("redis://127.0.0.1:6379")
        Redisson.create(it)
    }


    val redissonMap = redissonClient.getMap<String,String>("content")
    fun set(k:String, v:String) {
        redissonMap[k] = v
    }
    fun get(k:String) : String?{
        return redissonMap[k]
    }


    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create())
        router.get("/").handler(::index)
        router.post("/paste").handler(::paste)
        router.get("/*").handler(::query)
        vertx.createHttpServer().requestHandler(router).listen(8089)
    }

    fun index(context: RoutingContext) {
        context.response().end("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Document</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <form action=\"paste\" method=\"post\">\n" +
                "        <input type=\"text\" name=\"content\">\n" +
                "        <input type=\"submit\">\n" +
                "    </form>\n" +
                "</body>\n" +
                "</html>")
    }
    fun paste(context: RoutingContext) = with(context) {
        val content = request().getParam("content")
        val key =  Integer.toHexString(content.hashCode()).slice((0..5))
        set(key, content)
        response().putHeader("location", key).setStatusCode(302).end()
    }
    fun query(context: RoutingContext) {
        try {
            val content = get(context.request().uri().toString().slice(1..6))
            context.response().end(content)
        } catch (e: Exception) {
            context.response().setStatusCode(404).end("not found")
        }
    }

}