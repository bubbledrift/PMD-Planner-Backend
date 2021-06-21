package pokemonapi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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

//  private static Database db;
//  static {
//    try {
//      db = new Database();
//    } catch (SQLException | ClassNotFoundException throwables) {
//      throwables.printStackTrace();
//    }
//  }

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
    Spark.post("/checkPopularity", new CheckPopularityHandler());
    Spark.get("/", new HomeGUI(), freeMarker);
  }

  /**
   * Updates the popularity of pokemon by getting which pokemon were added to a user's team.
   * Request's body should include a list of pokemon to used in a team.
   */
  public static class UpdatePopularityHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {

      JSONObject data = new JSONObject(request.body());
      JSONArray toIncrement = data.getJSONArray("toIncrement");

      Class.forName("org.sqlite.JDBC");
      String urlToDB = "jdbc:sqlite:data/Gen3/Emerald/EmeraldPopularityTest.sqlite3";
      Connection conn = DriverManager.getConnection(urlToDB);

      //For each pokemon, go into the database and increment it's uses count.
      for (int i = 0; i < toIncrement.length(); i++) {
        String pokemon = toIncrement.getString(i);
        PreparedStatement prepStatement = conn.prepareStatement(
            "UPDATE EmeraldPopularity SET Uses = Uses + 1 WHERE Name=\"" + pokemon + "\";"
        );
        prepStatement.executeUpdate();
        prepStatement.close();
      }

      Map<String, Object> variables = ImmutableMap.of("success", "success");

      Gson gson = new Gson();
      return gson.toJson(variables);
    }
  }

  /**
   * Gets a list of the top 100 most popular/most used pokemon.
   * Request's body should be empty.
   */
  public static class CheckPopularityHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {

      Class.forName("org.sqlite.JDBC");
      String urlToDB = "jdbc:sqlite:data/Gen3/Emerald/EmeraldPopularityTest.sqlite3";
      Connection conn = DriverManager.getConnection(urlToDB);

      //Gets pokemon ordered by how many times they've been used in a team.
      PreparedStatement prepStatement = conn.prepareStatement(
          "SELECT Name FROM EmeraldPopularity ORDER BY Uses DESC LIMIT 100"
      );
      ResultSet rs = prepStatement.executeQuery();

      ArrayList byPopularity = new ArrayList<String>();
      while (rs.next()) {
        byPopularity.add(rs.getString(1));
      }
      rs.close();
      prepStatement.close();

      Map<String, Object> variables = ImmutableMap.of("byPopular", byPopularity);

      Gson gson = new Gson();
      return gson.toJson(variables);
    }
  }

//  /**
//   * Adds a number to the database's count and returns the count.
//   */
//  public static class TestHandler implements Route {
//    @Override
//    public Object handle(Request request, Response response) throws Exception {
//
//      JSONObject data = new JSONObject(request.body());
//      String text = data.getString("text");
//
//      Map<String, Object> variables = ImmutableMap.of("reply", text + " tested");
//
//      Gson gson = new Gson();
//      return gson.toJson(variables);
//    }
//  }

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