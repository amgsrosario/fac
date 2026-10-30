package com.ar2lda.fac.testinfra;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.cos.COSName;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.Decoder;
import com.google.zxing.qrcode.detector.Detector;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;

/** Independent inspection of actual PDF image transforms and decoded QR streams. */
public final class PdfQrInspection extends PDFGraphicsStreamEngine {
    public record Qr(double widthMm,double heightMm,double activeWidthMm,double minBorderMm,
                     double topFromPageTop,String payload,int version,String ecLevel,int byteSegments) { }
    private final List<Qr> codes=new ArrayList<>();
    private PdfQrInspection(PDPage page) { super(page); }
    public static List<Qr> inspect(PDPage page)throws IOException {
        var inspector=new PdfQrInspection(page);inspector.processPage(page);return List.copyOf(inspector.codes);
    }
    @Override public void drawImage(PDImage image)throws IOException {
        try {
            var bitmap=new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image.getImage())));
            var black=bitmap.getBlackMatrix();var detected=new Detector(black).detect();
            var decoded=new Decoder().decode(detected.getBits());
            var transform=getGraphicsState().getCurrentTransformationMatrix();
            double width=transform.getScalingFactorX()*25.4/72.0,height=transform.getScalingFactorY()*25.4/72.0;
            int[] rect=black.getEnclosingRectangle();
            double border=Math.min(Math.min(rect[0],image.getWidth()-rect[0]-rect[2])*width/image.getWidth(),
                    Math.min(rect[1],image.getHeight()-rect[1]-rect[3])*height/image.getHeight());
            var origin=transform.transformPoint(0,0);var opposite=transform.transformPoint(1,1);
            double top=getPage().getMediaBox().getHeight()-Math.max(origin.y,opposite.y);
            codes.add(new Qr(width,height,rect[2]*width/image.getWidth(),border,top,decoded.getText(),
                    (detected.getBits().getWidth()-17)/4,decoded.getECLevel(),decoded.getByteSegments()==null?0:decoded.getByteSegments().size()));
        } catch(ReaderException ignored) { /* Other images, e.g. company logo, are not QR codes. */ }
    }
    @Override public void appendRectangle(Point2D a,Point2D b,Point2D c,Point2D d) { }
    @Override public void clip(int rule) { }
    @Override public void moveTo(float x,float y) { }
    @Override public void lineTo(float x,float y) { }
    @Override public void curveTo(float a,float b,float c,float d,float e,float f) { }
    @Override public Point2D getCurrentPoint() { return new Point2D.Float(); }
    @Override public void closePath() { }
    @Override public void endPath() { }
    @Override public void strokePath() { }
    @Override public void fillPath(int rule) { }
    @Override public void fillAndStrokePath(int rule) { }
    @Override public void shadingFill(COSName name) { }
}
