package stego_lsb;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;

public class LSB {
	
	int a,r,g,b,t,i,j;
	int h,w,size=0;
	int pixels[],red[],green[],blue[],alpha[],buf[];
	Image img;
	ColorModel cm;
	String key;
	Runtime runtime=Runtime.getRuntime();
	
	public LSB(Image input_image) throws Exception {
		
		this.img = input_image;
		this.h = this.img.getHeight(null);
		this.w = this.img.getWidth(null);
		
		pixels = new int[w*h];
		alpha = new int[w*h];
		red = new int[w*h];
		green = new int[w*h];
		blue = new int[w*h];

		try {
			PixelGrabber pg = new PixelGrabber(img,0,0,w,h,true);

			if(pg.grabPixels()==true) {
				
				cm = pg.getColorModel();

				size = 0; 
				pixels = (int[])pg.getPixels();
				for(j=0; j<h; j++) {
					
					for (i=0; i<w; i++) {
						alpha[size] = ((pixels[j * w + i] >> 24) & 0xff);
						red  [size] = ((pixels[j * w + i] >> 16) & 0xff);
						green[size] = ((pixels[j * w + i] >>  8) & 0xff);
						blue [size] = ((pixels[j * w + i]      ) & 0xff);
						size++;
					}         
					
				}
				
			}
		}
		catch(Exception e) {
			throw new Exception("Processing Image Error");
		}
				
	}

	public Image encode(String text, String key) throws Exception {

		int[] textArray = getBuffer(text);

		this.key = key;
		
		this.buf = textArray;

		if(buf.length>red.length) {
			throw new Exception("Cannot Encode: No Enough Space To Store Data.");
		}
		else {
		
			for (i=0; i<8; i++) {
				buf[i+8] = key.charAt(i);
			}              

			for(i=0; i<buf.length; i++) {
				
				t = buf[i];
				r = t%8;
				t = t/8;
				g = t%7;
				b = t/7;

				red[i] = r+(red  [i]-red  [i]%8);
				green[i] = g+(green[i]-green[i]%8);
				blue[i] = b+(blue [i]-blue [i]%8);
				
			}

		}
		
		size = 0;

		for(j=0; j<h; j++) {	
			
			for (i=0; i<w; i++) {
				a = (alpha[size] << 24) & 0xff000000;
				r = (red  [size] << 16) & 0x00ff0000;
				g = (green[size] << 8)  & 0x0000ff00;
				b = (blue [size])       & 0x000000ff;
				pixels[j * w + i] = a | r | g | b;
				size++;            
			}         

		}
		
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, w, h, pixels, 0, w);	

		return image; 

	}

	public int[] getBuffer(String text) {
		
		int len = text.length()+16;

		int x,y,z;

		x = len/10000;
		y = len/100- x*100;
		z = len-(x*10000+y*100);

		buf = new int[len+16];
		buf[0] = x;
		buf[1] = y;
		buf[2] = z;

		for(int i=0; i< text.length(); i++) {
			buf[i+16] = text.charAt(i);
		}
		
		return buf; 
		
	}

	public String decode(String key) throws Exception {

		char str;
		String embedKey = new String();
		int len;
		int ch;
		int x[] = new int[3];

		for (i=0; i<3; i++) {
			r = red  [i]-(red  [i] - red  [i]%8);
			g = green[i]-(green[i] - green[i]%8);
			b = blue [i]-(blue [i] - blue [i]%8);
			x[i] = (((b*7)+g)*8)+r;
		}
		
		len = x[0]*10000+x[1]*100+x[2];
		
		char text_chars[] = new char[len-16];
		
		for (i=8; i<16; i++) {
			r = red  [i]-(red  [i] - red  [i]%8);
			g = green[i]-(green[i] - green[i]%8);
			b = blue [i]-(blue [i] - blue [i]%8);
			ch = (((b*7)+g)*8)+r;
			str = (char)ch;
			embedKey = embedKey+str;
		}
		
		if (key.equalsIgnoreCase(embedKey)) {  
			
			for(i=16; i<len; i++) {

				r = red  [i]-(red  [i]-red  [i]%8);
				g = green[i]-(green[i]-green[i]%8);
				b = blue [i]-(blue [i]-blue [i]%8);

				ch = (((b*7)+g)*8)+r;
				text_chars[i-16] = (char)ch;
			}
			
			String text = new String(text_chars);
			
			return text;
			
		}
		else {
			throw new Exception("Invalid Password.");
		}

	}

}