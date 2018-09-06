package vertx.github.trending;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 
 * @author yufan.liu
 *
 */
public class Server extends AbstractVerticle {

	private Map<Integer, Repo> repos = new LinkedHashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	LoadingCache<String, Map<Integer, Repo>> cache;

	@Override
	public void start(Future<Void> fut) {
		int cachedExpire = config().getInteger("cache.expire", 600);
		initialize(cachedExpire);

		// Create a router object.
		Router router = Router.router(vertx);

		// Bind "/" to our hello message.
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("<h1>Hello from Vert.x</h1>");
		});

		router.route("/assets/*").handler(StaticHandler.create("assets"));

		router.get("/api/repos").handler(this::getAll);
		router.get("/api/refresh/:timePeriod").handler(this::refresh);
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

	private void refresh(RoutingContext routingContext) {
		String timePeriod = routingContext.request().getParam("timePeriod");
		//String language = routingContext.request().getParam("language");

		try {
			repos = cache.get(timePeriod);
			routingContext.response().setStatusCode(200).end();
			logger.info("Refresh service succeed, return " + repos.size() + " results");
		} catch (ExecutionException e) {
			routingContext.response().setStatusCode(500).end();
			logger.error("internal error: ", e);
		}

	}

	private void getAll(RoutingContext routingContext) {
		// Write the HTTP response
		// The response is in JSON using the utf-8 encoding
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(repos.values()));
	}

	private void initialize(int cachedExpire) {
		CacheLoader<String, Map<Integer, Repo>> loader;
		loader = new CacheLoader<String, Map<Integer, Repo>>() {
			@Override
			public Map<Integer, Repo> load(String key) throws ClientProtocolException, IOException {
				return generateRepoSet(key);
			}
		};
		
		cache = CacheBuilder.newBuilder().expireAfterAccess(cachedExpire, TimeUnit.SECONDS).build(loader);

		try {
			repos = cache.get("one_year");
			logger.info("Initialize succeed, return " + repos.size() + " results");
			
		} catch (ExecutionException e) {
			logger.error("Initialize failed by internal error: ", e);
		}

	}

	private Map<Integer, Repo> generateRepoSet(String key) throws ClientProtocolException, IOException {
		List<Repo> repoList;
		Map<Integer, Repo> repos = new LinkedHashMap<>();
		logger.info("Calling github API");
		repoList = GithubSearch.search(key, null);
		for (Repo repo : repoList) {
			repos.put(repo.getId(), repo);
		}
		return repos;
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