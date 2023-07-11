import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

public class test {
    public static final RenderingHints RH = new RenderingHints(new HashMap<RenderingHints.Key, Object>() {{
        put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }});

    public static void main(String[] args) {
        try {
            final File
                    handGIF = new File("hand.gif"),
                    avatar = new File("avatar.png"),
                    out = new File("test.gif");

            final boolean x = true, cx = true;
            final int size = 256, force = size / 2, offset = force / 2;
            final float[] animation = new float[] { -.05f, .1f, .2f, .19f, .1f };



            final ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            final BufferedImage[] frames;
            try (final ImageInputStream ciis = ImageIO.createImageInputStream(handGIF)) {
                reader.setInput(ciis, false);

                frames = new BufferedImage[reader.getNumImages(true)];

                for (int i = 0; i < frames.length; i++)
                    frames[i] = reader.read(i);
            }

            final BufferedImage ava = ImageIO.read(avatar);

            if (out.exists())
                out.delete();
            try (
                    final ImageOutputStream o = new FileImageOutputStream(out);
                    final GIFMaker g = new GIFMaker(o, BufferedImage.TYPE_INT_ARGB, 50, true, true, GIFMaker.RESTORE_TO_BACKGROUND_COLOR)
            ) {
                for (int i = 0; i < frames.length; i++) {
                    final BufferedImage frame = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    final Graphics2D gr = (Graphics2D) frame.getGraphics();
                    gr.setRenderingHints(RH);
                    final int c1 = Math.round(animation[i] * force), c2 = x ? Math.round((.2f - animation[i]) * force) : 0;
                    gr.drawImage(ava, cx ? c2 / 2 : 0, offset + c1, size - c2, size - offset - c1, null);
                    gr.drawImage(frames[i], 0, 0, size, size, null);
                    gr.dispose();
                    g.write(frame);
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}