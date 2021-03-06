package mkl.testarea.pdfbox2.form;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CreateImageButton
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/39958843/how-to-import-an-icon-to-a-button-field-in-a-pdf-using-pdfbox">
     * How to import an icon to a button field in a PDF using PDFBox?
     * </a>
     * <p>
     * This test shows how one can create a button with an image.
     * </p>
     */
    @Test
    public void testCreateSimpleImageButton() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("2x2colored.png");
                PDDocument document = new PDDocument()  )
        {
            BufferedImage bufferedImage = ImageIO.read(resource);
            PDImageXObject pdImageXObject = LosslessFactory.createFromImage(document, bufferedImage);
            float width = 10 * pdImageXObject.getWidth();
            float height = 10 * pdImageXObject.getHeight();

            PDAppearanceStream pdAppearanceStream = new PDAppearanceStream(document);
            pdAppearanceStream.setResources(new PDResources());
            try (PDPageContentStream pdPageContentStream = new PDPageContentStream(document, pdAppearanceStream))
            {
                pdPageContentStream.drawImage(pdImageXObject, 0, 0, width, height);
            }
            pdAppearanceStream.setBBox(new PDRectangle(width, height));

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            PDPushButton pdPushButton = new PDPushButton(acroForm);
            pdPushButton.setPartialName("ImageButton");
            List<PDAnnotationWidget> widgets = pdPushButton.getWidgets();
            for (PDAnnotationWidget pdAnnotationWidget : widgets)
            {
                pdAnnotationWidget.setRectangle(new PDRectangle(50, 750, width, height));
                pdAnnotationWidget.setPage(page);
                page.getAnnotations().add(pdAnnotationWidget);

                PDAppearanceDictionary pdAppearanceDictionary = pdAnnotationWidget.getAppearance();
                if (pdAppearanceDictionary == null)
                {
                    pdAppearanceDictionary = new PDAppearanceDictionary();
                    pdAnnotationWidget.setAppearance(pdAppearanceDictionary);
                }

                pdAppearanceDictionary.setNormalAppearance(pdAppearanceStream);
            }

            acroForm.getFields().add(pdPushButton);

            document.save(new File(RESULT_FOLDER, "imageButton.pdf"));
        }
    }
}
