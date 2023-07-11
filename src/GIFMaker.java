import org.w3c.dom.Node;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;

public class GIFMaker implements AutoCloseable {
    public static final String NONE = "none", DO_NOT_DISPOSE = "doNotDispose", RESTORE_TO_BACKGROUND_COLOR = "restoreToBackgroundColor", RESTORE_TO_PREVIOUS = "restoreToPrevious";

    private final ImageWriter w;
    private final ImageWriteParam imageWriteParam;
    private final IIOMetadata metadata;

    public GIFMaker(final ImageOutputStream os, final int imageType, final int timeBetweenFramesMS, final boolean loopContinuously, final boolean transparent, final String disposalMethod) throws IOException {
        final Iterator<ImageWriter> wl = ImageIO.getImageWritersBySuffix("gif");
        w = wl.hasNext() ? wl.next() : null;
        if (w == null) throw new IOException("No GIF Image Writers Exist!");
        imageWriteParam = w.getDefaultWriteParam();
        final ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
        metadata = w.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);
        final String metaFormatName = metadata.getNativeMetadataFormatName();
        final IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);
        final IIOMetadataNode gce = get(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", disposalMethod);
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", transparent ? "TRUE" : "FALSE");
        gce.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10));
        gce.setAttribute("transparentColorIndex", "0");
        get(root, "CommentExtensions").setAttribute("CommentExtension", "Test Comment");
        IIOMetadataNode appEN = get(root, "ApplicationExtensions");
        final IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");
        final int loop = loopContinuously ? 0 : 1;
        child.setUserObject(new byte[] { 0x1, (byte) (loop & 0xFF), (byte) ((loop >> 8) & 0xFF) });
        appEN.appendChild(child);
        metadata.setFromTree(metaFormatName, root);
        w.setOutput(os);
        w.prepareWriteSequence(null);
    }

    public void write(RenderedImage img) throws IOException { w.writeToSequence(new IIOImage(img, null, metadata), imageWriteParam); }

    @Override
    public void close() throws IOException { w.endWriteSequence(); }

    public static IIOMetadataNode get(final IIOMetadataNode r, final String name) {
        final int n = r.getLength();
        for (int i = 0; i < n; i++) {
            final Node no = r.item(i);
            if (no.getNodeName().compareToIgnoreCase(name) == 0)
                return (IIOMetadataNode) no;
        }
        final IIOMetadataNode no = new IIOMetadataNode(name);
        r.appendChild(no);
        return no;
    }
}
