/**
 * Example for a viewhandler implementation contributed by Leonardo F. Cardoso.
 */
package br.nom.leonardo.viewhandler;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.viewhandler.ViewHandler;

/**
 * Uses iText to implement a "stamp" with userid
 * 
 * @author Leonardo F. Cardoso
 */
public class PDFViewHandler implements ViewHandler {

	@Override
	public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req,
			HttpServletResponse resp) {

		try {

			resp.setContentType("application/pdf");

			String userid = req.getSession().getAttribute("userid").toString().trim().toUpperCase();

			PdfReader reader = new PdfReader(filePath);
			int numeroPaginasNoPDF = reader.getNumberOfPages();

			PdfStamper stamp = new PdfStamper(reader, resp.getOutputStream());
			int indice = 1;
			PdfContentByte over;

			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

			PdfGState gstate = new PdfGState();
			gstate.setFillOpacity(0.3f);
			gstate.setStrokeOpacity(0.3f);

			while (indice <= numeroPaginasNoPDF) {

				over = stamp.getOverContent(indice);

				over.saveState();
				over.setGState(gstate);

				over.beginText();

				over.setFontAndSize(bf, 150);

				float alturaPagina = reader.getPageSize(indice).getHeight();
				float larguraPagina = reader.getPageSize(indice).getWidth();
				double rotacao = Math.toDegrees(Math.atan(alturaPagina / larguraPagina));

				over.showTextAligned(PdfContentByte.ALIGN_CENTER, userid.toUpperCase(), larguraPagina / 2,
						alturaPagina / 2, (float) rotacao);

				over.endText();

				over.restoreState();

				indice++;
			}

			stamp.close();

		} catch (Exception e) {
			// TODO exception handling
			e.printStackTrace();
		}

	}

	@Override
	public void processZipContent(String fileName, InputStream zipIn, ViewHandlerConfig viewHandlerConfig,
			HttpServletRequest req, HttpServletResponse resp) {
	}

	@Override
	public boolean supportsZipContent() {
		return false;
	}

}
