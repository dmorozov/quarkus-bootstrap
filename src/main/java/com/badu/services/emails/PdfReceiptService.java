package com.badu.services.emails;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.badu.dto.emails.UpdateAccessEmailData;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PdfReceiptService {

  public <T> Uni<byte[]> generatePdfReceipt(String receiptType, T data) {
    MailTemplateInstance template = resolveTemplate(receiptType, data);
    return Uni.createFrom().completionStage(template.templateInstance().renderAsync())
        .onItem().transformToUni(receiptHtml -> {

          try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            String baseUri = PdfReceiptService.class.getResource("/").toExternalForm();
            builder.withHtmlContent(receiptHtml, baseUri);
            builder.toStream(baos);
            builder.run();

            byte[] pdfdata = baos.toByteArray();
            return Uni.createFrom().item(pdfdata);
          } catch (Exception e) {
            return Uni.createFrom().failure(e);
          }
        });
  }

  private <T> MailTemplateInstance resolveTemplate(String receiptType, T data) {
    return switch (receiptType) {
      case "chatAccessUpdated" -> CustomerReceiptTemplates.chatAccessUpdated((UpdateAccessEmailData) data);
      default -> throw new IllegalArgumentException("Unknown receipt type: " + receiptType);
    };
  }

  private void generateImageData() {
    try {
      File imageFile = new File("path/to/your/image.png");
      BufferedImage image = ImageIO.read(imageFile);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "png", outputStream);
      byte[] imageBytes = outputStream.toByteArray();

      String base64String = Base64.getEncoder().encodeToString(imageBytes);
      String dataUri = "data:image/png;base64," + base64String;

      System.out.println(dataUri);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
