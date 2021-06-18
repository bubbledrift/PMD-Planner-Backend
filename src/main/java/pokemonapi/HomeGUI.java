package pokemonapi;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.HashMap;
import java.util.Map;

public class HomeGUI implements TemplateViewRoute {

  @Override
  public ModelAndView handle(Request request, Response response) throws Exception {
    Map<String,String> map = new HashMap<>();
    map.put("content", "Hello");
    map.put("title", "Pokemon Planner");
    return new ModelAndView(map, "main.ftl");
  }
}
