package mybatis.mapper;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlMapperLoader {
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
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                String tag = element.getTagName();
                boolean select = switch (tag) {
                    case "select" -> true;
                    case "insert", "update", "delete" -> false;
                    default -> throw new IllegalStateException("不支持的节点：" + tag);
                };
                String id = element.getAttribute("id");
                if (id.isBlank()) {
                    throw new IllegalStateException(tag + " 缺少id");
                }
                if (statements.put(id, new XmlStatement(innerXml(element).trim(), select)) != null) {
                    throw new IllegalStateException("重复 id：" + id);
                }
            }
            return statements;
        } catch (Exception e) {
            throw new IllegalStateException("读取 " + mapperType.getName() + ".xml 失败", e);
        }
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
