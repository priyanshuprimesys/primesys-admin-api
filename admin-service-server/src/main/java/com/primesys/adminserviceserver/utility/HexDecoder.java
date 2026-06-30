package com.primesys.adminserviceserver.utility;

import org.springframework.stereotype.Component;

@Component
public class HexDecoder {

    public static String getAsciiData(String hexString) {

        // Convert hex to bytes
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
        }

        // Convert bytes to string
        String ascii = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);

        // Remove null padding
        ascii = ascii.replace("\u0000", "");

        // System.out.println(ascii);
        return ascii;
    }
}
