package com.devsu.app.infraestructure.output.report;

import com.devsu.app.domain.model.AccountStatement;
import com.devsu.app.domain.model.CustomerStatement;
import com.devsu.app.domain.model.Movement;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
public class PdfReportService {

    private static final DeviceRgb COLOR_BLUE_DARK  = new DeviceRgb(26, 35, 126);
    private static final DeviceRgb COLOR_YELLOW     = new DeviceRgb(255, 193, 7);
    private static final DeviceRgb COLOR_WHITE      = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb COLOR_GRAY_LIGHT = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb COLOR_GRAY_TEXT  = new DeviceRgb(100, 100, 100);
    private static final DeviceRgb COLOR_DEBIT      = new DeviceRgb(198, 40, 40);
    private static final DeviceRgb COLOR_CREDIT     = new DeviceRgb(27, 94, 32);

    public Mono<byte[]> generatePdf(CustomerStatement statement) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(0, 0, 36, 0);

            addHeader(document, statement);
            addCustomerInfo(document, statement);
            addSummary(document, statement);

            statement.getAccounts().forEach(account ->
                    addAccountSection(document, account));

            addFooter(document, statement);
            document.close();
            return out.toByteArray();
        });
    }

    private void addHeader(Document document, CustomerStatement statement) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(COLOR_BLUE_DARK)
                .setMarginBottom(0);

        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(24);
        logoCell.add(new Paragraph("BANCO")
                .setFontColor(COLOR_YELLOW)
                .setFontSize(28)
                .setBold());
        logoCell.add(new Paragraph("Estado de Cuenta")
                .setFontColor(COLOR_WHITE)
                .setFontSize(12));

        Cell periodCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(24)
                .setTextAlignment(TextAlignment.RIGHT);
        periodCell.add(new Paragraph("PERÍODO")
                .setFontColor(COLOR_YELLOW)
                .setFontSize(9)
                .setBold());
        periodCell.add(new Paragraph(
                statement.getStartDate().toLocalDate() + " — " + statement.getEndDate().toLocalDate())
                .setFontColor(COLOR_WHITE)
                .setFontSize(11));

        header.addCell(logoCell);
        header.addCell(periodCell);
        document.add(header);
    }

    private void addCustomerInfo(Document document, CustomerStatement statement) {
        Table info = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(COLOR_GRAY_LIGHT)
                .setMarginBottom(0);

        addInfoCell(info, "CLIENTE", statement.getCustomerName());
        addInfoCell(info, "IDENTIFICACIÓN", statement.getCustomerIdentification());
        addInfoCell(info, "ID DE CLIENTE", statement.getClientId());

        document.add(info);
        document.add(new Paragraph(" ").setMargin(8));
    }

    private void addInfoCell(Table table, String label, String value) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(24)
                .setPaddingTop(12)
                .setPaddingBottom(12);
        cell.add(new Paragraph(label)
                .setFontColor(COLOR_GRAY_TEXT)
                .setFontSize(8)
                .setBold());
        cell.add(new Paragraph(value != null ? value : "-")
                .setFontSize(11)
                .setBold());
        table.addCell(cell);
    }

    private void addSummary(Document document, CustomerStatement statement) {
        document.add(new Paragraph("  Resumen del Período")
                .setFontSize(10)
                .setBold()
                .setFontColor(COLOR_GRAY_TEXT)
                .setMarginLeft(24));

        Table summary = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginLeft(24)
                .setMarginRight(24)
                .setMarginBottom(16);

        Cell debitCell = new Cell()
                .setBackgroundColor(new DeviceRgb(255, 235, 238))
                .setBorderLeft(new SolidBorder(COLOR_DEBIT, 4))
                .setBorderTop(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(Border.NO_BORDER)
                .setPadding(16)
                .setMarginRight(8);
        debitCell.add(new Paragraph("Total Débitos")
                .setFontColor(COLOR_DEBIT).setFontSize(9).setBold());
        debitCell.add(new Paragraph("$ " + statement.getGrandTotalDebits())
                .setFontColor(COLOR_DEBIT).setFontSize(18).setBold());

        Cell creditCell = new Cell()
                .setBackgroundColor(new DeviceRgb(232, 245, 233))
                .setBorderLeft(new SolidBorder(COLOR_CREDIT, 4))
                .setBorderTop(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(Border.NO_BORDER)
                .setPadding(16);
        creditCell.add(new Paragraph("Total Créditos")
                .setFontColor(COLOR_CREDIT).setFontSize(9).setBold());
        creditCell.add(new Paragraph("$ " + statement.getGrandTotalCredits())
                .setFontColor(COLOR_CREDIT).setFontSize(18).setBold());

        summary.addCell(debitCell);
        summary.addCell(creditCell);
        document.add(summary);
    }

    private void addAccountSection(Document document, AccountStatement account) {
        Table accountHeader = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginLeft(24)
                .setMarginRight(24)
                .setMarginBottom(0);

        Cell numberCell = new Cell()
                .setBackgroundColor(COLOR_BLUE_DARK)
                .setBorder(Border.NO_BORDER)
                .setPadding(10);
        numberCell.add(new Paragraph("N° " + account.getAccountNumber())
                .setFontColor(COLOR_WHITE).setFontSize(11).setBold());
        numberCell.add(new Paragraph(account.getAccountType())
                .setFontColor(COLOR_YELLOW).setFontSize(9));

        accountHeader.addCell(numberCell);
        addAccountSummaryCell(accountHeader, "Saldo Actual",
                "$ " + account.getCurrentBalance(), COLOR_BLUE_DARK);
        addAccountSummaryCell(accountHeader, "Débitos",
                "$ " + account.getTotalDebits(), COLOR_DEBIT);
        addAccountSummaryCell(accountHeader, "Créditos",
                "$ " + account.getTotalCredits(), COLOR_CREDIT);

        document.add(accountHeader);
        addMovementsTable(document, account);
        document.add(new Paragraph(" ").setMargin(12));
    }

    private void addAccountSummaryCell(Table table, String label, String value, DeviceRgb color) {
        Cell cell = new Cell()
                .setBackgroundColor(COLOR_GRAY_LIGHT)
                .setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(color, 2))
                .setPadding(10);
        cell.add(new Paragraph(label)
                .setFontColor(COLOR_GRAY_TEXT).setFontSize(8).setBold());
        cell.add(new Paragraph(value)
                .setFontColor(color).setFontSize(11).setBold());
        table.addCell(cell);
    }

    private void addMovementsTable(Document document, AccountStatement account) {
        if (account.getMovements() == null || account.getMovements().isEmpty()) {
            document.add(new Paragraph("  Sin movimientos en el período.")
                    .setFontColor(COLOR_GRAY_TEXT)
                    .setFontSize(9)
                    .setMarginLeft(24)
                    .setMarginBottom(8));
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1.5f, 1.5f, 1.5f, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginLeft(24)
                .setMarginRight(24);

        String[] headers = {"Fecha", "Tipo", "Valor", "Saldo", "Cuenta Referencia"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .setBackgroundColor(COLOR_BLUE_DARK)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(8)
                    .add(new Paragraph(h)
                            .setFontColor(COLOR_WHITE)
                            .setFontSize(9)
                            .setBold()));
        }

        boolean alternate = false;
        for (Movement m : account.getMovements()) {
            DeviceRgb rowColor = alternate ? COLOR_GRAY_LIGHT : COLOR_WHITE;
            DeviceRgb typeColor = "Debito".equals(m.getMovementType()) ? COLOR_DEBIT : COLOR_CREDIT;

            addMovementCell(table, m.getMovementDate().toString().replace("T", " ").substring(0, 16),
                    rowColor, COLOR_GRAY_TEXT);
            addMovementCell(table, m.getMovementType(), rowColor, typeColor);
            addMovementCell(table, "$ " + m.getValue(), rowColor, typeColor);
            addMovementCell(table, "$ " + m.getBalance(), rowColor, COLOR_BLUE_DARK);
            addMovementCell(table,
                    m.getReferenceAccountNumber() != null ? m.getReferenceAccountNumber() : "-",
                    rowColor, COLOR_GRAY_TEXT);

            alternate = !alternate;
        }

        document.add(table);
    }

    private void addMovementCell(Table table, String text, DeviceRgb bg, DeviceRgb fg) {
        table.addCell(new Cell()
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_GRAY_LIGHT, 0.5f))
                .setPadding(7)
                .add(new Paragraph(text)
                        .setFontColor(fg)
                        .setFontSize(8)));
    }

    private void addFooter(Document document, CustomerStatement statement) {
        document.add(new Paragraph(" ").setMargin(8));

        Table footer = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(COLOR_BLUE_DARK);

        Cell footerCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER);
        footerCell.add(new Paragraph("Este documento es un comprobante oficial de estado de cuenta.")
                .setFontColor(COLOR_WHITE).setFontSize(8));
        footerCell.add(new Paragraph("Generado el " + LocalDate.now() + "  •  BANCO S.A.")
                .setFontColor(COLOR_YELLOW).setFontSize(8));

        footer.addCell(footerCell);
        document.add(footer);
    }
}
