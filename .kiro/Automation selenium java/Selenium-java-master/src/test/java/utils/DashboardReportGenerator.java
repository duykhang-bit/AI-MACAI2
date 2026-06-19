package utils;

import org.testng.*;
import org.testng.xml.XmlSuite;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Custom Dashboard Report — hiện pie chart + bảng chi tiết trên cùng 1 trang.
 * Tự động chạy sau khi test suite hoàn tất.
 */
public class DashboardReportGenerator implements IReporter {

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        int passed = 0, failed = 0, skipped = 0;
        List<TestResult> results = new ArrayList<>();

        for (ISuite suite : suites) {
            for (ISuiteResult sr : suite.getResults().values()) {
                ITestContext ctx = sr.getTestContext();

                for (ITestResult r : ctx.getPassedTests().getAllResults()) {
                    passed++;
                    results.add(new TestResult("Passed", r));
                }
                for (ITestResult r : ctx.getFailedTests().getAllResults()) {
                    failed++;
                    results.add(new TestResult("Failed", r));
                }
                for (ITestResult r : ctx.getSkippedTests().getAllResults()) {
                    skipped++;
                    results.add(new TestResult("Skipped", r));
                }
            }
        }

        // Sort by start time
        results.sort((a, b) -> Long.compare(a.startTime, b.startTime));

        String html = buildHtml(passed, failed, skipped, results);
        try {
            File file = new File(outputDirectory + "/Dashboard.html");
            FileWriter fw = new FileWriter(file);
            fw.write(html);
            fw.close();
            System.out.println("📊 Dashboard Report: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildHtml(int passed, int failed, int skipped, List<TestResult> results) {
        int total = passed + failed + skipped;
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        StringBuilder rows = new StringBuilder();
        for (TestResult r : results) {
            String color = r.status.equals("Passed") ? "#28a745" : r.status.equals("Failed") ? "#dc3545" : "#ffc107";
            String duration = String.format("%.2f", r.duration / 1000.0);
            String error = r.errorMsg != null ? r.errorMsg.replace("<", "&lt;").replace(">", "&gt;") : "";
            if (error.length() > 150) error = error.substring(0, 150) + "...";

            rows.append("<tr>")
                .append("<td style='color:").append(color).append(";font-weight:bold'>").append(r.status).append("</td>")
                .append("<td>").append(r.className).append("::").append(r.methodName).append("</td>")
                .append("<td>").append(duration).append("s</td>")
                .append("<td style='font-size:12px;color:#666'>").append(error).append("</td>")
                .append("</tr>\n");
        }

        return "<!DOCTYPE html>\n<html><head><meta charset='UTF-8'>\n"
            + "<title>Test Dashboard</title>\n"
            + "<style>\n"
            + "body{font-family:'Segoe UI',sans-serif;margin:0;padding:20px;background:#f5f5f5}\n"
            + "h1{text-align:center;color:#333;margin-bottom:5px}\n"
            + ".subtitle{text-align:center;color:#666;margin-bottom:30px}\n"
            + ".summary{display:flex;justify-content:center;gap:30px;margin-bottom:30px;flex-wrap:wrap}\n"
            + ".card{background:#fff;border-radius:12px;padding:20px 30px;box-shadow:0 2px 8px rgba(0,0,0,0.1);text-align:center;min-width:120px}\n"
            + ".card .num{font-size:36px;font-weight:bold}\n"
            + ".card .label{font-size:14px;color:#666;margin-top:5px}\n"
            + ".green{color:#28a745} .red{color:#dc3545} .yellow{color:#ffc107} .blue{color:#007bff}\n"
            + ".chart-container{text-align:center;margin-bottom:30px}\n"
            + "table{width:100%;border-collapse:collapse;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1)}\n"
            + "th{background:#343a40;color:#fff;padding:12px 15px;text-align:left}\n"
            + "td{padding:10px 15px;border-bottom:1px solid #eee}\n"
            + "tr:hover{background:#f8f9fa}\n"
            + ".pass-bar{height:24px;border-radius:12px;background:#e9ecef;overflow:hidden;max-width:400px;margin:0 auto 30px}\n"
            + ".pass-bar-fill{height:100%;border-radius:12px;background:linear-gradient(90deg,#28a745,#20c997)}\n"
            + ".footer{text-align:center;color:#999;margin-top:20px;font-size:12px}\n"
            + "</style></head><body>\n"
            + "<h1>🧪 Selenium Test Dashboard</h1>\n"
            + "<p class='subtitle'>Generated: " + timestamp + "</p>\n"
            + "<div class='summary'>\n"
            + "  <div class='card'><div class='num blue'>" + total + "</div><div class='label'>Total</div></div>\n"
            + "  <div class='card'><div class='num green'>" + passed + "</div><div class='label'>Passed</div></div>\n"
            + "  <div class='card'><div class='num red'>" + failed + "</div><div class='label'>Failed</div></div>\n"
            + "  <div class='card'><div class='num yellow'>" + skipped + "</div><div class='label'>Skipped</div></div>\n"
            + "  <div class='card'><div class='num' style='color:#17a2b8'>" + String.format("%.1f%%", passRate) + "</div><div class='label'>Pass Rate</div></div>\n"
            + "</div>\n"
            + "<div class='pass-bar'><div class='pass-bar-fill' style='width:" + String.format("%.1f", passRate) + "%'></div></div>\n"
            + "<table>\n"
            + "<tr><th>Result</th><th>Test</th><th>Duration</th><th>Error</th></tr>\n"
            + rows.toString()
            + "</table>\n"
            + "<p class='footer'>Generated by Selenium Automation Framework | " + timestamp + "</p>\n"
            + "</body></html>";
    }

    private static class TestResult {
        String status, className, methodName, errorMsg;
        long startTime, duration;

        TestResult(String status, ITestResult r) {
            this.status = status;
            this.className = r.getTestClass().getName();
            this.methodName = r.getMethod().getMethodName();
            this.startTime = r.getStartMillis();
            this.duration = r.getEndMillis() - r.getStartMillis();
            this.errorMsg = r.getThrowable() != null ? r.getThrowable().getMessage() : null;
        }
    }
}
