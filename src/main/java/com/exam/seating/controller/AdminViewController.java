package com.exam.seating.controller;

import com.exam.seating.dto.ViewSeatingDTO;
import com.exam.seating.pdf.HtmlPdfGenerator;
import com.exam.seating.service.PdfRenderService;
import com.exam.seating.service.ViewSeatingService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    private final ViewSeatingService viewSeatingService;
    private final PdfRenderService pdfRenderService;

    public AdminViewController(
            ViewSeatingService viewSeatingService,
            PdfRenderService pdfRenderService
    ) {
        this.viewSeatingService = viewSeatingService;
        this.pdfRenderService = pdfRenderService;
    }

    @GetMapping("/seating-preview")
    public String seatingPreview(
            @RequestParam Long examId,
            @RequestParam Long roomId,
            @RequestParam Integer year,
            @RequestParam Integer semester,
            Model model) {

        ViewSeatingDTO seating =
                viewSeatingService.getSeating(examId, roomId, year, semester);

        model.addAttribute("seating", seating);
        return "seating-preview";
    }

    @GetMapping("/seating/download")
    public void downloadSeatingPdf(
            @RequestParam Long examId,
            @RequestParam Long roomId,
            @RequestParam Integer year,
            @RequestParam Integer semester,
            HttpServletResponse response) throws Exception {

        ViewSeatingDTO seating =
                viewSeatingService.getSeating(examId, roomId, year, semester);

        String html = pdfRenderService.renderHtml(seating);

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=seating.pdf"
        );

        HtmlPdfGenerator.generate(html, response.getOutputStream());
    }
}