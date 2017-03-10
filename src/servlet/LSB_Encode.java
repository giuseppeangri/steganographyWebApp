package servlet;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
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

import stego_f5.Bmp;
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
		
		Image inputImage_image = null;
  				
		// SET RESPONSE TYPE
					
			response.setContentType("image/png");	
		
		// INPUT IMAGE
		
			// Get part
			Part inputImage_part = request.getPart("image");
			
			// Set response filename
			String inputImage_filename = inputImage_part.getSubmittedFileName();
			response.setHeader("Content-Disposition", "attachment;filename="+inputImage_filename.substring(0, inputImage_filename.lastIndexOf("."))+"_encoded_lsb.png");
			
			// Get part size
			int inputImage_partSize = (int) inputImage_part.getSize();
			
			// Get Content Type
			String inputImage_contentType = inputImage_part.getContentType();
			String inputImage_type = inputImage_contentType.substring(inputImage_contentType.lastIndexOf("/")+1);

			if(inputImage_type.equalsIgnoreCase("bmp")) {

//				BMP

				// Get inputstream from part
				BufferedInputStream inputImage_stream = new BufferedInputStream(inputImage_part.getInputStream());

				// Bmp encoder
				Bmp inputImage_bmp = new Bmp(inputImage_stream);

				// Obtain image
				inputImage_image = inputImage_bmp.getImage();

			}
			else if(inputImage_type.equalsIgnoreCase("jpeg")) {

//				JPEG

				// Get inputstream from part
				DataInputStream inputImage_stream = new DataInputStream(inputImage_part.getInputStream());

				// Input image as byte[]
				byte[] inputImage_byte = new byte[inputImage_partSize];

				// Convert inputstream to byte[]
				inputImage_stream.readFully(inputImage_byte);

				// Obtain an image icon
				ImageIcon inputImage_imageIcon = new ImageIcon(inputImage_byte);

				// Obtain image from image icon
				inputImage_image = inputImage_imageIcon.getImage(); // Get the image

			}
			else {
				response.setHeader("ERROR", "The input image is not supported.");
				response.sendError(500);
			}

		// INPUT FILE
		
			// Get part
			Part inputFile_part = request.getPart("embed");
			
			// Get inputstream from part
			InputStream inputFile_stream = inputFile_part.getInputStream();

			// Get string from inputstream

			String inputFile_string = streamToString(inputFile_stream);
		
		// INPUT KEY
			
			String key = request.getParameter("key");
			
		// ENCODE
				
			Image encodedImage_image_png = null;
			try {
				LSB lsb = new LSB(inputImage_image);
				encodedImage_image_png = lsb.encode(inputFile_string, key);
			} catch (Exception e1) {
				response.setHeader("ERROR", e1.getMessage());
				response.sendError(500);
			}
			
			JpegEncoder jpg;

			int quality = 100;
			
			String comment  = "Stegano Embedder";
			
			ByteArrayOutputStream encodedImage_stream = new ByteArrayOutputStream();
			
			jpg = new JpegEncoder(encodedImage_image_png, quality, encodedImage_stream, comment);
			
			jpg.Compress();
			
			encodedImage_stream.close();
			
			// Obtain an image icon
			ImageIcon encodedImage_imageIcon = new ImageIcon(encodedImage_stream.toByteArray());
			
			// Obtain image from image icon
			Image encodedImage_image_jpg = encodedImage_imageIcon.getImage(); // Get the image
												
		// PSNR
			
			PSNR psnr_png = new PSNR(encodedImage_image_png, inputImage_image);
			
			PSNR psnr_jpg = new PSNR(encodedImage_image_jpg, inputImage_image);
						
			response.setHeader("PNG_PSNR_255", String.valueOf(psnr_png.getPsnr_255()));
			
			response.setHeader("PNG_PSNR_PEAK", String.valueOf(psnr_png.getPsnr_peak()));
			
			response.setHeader("PNG_PEAK", String.valueOf(psnr_png.getPeak()));
			
			response.setHeader("PNG_MSE", String.valueOf(psnr_png.getMse()));
			
			response.setHeader("JPG_PSNR_255", String.valueOf(psnr_jpg.getPsnr_255()));
			
			response.setHeader("JPG_PSNR_PEAK", String.valueOf(psnr_jpg.getPsnr_peak()));
			
			response.setHeader("JPG_PEAK", String.valueOf(psnr_jpg.getPeak()));
			
			response.setHeader("JPG_MSE", String.valueOf(psnr_jpg.getMse()));
			
			response.setHeader("IMAGE_WIDTH", String.valueOf(psnr_png.getWidth()));
			
			response.setHeader("IMAGE_HEIGHT", String.valueOf(psnr_png.getHeight()));
			
		// CLOSE RESPONSE
			
			ImageIO.write(toBufferedImage(encodedImage_image_png), "png", out);
			
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
