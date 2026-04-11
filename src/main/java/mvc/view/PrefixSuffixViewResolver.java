package mvc.view;

import spring.ioc.annotation.Bean;
import lombok.Data;
import mvc.view.interfaces.View;
import mvc.view.interfaces.ViewResolver;

@Data
@Bean
public class PrefixSuffixViewResolver implements ViewResolver {

    String prefix = "templates/";
    String suffix = ".html";

    @Override
    public View resolveViewName(String viewName) {
        if (viewName == null || viewName.isBlank()) {
            throw new IllegalArgumentException("viewName不能为空！");
        }
        if (viewName.startsWith("/")) {
            viewName = viewName.substring(1);
        }
        if (viewName.endsWith("/")) {
            viewName = viewName.substring(0, viewName.length() - 1);
        }
        return new ClasspathHtmlView(prefix + viewName + suffix);
    }
}
