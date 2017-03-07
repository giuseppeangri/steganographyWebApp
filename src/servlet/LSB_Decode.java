package servlet;

import java.awt.Image;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import stego_lsb.LSB;

/**
 * Servlet implementation class LSB_Decode
 */
@MultipartConfig()
@WebServlet("/api/lsb/decode")
public class LSB_Decode extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LSB_Decode() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		OutputStream out = response.getOutputStream();
		
		// SET RESPONSE TYPE
					
			response.setContentType("text/plain");
		
		// INPUT IMAGE
		
			// Get part
			Part inputImage_part = request.getPart("image");
			
			// Set response filename
			String inputImage_filename = inputImage_part.getSubmittedFileName();
			response.setHeader("Content-Disposition", "attachment;filename="+inputImage_filename.substring(0, inputImage_filename.lastIndexOf("."))+"_decoded.txt");
			
			// Get part size
			int inputImage_partSize = (int) inputImage_part.getSize();
			
			Image inputImage_image = ImageIO.read(inputImage_part.getInputStream());

		// INPUT PASSWORD
		
			String password = "abc12345";	
		
		// DECODE
			
			LSB lsb = null;
			try {
				lsb = new LSB(inputImage_image);
			} catch (Exception e1) {
				response.setHeader("ERROR", e1.getMessage());
				response.sendError(500);
			}
			
			try {
				String embedText = lsb.decode(password);
				out.write(embedText.getBytes());
			} catch (Exception e1) {
				response.setHeader("ERROR", e1.getMessage());
				response.sendError(401);
			}
						
		// CLOSE RESPONSE
			
			try {
				out.close();
			} catch(IOException e) {
				
			}
	
	}

}
