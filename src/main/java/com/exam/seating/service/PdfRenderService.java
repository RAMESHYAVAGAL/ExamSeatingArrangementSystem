package com.exam.seating.service;

import com.exam.seating.dto.ViewSeatingDTO;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class PdfRenderService {

    private final SpringTemplateEngine templateEngine;

    public PdfRenderService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderHtml(ViewSeatingDTO seating) {

        Context context = new Context();
        context.setVariable("seating", seating);

        return templateEngine.process("seating-pdf", context);
    }
}
