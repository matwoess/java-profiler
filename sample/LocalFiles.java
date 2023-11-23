import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class LocalFiles {
  static final String MONEY_FORMAT = "€%.2f,-";

  public static void main(String[] args) {
    try {
      float total = getPriceTotalFromShoppingList(Path.of("..", "sample", "files", "shopping-list.xml"));
      writePriceTotalToFile(total, Path.of(args[0]));
    } catch (ParserConfigurationException | IOException | SAXException e) {
      throw new RuntimeException(e);
    }
  }

  public static float getPriceTotalFromShoppingList(Path xmlFilePath) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document document = builder.parse(xmlFilePath.toFile());
    Element list = document.getDocumentElement();
    NodeList items = list.getElementsByTagName("item");
    System.out.println("Items: ");
    float total = 0.f;
    for (int i = 0; i < items.getLength(); i++) {
      Element item = (Element) items.item(i);
      String name = item.getTextContent();
      int amount = Integer.parseInt(item.getAttribute("amount"));
      float price = Float.parseFloat(item.getAttribute("price"));
      System.out.printf("%d: %s (%d x %s)\n".formatted(i + 1, name, amount, formatPrice(price)));
      total += amount * price;
    }
    return total;
  }

  private static void writePriceTotalToFile(double total, Path filePath) throws IOException {
    System.out.printf("writing total amount %s to file '%s'%n\n", formatPrice(total), filePath);
    Files.writeString(filePath, String.valueOf(total));
  }

  private static String formatPrice(double price) {
    return MONEY_FORMAT.formatted(price);
  }
}