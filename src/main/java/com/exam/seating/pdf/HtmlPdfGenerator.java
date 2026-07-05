package com.exam.seating.pdf;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.OutputStream;

public class HtmlPdfGenerator {

    public static void generate(String html, OutputStream out) throws Exception {

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out, false);

        renderer.finishPDF();
    }
}
