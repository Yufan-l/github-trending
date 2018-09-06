package vertx.github.trending;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author yufan.liu
 *
 */

public class GithubSearch {

	public static List<Repo> search(String option, String language) throws ClientProtocolException, IOException {
		long time = System.currentTimeMillis();
		if (option.equals("one_day")) {
			return search(time - 24L * 60 * 60 * 1000, language);
		} else if (option.equals("one_week")) {
			return search(time - 7L * 24 * 60 * 60 * 1000, language);
		} else if (option.equals("one_month")) {
			return search(time - 30L * 24 * 60 * 60 * 1000, language);
		} else if (option.equals("one_year")) {
			return search(time - 365L * 24 * 60 * 60 * 1000, language);
		} else
			return null;
	}

	public static List<Repo> search(long timestamp, String language) throws ClientProtocolException, IOException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(timestamp);
		String dateString = df.format(date);
		String query ="";
		if(language == null)
			query = URLEncoder.encode("created:>" + dateString, "UTF-8");
		else
			query = URLEncoder.encode("created:>" + dateString + "+language:" + language, "UTF-8");
		String SAMPLE_URL = "https://api.github.com/search/repositories?q=" + query
				+ "&sort=forks&order=desc&per_page=10&page=1";

		HttpClient client = HttpClientBuilder.create().build();
		HttpUriRequest request = RequestBuilder.get().setUri(SAMPLE_URL)
				.setHeader(HttpHeaders.ACCEPT, "application/vnd.github.mercy-preview+jso").build();
		HttpResponse response = client.execute(request);

		String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
		// System.out.println(responseString);
		return parser(responseString);
	}

	public static List<Repo> parser(String response) {
		List<Repo> repos = new ArrayList<Repo>();
		JsonElement jelement = new JsonParser().parse(response);
		JsonObject jobject = jelement.getAsJsonObject();
		JsonArray arr = jobject.getAsJsonArray("items");
		for (JsonElement e : arr) {
			JsonObject repo = e.getAsJsonObject();
			repos.add(new Repo(repo));
		}
		return repos;
	}

}
