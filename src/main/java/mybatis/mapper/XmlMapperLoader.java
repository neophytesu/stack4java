package mybatis.mapper;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlMapperLoader {
    private static final Pattern INCLUDE = Pattern.compile("<include refid=\"(\\w+)\"\\s*></include>");

    private static String resolveIncludes(String sql, Map<String, String> fragments) {
        return INCLUDE.matcher(sql).replaceAll(m -> {
            String id = m.group(1);
            String frag = fragments.get(id);
            if (frag == null) {
                throw new IllegalStateException("找不到 sql 片段：" + id);
            }
            return Matcher.quoteReplacement(frag);
        });
    }

    public static Map<String, XmlStatement> load(Class<?> mapperType) {
        try (InputStream in = mapperType.getResourceAsStream(mapperType.getSimpleName() + ".xml")) {
            if (in == null) {
                return Map.of();
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setExpandEntityReferences(false);
            Document doc = dbf.newDocumentBuilder().parse(in);
            Element root = doc.getDocumentElement();
            if (!"mapper".equals(root.getTagName())) {
                throw new IllegalStateException(mapperType + " 的 XML 根节点必须是 mapper");
            }
            String ns = root.getAttribute("namespace");
            if (!mapperType.getName().equals(ns)) {
                throw new IllegalStateException("namespace 必须是 " + mapperType.getName() + ", 实际是 " + ns);
            }
            Map<String, XmlStatement> statements = new LinkedHashMap<>();
            NodeList children = root.getChildNodes();
            Map<String, String> fragments = new LinkedHashMap<>();
            Map<String, Map<String, String>> resultMaps = new LinkedHashMap<>();
            for (int i = 0; i < children.getLength(); i++) {
                Element element = elementAt(children, i);
                if (element == null) {
                    continue;
                }
                String tag = element.getTagName();
                String id = element.getAttribute("id");
                if ("sql".equals(tag)) {
                    if (id.isBlank() || fragments.put(id, innerXml(element).trim()) != null) {
                        throw new IllegalStateException("sql 缺少 id 或重复：" + id);
                    }
                    continue;
                }
                if ("resultMap".equals(tag)) {
                    if (id.isBlank() || resultMaps.put(id, parseResultMap(element)) != null) {
                        throw new IllegalStateException("resultMap 缺少 id 或重复：" + id);
                    }
                }
            }
            for (int i = 0; i < children.getLength(); i++) {
                Element element = elementAt(children, i);
                if (element == null) {
                    continue;
                }
                String tag = element.getTagName();
                if ("sql".equals(tag) || "resultMap".equals(tag)) {
                    continue;
                }
                boolean select = switch (tag) {
                    case "select" -> true;
                    case "insert", "update", "delete" -> false;
                    default -> throw new IllegalStateException("不支持的节点：" + tag);
                };
                String id = element.getAttribute("id");
                if (id.isBlank()) {
                    throw new IllegalStateException(tag + " 缺少id");
                }
                String raw = resolveIncludes(innerXml(element).trim(), fragments);
                String resultMapId = element.getAttribute("resultMap");
                Map<String, String> columnByProperty = Map.of();
                if (!resultMapId.isBlank()) {
                    columnByProperty = resultMaps.get(resultMapId);
                    if (columnByProperty == null) {
                        throw new IllegalStateException("找不到 resultMap：" + resultMapId);
                    }
                }
                if (statements.put(id, new XmlStatement(raw, select, columnByProperty)) != null) {
                    throw new IllegalStateException("重复 id：" + id);
                }
            }
            return statements;
        } catch (Exception e) {
            throw new IllegalStateException("读取 " + mapperType.getName() + ".xml 失败", e);
        }
    }

    private static Element elementAt(NodeList children, int i) {
        Node node = children.item(i);
        return node.getNodeType() == Node.ELEMENT_NODE ? (Element) node : null;
    }

    private static Map<String, String> parseResultMap(Element resultMap) {
        Map<String, String> map = new LinkedHashMap<>();
        NodeList children = resultMap.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element el = (Element) node;
            if (!"result".equals(el.getTagName())) {
                throw new IllegalStateException("resultMap 里不支持：" + el.getTagName());
            }
            String property = el.getAttribute("property");
            String column = el.getAttribute("column");
            if (property.isBlank() || column.isBlank()) {
                throw new IllegalStateException("result 需要 property 和 column");
            }
            map.put(property, column);
        }
        return Map.copyOf(map);
    }

    private static String innerXml(Node node) {
        StringBuilder sb = new StringBuilder();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            sb.append(toXml(children.item(i)));
        }
        return sb.toString();
    }

    private static String toXml(Node node) {
        return switch (node.getNodeType()) {
            case Node.TEXT_NODE, Node.CDATA_SECTION_NODE -> node.getNodeValue();
            case Node.ELEMENT_NODE -> {
                Element element = (Element) node;
                StringBuilder sb = new StringBuilder();
                sb.append("<").append(element.getTagName());
                NamedNodeMap attributes = element.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node a = attributes.item(i);
                    sb.append(' ').append(a.getNodeName()).append("=\"").append(a.getNodeValue()).append("\"");
                }
                sb.append(">");
                NodeList children = element.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    sb.append(toXml(children.item(j)));
                }
                sb.append("</").append(element.getTagName()).append(">");
                yield sb.toString();
            }
            default -> "";
        };
    }
}
