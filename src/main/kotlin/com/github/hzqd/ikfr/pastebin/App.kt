package com.github.hzqd.ikfr.pastebin

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.redisson.Redisson
import org.redisson.config.Config
import java.nio.charset.StandardCharsets

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

        router.get("/").handler(::index)
        router.post("/paste").handler(::paste)
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
    fun paste(context: RoutingContext) {
        val content = context.request().getParam("content")
        println(content)
    }


}