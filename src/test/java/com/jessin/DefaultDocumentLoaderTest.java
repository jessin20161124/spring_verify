package com.jessin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author zexin.guo
 * @create 2018-07-21 下午11:34
 **/
public class DefaultDocumentLoaderTest {
    protected final Log logger = LogFactory.getLog(getClass());

    @Test
    public void test1() {
        DocumentLoader defaultDocumentLoader = new DefaultDocumentLoader();
        Resource resource = new ClassPathResource("/spring/app.xml");

        try {
            Document doc = defaultDocumentLoader.loadDocument(
                    new InputSource(resource.getInputStream()),
                    null,
                    new SimpleSaxErrorHandler(logger),
                    XmlValidationModeDetector.VALIDATION_XSD, false);
            Element root = doc.getDocumentElement();
            logger.info("根节点：" + root.getNodeName());
            searchElement(root);
        } catch (Exception e) {
            throw new RuntimeException("读取xml出错", e);
        }
    }

    private void searchElement(Element root) {
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                logger.info("子节点：" + ele.getNodeName());
                searchElement(ele);
            }
        }
    }
}
