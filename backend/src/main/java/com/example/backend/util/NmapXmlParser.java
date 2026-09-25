package com.example.backend.util;

import com.example.backend.dto.parse.PortInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NmapXmlParser {
    public List<PortInfo> parse(String xmlContent) {
        List<PortInfo> ports = new ArrayList<>();
        try {
            // Trim để bỏ DOCTYPE gây lỗi với một số Java XML parser
            String cleanXml = xmlContent.trim();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Tắt xử lý DOCTYPE để tránh lỗi bảo mật XXE
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                    new ByteArrayInputStream(cleanXml.getBytes(StandardCharsets.UTF_8))
            );
            NodeList portNodes = doc.getElementsByTagName("port");
            for (int i = 0; i < portNodes.getLength(); i++) {
                Element portEl = (Element) portNodes.item(i);
                int portId = Integer.parseInt(portEl.getAttribute("portid"));
                String protocol = portEl.getAttribute("protocol");
                // Lấy state
                NodeList stateNodes = portEl.getElementsByTagName("state");
                String state = "unknown";
                if (stateNodes.getLength() > 0) {
                    state = ((Element) stateNodes.item(0)).getAttribute("state");
                }
                // Lấy tên service
                NodeList serviceNodes = portEl.getElementsByTagName("service");
                String serviceName = "unknown";
                if (serviceNodes.getLength() > 0) {
                    serviceName = ((Element) serviceNodes.item(0)).getAttribute("name");
                }
                ports.add(new PortInfo(portId, protocol, state, serviceName));
            }
        } catch (Exception e) {
            log.error("Lỗi parse Nmap XML: {}", e.getMessage());
        }
        return ports;
    }
}
