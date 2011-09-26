package gov.nasa.pds.imaging.generate.automatic.elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class Md5Checksum implements Element {

    private File file;

    public Md5Checksum() { }

    private byte[] createChecksum() {
        InputStream fis = null;
        MessageDigest complete = null;
        try {
            fis = new FileInputStream(this.file);

            final byte[] buffer = new byte[1024];
            complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return complete.digest();
    }

    @Override
    public String getUnits() {
        return null;
    }

    @Override
    public String getValue() {
        final byte[] b = createChecksum();
        String checksum = "";
        for (int i = 0; i < b.length; i++) {
            checksum += Integer.toString((b[i] & 0xff) + 0x100, 16)
                    .substring(1);
        }
        return checksum;
    }

    @Override
    public void setParameters(final String filePath) {
        this.file = new File(filePath);
    }
}