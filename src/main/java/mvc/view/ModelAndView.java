package mvc.view;

import lombok.Data;

import java.util.Map;

@Data
public class ModelAndView {
    String viewName;
    Map<String, Object> model;
}
