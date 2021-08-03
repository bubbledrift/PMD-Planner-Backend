package pokemonapi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import freemarker.template.Configuration;

public final class Main {

  private static final int DEFAULT_PORT = 4567;

  public static void main(String[] args) throws Exception {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() throws Exception {
    runSparkServer(DEFAULT_PORT);
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private static void runSparkServer(int port) {
    Spark.port(getHerokuAssignedPort());
    Spark.externalStaticFileLocation("src/main/resources/static");

    Spark.options("/*", (request, response) -> {
      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");

      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));


    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();

    // Setup Spark Routes
    Spark.post("/updatePopularity", new UpdatePopularityHandler());
    Spark.get("/getPopularity", new GetPopularityHandler());
    Spark.get("/", new HomeGUI(), freeMarker);
  }

  /**
   * Updates the popularity of pokemon by getting which pokemon were added to a user's team.
   * Request's body should include a list of pokemon used in a team.
   */
  public static class UpdatePopularityHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {

      JSONObject data = new JSONObject(request.body());
      JSONArray toIncrement = data.getJSONArray("toIncrement");

      Class.forName("org.sqlite.JDBC");
      String urlToDB = "jdbc:sqlite:data/RTDXPopularity.sqlite3";
      Connection conn = DriverManager.getConnection(urlToDB);

      Map<String, Object> variables = ImmutableMap.of("success", "success");
      Gson gson = new Gson();


      //If the length is longer than 10, remove as it could be a bot.
      if (toIncrement.length() > 10) {
        return gson.toJson(variables);
      }

      //For each pokemon, go into the database and increment it's uses count.
      for (int i = 0; i < toIncrement.length(); i++) {
        String PokeNumber = toIncrement.getString(i);
        System.out.println(PokeNumber);
        PreparedStatement prepStatement = conn.prepareStatement(
            "UPDATE Popularity SET Popularity = Popularity + 1 WHERE Number=\"" + PokeNumber + "\";"
        );
        prepStatement.executeUpdate();
        prepStatement.close();
      }


      return gson.toJson(variables);
    }
  }

  /**
   * Gets a list of the popular/most used pokemon.
   */
  public static class GetPopularityHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {

      Class.forName("org.sqlite.JDBC");
      String urlToDB = "jdbc:sqlite:data/RTDXPopularity.sqlite3";
      Connection conn = DriverManager.getConnection(urlToDB);

      //Gets pokemon ordered by how many times they've been used in a team.
      PreparedStatement prepStatement = conn.prepareStatement(
          "SELECT Number FROM Popularity ORDER BY Popularity DESC"
      );
      ResultSet rs = prepStatement.executeQuery();

      Map byPopularity = new HashMap<String, Object>();
      int i = 1;

      while (rs.next()) {

        byPopularity.put(rs.getString(1), i);

        i++;
      }
      rs.close();
      prepStatement.close();

      Map<String, Object> variables = ImmutableMap.of("byPopular", byPopularity);

      Gson gson = new Gson();
      return gson.toJson(variables);
    }
  }

  /**
   * Display an error page when an exception occurs in the server.
   *
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  /**
   * Needed to get the heroku assigned port.
   * @return The heroku assigned port
   */
  static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
  }

}