package vertx.github.trending;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import api.OpenApiRoutePublisher;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * 
 * @author yufan.liu
 *
 */
public class Server extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	public static final String APPLICATION_JSON = "application/json";
	Services services;

	@Override
	public void start(Future<Void> fut) {
		int cachedExpire = config().getInteger("cache.expire", 600);
		services = new Services();
		services.initialize(cachedExpire);

		// Create a router object.
		Router router = Router.router(vertx);

		// Bind "/" to our hello message.
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("<h1>Hello from Vert.x</h1>");
		});

		router.route("/assets/*").handler(StaticHandler.create("assets"));

		router.get("/api/repos").handler(services::getAll);
		router.get("/api/refresh/:timePeriod").handler(services::refresh);

		OpenAPI openAPIDoc = OpenApiRoutePublisher.publishOpenApiSpec(router, "spec", "Vertx Swagger Auto Generation",
				"1.0.0", "http://localhost:" + config().getInteger("http.port", 8080) + "/");
		openAPIDoc.addTagsItem(new io.swagger.v3.oas.models.tags.Tag().name("Repo").description("Repo operations"));

		router.get("/swagger").handler(res -> {
			res.response().setStatusCode(200).end(io.swagger.v3.core.util.Json.pretty(openAPIDoc));
		});

		// Serve the Swagger UI out on /doc/index.html
		router.route("/doc/*").handler(
				StaticHandler.create().setCachingEnabled(false).setWebRoot("api/webroot/node_modules/swagger-ui-dist"));

		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		vertx.createHttpServer().requestHandler(router::accept).listen(
				// Retrieve the port from the configuration,
				// default to 8080.
				config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						fut.complete();
					} else {
						fut.fail(result.cause());
					}
				});
		logger.info("Server started on port: " + config().getInteger("http.port", 8080));
		logger.info("Cache result for: " + cachedExpire + " seconds");
	}

	// For IDE debug
	public static void main(String args[]) {
		DeploymentOptions options = new DeploymentOptions();
		JsonObject jo = new JsonObject();
		jo.put("http.port", 8080);
		options.setConfig(jo);

		Runner.runExample(Server.class, options);
	}

}