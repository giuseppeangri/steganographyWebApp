package servlet;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
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
import stego_lsb.LSB;
import stego_psnr.PSNR;

/**
 * Servlet implementation class LSB_Encode
 */
@MultipartConfig()
@WebServlet("/api/lsb/encode")
public class LSB_Encode extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LSB_Encode() {
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
			response.setHeader("Content-Disposition", "attachment;filename="+inputImage_filename.substring(0, inputImage_filename.lastIndexOf("."))+"_encoded_lsb.jpg");
			
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

			// Get string from inputstream

			String inputFile_string = streamToString(inputFile_stream);
		
		// INPUT PASSWORD
			
			String password = "abc12345";
			
		// ENCODE
			
			LSB lsb = new LSB(inputImage_image);
			
			Image encodedImage_imageLsb = lsb.encode(inputFile_string, password);
			
			ByteArrayOutputStream encodedImage_stream = new ByteArrayOutputStream();
			
			jpg = new JpegEncoder(encodedImage_imageLsb, quality, encodedImage_stream, comment);
			
			jpg.Compress();
			
			encodedImage_stream.close();
			
			// Obtain an image icon
			ImageIcon encodedImage_imageIcon = new ImageIcon(encodedImage_stream.toByteArray());
			
			// Obtain image from image icon
			Image encodedImage_image = encodedImage_imageIcon.getImage(); // Get the image
			
		// PSNR
			
			PSNR psnr = new PSNR(encodedImage_image, inputImage_image);
						
			float psnr_value = psnr.getPSNR();
			
		// CLOSE RESPONSE
			
			ImageIO.write(toBufferedImage(encodedImage_image), "jpg", out);

			try {
				out.close();
			} catch(IOException e) {
				
			}
		
	}
	
	private String streamToString(InputStream inputStream) {

		try {

			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}

			return result.toString("UTF-8");

		}
		catch(IOException e) {

		}

		return null;

	}
	
	public static BufferedImage toBufferedImage(Image img) {

		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;

	}

}
