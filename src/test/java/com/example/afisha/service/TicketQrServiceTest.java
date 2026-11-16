package com.example.afisha.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class TicketQrServiceTest {

    @Test
    void generatesPngQrCodeContainingOriginalUrl() throws Exception {
        String url = "https://example.test/ticket.html?token=some-test-token";
        byte[] png = new TicketQrService().generateQrCode(url);
        assertTrue(png.length > 100);
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 'P', png[1]);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(340, image.getWidth());
        assertEquals(340, image.getHeight());
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        assertEquals(url, new MultiFormatReader().decode(bitmap).getText());
    }

}