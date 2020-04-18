package com.github.hzqd.ikfr.pastebin

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine
import org.redisson.Redisson
import org.redisson.api.RMap
import org.redisson.config.Config

object App {

    val vertx = Vertx.vertx()
    val router = Router.router(vertx)
    val engine = ThymeleafTemplateEngine.create(vertx)
    val redissonMap: Option<RMap<String, String>> = Option(
        Config().let {
            it.useSingleServer().address = "redis://127.0.0.1:6379"
            Redisson.create(it)
        }.getMap("content")
    )

    fun set(k: String, v: String) {
        return
        when (redissonMap) {
            is Some<RMap<String, String>> -> redissonMap.t[k] = v
            is None -> println("None")
        }
    }

    fun get(k: String): String = when (redissonMap) {
        is Some<RMap<String, String>> -> redissonMap.t[k] ?: "None"
        is None -> "None"
    }


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
        engine.render(JsonObject(), "webroot/templates/input.html") {
        when {
            it.succeeded() -> context.response().end(it.result())
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
        get(context.request().uri().toString().slice(1..6)).let {content ->
            val replacectx = JsonObject()
                .put("content", content)
                .put("id","raw" + context.request().uri())
            engine.render(replacectx, "webroot/templates/result.html") {rendering ->
                when {
                    rendering.succeeded() -> context.response().end(rendering.result())
                    else -> context.response().setStatusCode(500).end("Template file not Found!")
                }
            }
        }
    }

    fun raw(context: RoutingContext) {
        get(context.request().uri().toString().slice(5..10)).let {content ->
                context.response().end(content)
            }
        }
}