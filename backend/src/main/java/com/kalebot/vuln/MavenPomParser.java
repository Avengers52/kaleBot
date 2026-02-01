package com.kalebot.vuln;

import com.kalebot.model.vuln.Dependency;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenPomParser {
  public List<Dependency> parse(String pomContent) {
    if (pomContent == null || pomContent.isBlank()) {
      return List.of();
    }
    try {
      Document document = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder()
          .parse(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
      document.getDocumentElement().normalize();
      Map<String, String> properties = readProperties(document);
      Map<String, String> managedVersions = readDependencyManagement(document, properties);
      NodeList dependencyNodes = document.getElementsByTagName("dependency");
      List<Dependency> dependencies = new ArrayList<>();
      for (int i = 0; i < dependencyNodes.getLength(); i++) {
        Node node = dependencyNodes.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        Element element = (Element) node;
        if (!isDirectDependency(element)) {
          continue;
        }
        String groupId = resolveProperties(getChildText(element, "groupId"), properties);
        String artifactId = resolveProperties(getChildText(element, "artifactId"), properties);
        String version = resolveProperties(getChildText(element, "version"), properties);
        if ((version == null || version.isBlank()) && groupId != null && artifactId != null) {
          version = managedVersions.get(groupId + ":" + artifactId);
        }
        if (groupId != null && artifactId != null) {
          dependencies.add(new Dependency(groupId, artifactId, version, buildPurl(groupId, artifactId, version)));
        }
      }
      return dependencies;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to parse Maven POM", e);
    }
  }

  private boolean isDirectDependency(Element element) {
    Node parent = element.getParentNode();
    if (parent == null) {
      return false;
    }
    Node grandParent = parent.getParentNode();
    if (grandParent == null) {
      return true;
    }
    return !"dependencyManagement".equalsIgnoreCase(grandParent.getNodeName());
  }

  private Map<String, String> readProperties(Document document) {
    Map<String, String> properties = new HashMap<>();
    NodeList propertiesNodes = document.getElementsByTagName("properties");
    if (propertiesNodes.getLength() == 0) {
      return properties;
    }
    Node propertiesNode = propertiesNodes.item(0);
    NodeList children = propertiesNode.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        properties.put(node.getNodeName(), node.getTextContent().trim());
      }
    }
    return properties;
  }

  private Map<String, String> readDependencyManagement(Document document, Map<String, String> properties) {
    Map<String, String> managed = new HashMap<>();
    NodeList dependencyManagementNodes = document.getElementsByTagName("dependencyManagement");
    if (dependencyManagementNodes.getLength() == 0) {
      return managed;
    }
    NodeList dependencyNodes = ((Element) dependencyManagementNodes.item(0)).getElementsByTagName("dependency");
    for (int i = 0; i < dependencyNodes.getLength(); i++) {
      Node node = dependencyNodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element element = (Element) node;
      String groupId = resolveProperties(getChildText(element, "groupId"), properties);
      String artifactId = resolveProperties(getChildText(element, "artifactId"), properties);
      String version = resolveProperties(getChildText(element, "version"), properties);
      if (groupId != null && artifactId != null && version != null) {
        managed.put(groupId + ":" + artifactId, version);
      }
    }
    return managed;
  }

  private String getChildText(Element element, String tagName) {
    NodeList nodes = element.getElementsByTagName(tagName);
    if (nodes.getLength() == 0) {
      return null;
    }
    return nodes.item(0).getTextContent().trim();
  }

  private String resolveProperties(String value, Map<String, String> properties) {
    if (value == null) {
      return null;
    }
    String resolved = value;
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      resolved = resolved.replace("${" + entry.getKey() + "}", entry.getValue());
    }
    return resolved;
  }

  private String buildPurl(String groupId, String artifactId, String version) {
    if (version == null || version.isBlank()) {
      return "pkg:maven/" + groupId + "/" + artifactId;
    }
    return "pkg:maven/" + groupId + "/" + artifactId + "@" + version;
  }
}
