package com.badu.services.emails;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.jboss.logging.Logger;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PdfReceiptService {

  private static final Logger LOG = Logger.getLogger(PdfReceiptService.class);

  private static final Pattern IMG_PATTERN = Pattern.compile("<img src=\"image:([^\"]+).* />");
  private static final String IMAGE_PREFIX = "data:image/png;base64,";
  private static final String IMAGE_BASE_PATH = "/images/";
  private static final String EMPTY_STRING = "";
  private static final String IMG_TAG = "<img src=\"%s%s\" />";

  public <T> Uni<byte[]> generatePdfReceipt(String receiptType, T data) {
    TemplateInstance template = ReceiptTemplates.resolveTemplate(receiptType, data);
    return Uni.createFrom().completionStage(template.renderAsync())
        .onItem().transformToUni(receiptHtml -> {

          try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            String baseUri = PdfReceiptService.class.getResource("/").toExternalForm();
            String htmlContent = processImageData(receiptHtml);
            builder.withHtmlContent(htmlContent, baseUri);
            builder.toStream(baos);
            builder.run();

            byte[] pdfdata = baos.toByteArray();
            return Uni.createFrom().item(pdfdata);
          } catch (Exception e) {
            return Uni.createFrom().failure(e);
          }
        });
  }

  private String processImageData(String html) {
    StringBuilder sb = new StringBuilder();

    int lastIndex = 0;
    Matcher matcher = IMG_PATTERN.matcher(html);
    while (matcher.find()) {
      sb.append(html.substring(lastIndex, matcher.start()));
      sb.append(generateImageData(matcher.group(1)));
      lastIndex = matcher.end();
    }

    sb.append(html.substring(lastIndex));
    return sb.toString();
  }

  private String generateImageData(String imagePath) {
    try {
      BufferedImage image = ImageIO.read(getClass().getResource(IMAGE_BASE_PATH + imagePath));

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "png", outputStream);
      byte[] imageBytes = outputStream.toByteArray();

      String base64String = Base64.getEncoder().encodeToString(imageBytes);
      return String.format(IMG_TAG, IMAGE_PREFIX, base64String);
    } catch (IOException e) {
      LOG.error(e);
    }

    return EMPTY_STRING;
  }
}
