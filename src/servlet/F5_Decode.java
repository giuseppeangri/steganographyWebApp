package servlet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import stego_f5.F5Random;
import stego_f5.HuffmanDecode;
import stego_f5.Permutation;

/**
 * Servlet implementation class F5_Decode
 */
@MultipartConfig()
@WebServlet("/api/f5/decode")
public class F5_Decode extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static int[] coeff;
	private static byte[] deZigZag = {
       0,  1,  5,  6, 14, 15, 27, 28,
       2,  4,  7, 13, 16, 26, 29, 42,
       3,  8, 12, 17, 25, 30, 41, 43,
       9, 11, 18, 24, 31, 40, 44, 53,
      10, 19, 23, 32, 39, 45, 52, 54,
      20, 22, 33, 38, 46, 51, 55, 60,
      21, 34, 37, 47, 50, 56, 59, 61,
      35, 36, 48, 49, 57, 58, 62, 63
    };
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public F5_Decode() {
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
			
			// Get inputstream from part
			DataInputStream inputImage_stream = new DataInputStream(inputImage_part.getInputStream());
			
			// Input image as byte[]
			byte[] input_image = new byte[inputImage_partSize];
			
			// Convert inputstream to byte[]
			inputImage_stream.readFully(input_image);

		// INPUT PASSWORD
		
			String password = "abc123";	
		
		// DECODE
		
			HuffmanDecode hd = new HuffmanDecode(input_image);
			
			coeff = hd.decode();
			
			F5Random random = new F5Random(password.getBytes());
			
			Permutation permutation = new Permutation(coeff.length, random);
			
			int extractedByte = 0;
			int availableExtractedBits = 0;
			int extractedFileLength = 0;
			int nBytesExtracted = 0;
			int shuffledIndex = 0;
			int extractedBit;
			int i;
			
			// extract length information
		    for (i=0; availableExtractedBits<32; i++) {
				shuffledIndex = permutation.getShuffled(i);
				if (shuffledIndex%64 == 0) continue; // skip DC coefficients
				shuffledIndex = shuffledIndex-(shuffledIndex%64)+deZigZag[shuffledIndex%64];
                if (coeff[shuffledIndex] == 0) continue; // skip zeroes
				if (coeff[shuffledIndex] > 0)
				    extractedBit=coeff[shuffledIndex]&1;
				else
				    extractedBit=1-(coeff[shuffledIndex]&1);
				extractedFileLength |= extractedBit << availableExtractedBits++;
		    }
		    
		    // remove pseudo random pad
		    extractedFileLength ^= random.getNextByte();
		    extractedFileLength ^= random.getNextByte()<<8;
		    extractedFileLength ^= random.getNextByte()<<16;
		    extractedFileLength ^= random.getNextByte()<<24;
		    int k = extractedFileLength >> 24;
		    k %= 32;
		    int n = (1 << k)-1;
		    extractedFileLength &= 0x007fffff;
		    availableExtractedBits = 0;
		    if (n>0) {
				int startOfN = i;
				int hash;
extractingLoop:
				do {
					
				    // 1. read n places, and calculate k bits
				    hash = 0;
				    int code = 1;
				    for (i=0; code<=n; i++) {
						// check for pending end of coeff
						if (startOfN+i>=coeff.length) break extractingLoop;
						shuffledIndex = permutation.getShuffled(startOfN+i);
						if (shuffledIndex%64 == 0) continue; // skip DC coefficients
						shuffledIndex = shuffledIndex-(shuffledIndex%64)+deZigZag[shuffledIndex%64];
						if (coeff[shuffledIndex] == 0) continue; // skip zeroes
						if (coeff[shuffledIndex] > 0)
						    extractedBit=coeff[shuffledIndex]&1;
						else
						    extractedBit=1-(coeff[shuffledIndex]&1);
						if (extractedBit==1) {
						    hash ^= code;
						}
						code++;
				    }
				    startOfN += i;
				    
				    // 2. write k bits bytewise
				    for (i=0; i<k; i++) {
						extractedByte |= ((hash>>i)&1) << availableExtractedBits++;
						if (availableExtractedBits == 8) {
						    // remove pseudo random pad
						    extractedByte ^= random.getNextByte();
						    out.write((byte) extractedByte);
						    extractedByte=0;
						    availableExtractedBits=0;
						    nBytesExtracted++;
						    // check for pending end of embedded data
						    if (nBytesExtracted==extractedFileLength)
								break extractingLoop;
						}
				    }
				    
				} 
				while(true);
		    } 
		    else {
			    
				for (; i<coeff.length; i++) {
				    shuffledIndex = permutation.getShuffled(i);
				    if (shuffledIndex%64 == 0) continue; // skip DC coefficients
				    shuffledIndex = shuffledIndex-(shuffledIndex%64)+deZigZag[shuffledIndex%64];
				    if (coeff[shuffledIndex] == 0) continue; // skip zeroes
				    if (coeff[shuffledIndex] > 0)
						extractedBit=coeff[shuffledIndex]&1;
				    else
						extractedBit=1-(coeff[shuffledIndex]&1);
				    extractedByte |= extractedBit << availableExtractedBits++;
				    if (availableExtractedBits == 8) {
						// remove pseudo random pad
						extractedByte ^= random.getNextByte();
						out.write((byte) extractedByte);
						extractedByte=0;
						availableExtractedBits=0;
						nBytesExtracted++;
						if (nBytesExtracted==extractedFileLength)
						    break;
				    }
				}
				
		    }
			
		// CLOSE RESPONSE
		
			try {
				out.close();
			} catch(IOException e) {
				
			}
	
	}

}
