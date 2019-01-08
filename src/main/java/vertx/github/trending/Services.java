package vertx.github.trending;

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

import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public class Services {
	
	private Map<Integer, Repo> repos = new LinkedHashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(Services.class);

	LoadingCache<String, Map<Integer, Repo>> cache;
	
	
	@Operation(summary = "Refresh all repos", method = "GET", operationId = "api/repos",
			    tags = {
			      "Repo"
			    },
			    responses = {
			      @ApiResponse(responseCode = "200", description = "OK",
			        content = @Content(
			          mediaType = "application/json",
			          encoding = @Encoding(contentType = "application/json"),
			          schema = @Schema(name = "repos",
			            implementation = Repo.class)
			        )
			      ),
			      @ApiResponse(responseCode = "500", description = "Internal Server Error.")
			    }
			)
	public void refresh(RoutingContext routingContext) {
		String timePeriod = routingContext.request().getParam("timePeriod");
		//String language = routingContext.request().getParam("language");

		try {
			repos = cache.get(timePeriod);
			routingContext.response().setStatusCode(200).end(Json.encodePrettily(repos.values()));
			logger.info("Refresh service succeed, return " + repos.size() + " results");
		} catch (ExecutionException e) {
			routingContext.response().setStatusCode(500).end("fail");
			logger.error("internal error: ", e);
		}

	}
	
	public void initialize(int cachedExpire) {
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
	
	
	public Map<Integer, Repo> generateRepoSet(String key) throws ClientProtocolException, IOException {
		List<Repo> repoList;
		Map<Integer, Repo> repos = new LinkedHashMap<>();
		logger.info("Calling github API");
		repoList = GithubSearch.search(key, null);
		for (Repo repo : repoList) {
			repos.put(repo.getId(), repo);
		}
		return repos;
	}
	
	
	@Operation(summary = "Get all repos", method = "GET", operationId = "api/refresh/:timePeriod",
		    tags = {
		      "Repo"
		    },
		    responses = {
		      @ApiResponse(responseCode = "200", description = "OK",
		        content = @Content(
		          mediaType = "application/json",
		          encoding = @Encoding(contentType = "application/json"),
		          schema = @Schema(name = "repos",
		            implementation = Repo.class)
		        )
		      ),
		      @ApiResponse(responseCode = "500", description = "Internal Server Error.")
		    }
		)
	public void getAll(RoutingContext routingContext) {
		// Write the HTTP response
		// The response is in JSON using the utf-8 encoding
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encodePrettily(repos.values()));
	}

}
