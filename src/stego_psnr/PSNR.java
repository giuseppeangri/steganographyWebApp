package stego_psnr;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.IOException;

import javax.swing.ImageIcon;

public class PSNR {
	
	private Image image01;
	private Image image02;
	
	private int width;
	private int height;
	
	public PSNR(Image image01, Image image02) {
		
		this.image01 = image01;
		this.image02 = image02;
		
		ImageIcon icon = new ImageIcon(image02);
		
		this.width = icon.getIconWidth();
        this.height = icon.getIconHeight();
        
	}
	
	public Image getImage01() {
		return image01;
	}

	public void setImage01(Image image01) {
		this.image01 = image01;
	}

	public Image getImage02() {
		return image02;
	}

	public void setImage02(Image image02) {
		this.image02 = image02;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getPSNR() throws IOException {
		
		int[][] image01_pixels = this.conver2Pixels(image01);
		int[][] image02_pixels = this.conver2Pixels(image02);
		
        float psnr_255, psnr_peak;
        float mse = 0;
        double peak = 0;
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
            	
                mse += Math.pow(image01_pixels[i][j] - image02_pixels[i][j], 2);
                
                if(peak < image01_pixels[i][j]) {
                	peak = image01_pixels[i][j];
                }
            }
        }
        
        mse = mse / (width * height);
        System.out.println("MSE:" + mse);
        
        psnr_255 = (float) (10 * (Math.log10(Math.pow(255, 2) / mse)));
        System.out.println("PSNR 255:" + psnr_255);
        
        psnr_peak = (float) (10 * (Math.log10(Math.pow(peak, 2) / mse)));
        System.out.println("PSNR Peak :" + psnr_peak);
        
        return psnr_peak;
        
    }
	
	private int[][] conver2Pixels(Image image) throws IOException {
		
        int[] pixel = new int[width * height];
        int[][] pixelMap = new int[width][height];
        int count = 0;
                
        PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixel, 0, width);
        try { pg.grabPixels();
        } catch (Exception e) { System.err.println(e);}
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int grayLevel = (int) ((0.299 * ((pixel[count] >> 16) & 0xff)) + (0.587 * ((pixel[count] >> 8) & 0xff)) + (0.114 * (pixel[count] & 0xff)));
                pixelMap[i][j] = grayLevel;
                count++;
            }
        }
        
        return  pixelMap;
    }
	
}
