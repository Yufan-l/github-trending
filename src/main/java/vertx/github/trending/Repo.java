package vertx.github.trending;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import api.Required;

public class Repo {

	@Required
	@JsonProperty(value = "id", required = true)
	private Integer id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("url")
	private String url;
	@JsonProperty("stars")
	private Integer stars;
	@JsonProperty("forks")
	private Integer forks;
	@JsonProperty("language")
	private String language;
	@JsonProperty("description")
	private String description;

	public Repo(String name, String url, int id) {
		this.name = name;
		this.url = url;
		this.id = id;
	}

	public Repo(JsonObject json) {
		this.name = getJsonString(json, "name");
		this.url = getJsonString(json, "html_url");
		this.id = getJsonInt(json, "id");
		this.stars = getJsonInt(json, "stargazers_count");
		this.language = getJsonString(json, "language");
		this.forks = getJsonInt(json, "forks");
		this.description = getJsonString(json, "description");
	}

	public Repo() {
		this.id = -1;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getForks() {
		return forks;
	}

	public void setForks(int forks) {
		this.forks = forks;
	}

	private String getJsonString(JsonObject json, String key) {
		JsonElement e = json.get(key);
		if (!e.isJsonNull())
			return e.getAsString();
		else
			return null;
	}

	private Integer getJsonInt(JsonObject json, String key) {
		JsonElement e = json.get(key);
		if (!e.isJsonNull())
			return e.getAsInt();
		else
			return null;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setStars(Integer stars) {
		this.stars = stars;
	}

	public void setForks(Integer forks) {
		this.forks = forks;
	}
}