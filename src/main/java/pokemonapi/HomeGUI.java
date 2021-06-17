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
    Map<String,String> animals = new HashMap<>();
    animals.put("content", "THIS IS THE STUFF ON THE PAGE.");
    animals.put("title", "TESTING");
    return new ModelAndView(animals, "main.ftl");
  }
}
