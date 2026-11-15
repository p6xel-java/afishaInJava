package com.example.afisha.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class TicketQrService {

    public byte[] generateQrCode(String url) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 340, 340, Map.of(EncodeHintType.MARGIN, 2));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка генерации QR-кода", e);
        }
    }
}
