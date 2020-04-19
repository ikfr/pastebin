package com.github.hzqd.ikfr.pastebin

import arrow.core.*
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine
import org.redisson.Redisson
import org.redisson.config.Config
import kotlin.system.exitProcess

object App {

    val vertx = Vertx.vertx()
    val router = Router.router(vertx)
    val engine = ThymeleafTemplateEngine.create(vertx)

    val redissonMap = try {
        Config().let {
            it.useSingleServer().address = "redis://127.0.0.1:6379"
            Redisson.create(it)
        }.getMap<String, String>("content")
    } catch (e: Exception) {
        println("Redis connection establish failed!")
        exitProcess(1)
    }


    fun set(k: String, v: String) {
        redissonMap[k] = v
    }

    fun get(k: String): String? = redissonMap[k]


    @JvmStatic
    fun main(args: Array<String>) {
        router.route().handler(BodyHandler.create())
        router.route("/static/*").handler(StaticHandler.create("webroot/static"))

        router.get("/").handler(::index)
        router.post("/paste").handler(::paste)
        router.get("/raw/*").handler(::raw)
        router.get("/*").handler(::query)
        vertx.createHttpServer().requestHandler(router).listen(8089)
    }

    fun index(context: RoutingContext) {
        engine.render(JsonObject(), "webroot/templates/input.html") { rendering ->
            when {
                rendering.succeeded() -> context.response().end(rendering.result())
                else -> context.response().setStatusCode(500).end("Template file not Found!")
            }
        }
    }

    fun paste(context: RoutingContext) = with(context) {
        val content = request().getParam("content")
        val key = Integer.toHexString(content.hashCode()).slice((0..5))
        set(key, content)
        response().putHeader("location", key).setStatusCode(302).end()
    }

    fun query(context: RoutingContext) {
        get(context.request().uri().toString().slice(1..6)).let { content ->
            val replaceCtx = JsonObject()
                .put("content", content)
                .put("id", "raw" + context.request().uri())
            engine.render(replaceCtx, "webroot/templates/result.html") { rendering ->
                when {
                    rendering.succeeded() -> context.response().end(rendering.result())
                    else -> context.response().setStatusCode(500).end("Template file not Found!")
                }
            }
        }
    }

    fun raw(context: RoutingContext) {
        get(context.request().uri().toString().slice(5..10)).let { content ->
            context.response().end(content)
        }
    }
}