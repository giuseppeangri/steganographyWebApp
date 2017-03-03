package servlet;


import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.swing.ImageIcon;

import stego_f5.JpegEncoder;
import stego_psnr.PSNR;

/**
 * Servlet implementation class F5_Encode
 */
@MultipartConfig()
@WebServlet("/api/f5/encode")
public class F5_Encode extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public F5_Encode() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		OutputStream out = response.getOutputStream();
		  
		JpegEncoder jpg;

		int quality = 100;
		
		String comment  = "Stegano Embedder";
		
		// SET RESPONSE TYPE
					
			response.setContentType("image/jpg");	
		
		// INPUT IMAGE
		
			// Get part
			Part inputImage_part = request.getPart("image");
			
			// Set response filename
			String inputImage_filename = inputImage_part.getSubmittedFileName();
			response.setHeader("Content-Disposition", "attachment;filename="+inputImage_filename.substring(0, inputImage_filename.lastIndexOf("."))+"_encoded_f5.jpg");
			
			// Get part size
			int inputImage_partSize = (int) inputImage_part.getSize();
			
			// Get inputstream from part
			DataInputStream inputImage_stream = new DataInputStream(inputImage_part.getInputStream());
			
			// Input image as byte[]
			byte[] inputImage_byte = new byte[inputImage_partSize];
			
			// Convert inputstream to byte[]
			inputImage_stream.readFully(inputImage_byte);
			
			// Obtain an image icon
			ImageIcon inputImage_imageIcon = new ImageIcon(inputImage_byte);
						
			// Obtain image from image icon
			Image inputImage_image = inputImage_imageIcon.getImage(); // Get the image
			
		// INPUT FILE
		
			// Get part
			Part inputFile_part = request.getPart("embed");
			
			// Get inputstream from part
			InputStream inputFile_stream = inputFile_part.getInputStream();

		// INPUT PASSWORD
		
			String password = "abc123";	
		
		// ENCODE
			
			ByteArrayOutputStream encodedImage_stream = new ByteArrayOutputStream();
			
			jpg = new JpegEncoder(inputImage_image, quality, encodedImage_stream, comment);
			
			jpg.Compress(inputFile_stream, password);
			
			encodedImage_stream.close();
			
			// Obtain an image icon
			ImageIcon encodedImage_imageIcon = new ImageIcon(encodedImage_stream.toByteArray());
			
			// Obtain image from image icon
			Image encodedImage_image = encodedImage_imageIcon.getImage(); // Get the image
			
		// PSNR
			
			PSNR psnr = new PSNR(encodedImage_image, inputImage_image);
			
			float psnr_value = psnr.getPSNR();
						
		// CLOSE RESPONSE
			
			out.write(encodedImage_stream.toByteArray());
		
			try {
				out.close();
			} catch(IOException e) {
				
			}
		
	}

}
