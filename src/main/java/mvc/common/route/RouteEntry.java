package mvc.common.route;

import lombok.Data;
import mvc.handler.Handler;

import java.util.*;

@Data
public class RouteEntry {

    String method;
    List<Segment> patternSegments;
    Handler handler;

    public Optional<Map<String, String>> tryMatch(String path) {
        List<String> segments = Arrays.stream(path.split("/")).filter(s -> !s.isBlank()).toList();
        if (patternSegments.size() != segments.size()) {
            return Optional.empty();
        }
        Map<String, String> pathVariables = new HashMap<>();
        for (int i = 0; i < segments.size(); i++) {
            Segment p = patternSegments.get(i);
            String seg = segments.get(i);
            if (p.getType().equals(SegType.VARIABLE)) {
                pathVariables.put(p.getName(), seg);
                continue;
            }
            if (!p.getName().equals(seg)) {
                return Optional.empty();
            }
        }
        return Optional.of(pathVariables);
    }
}
