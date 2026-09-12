package utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Utility để auto-detect giá từ pageSource và ghi ra file price-snapshot.properties.
 * Dùng trong lần chạy đầu tiên để capture giá thực tế từ web.
 * Sau đó đọc file này để update giá cứng vào TC.
 */
public class PriceSnapshotWriter {

    private static final String SNAPSHOT_FILE = "src/test/resources/data/price-snapshot.properties";

    /**
     * Trích xuất các số tiền từ pageSource (format X,XXX,XXX hoặc X.XXX.XXX)
     * Lọc lấy các số có dạng tiền VND (>= 1000)
     */
    public static List<String> extractPrices(String pageSource) {
        List<String> prices = new ArrayList<>();
        // Match số tiền format: 1,234,567 hoặc 1.234.567 hoặc 1234567
        Pattern p = Pattern.compile("\\b(\\d{1,3}(?:[,.]\\d{3})+)\\b");
        Matcher m = p.matcher(pageSource);
        Set<String> seen = new LinkedHashSet<>();
        while (m.find()) {
            String val = m.group(1);
            // Normalize về dạng X,XXX,XXX
            String normalized = val.replace(".", ",");
            seen.add(normalized);
        }
        prices.addAll(seen);
        return prices;
    }

    /**
     * Ghi snapshot giá cho 1 TC vào file properties.
     * Format: TC1.tongTien=250,000
     *         TC1.giamGia=5,000
     *         TC1.tamTinh=245,000
     *         TC1.allPrices=250,000|5,000|245,000|...
     */
    public static void writeSnapshot(String tcKey, String tongTien, String giamGia, String tamTinh, String allPrices) {
        try {
            Path path = Paths.get(SNAPSHOT_FILE);
            // Đọc file hiện tại
            Properties props = new Properties();
            if (Files.exists(path)) {
                try (InputStream is = Files.newInputStream(path)) {
                    props.load(is);
                }
            }

            // Update các key của TC này
            if (tongTien != null && !tongTien.isEmpty()) props.setProperty(tcKey + ".tongTien", tongTien);
            if (giamGia != null && !giamGia.isEmpty())   props.setProperty(tcKey + ".giamGia", giamGia);
            if (tamTinh != null && !tamTinh.isEmpty())   props.setProperty(tcKey + ".tamTinh", tamTinh);
            if (allPrices != null && !allPrices.isEmpty()) props.setProperty(tcKey + ".allPrices", allPrices);
            props.setProperty(tcKey + ".capturedAt", new java.util.Date().toString());

            // Ghi lại
            Files.createDirectories(path.getParent());
            try (OutputStream os = Files.newOutputStream(path)) {
                props.store(os, "Price Snapshot - auto-captured from UAT web");
            }

            System.out.println("[PriceSnapshot] Saved " + tcKey + ": tongTien=" + tongTien
                    + ", giamGia=" + giamGia + ", tamTinh=" + tamTinh);
        } catch (Exception e) {
            System.err.println("[PriceSnapshot] Lỗi ghi snapshot: " + e.getMessage());
        }
    }

    /**
     * Auto-detect giá từ pageSource dựa trên label trên trang.
     * Tìm giá gần text "Tổng tiền", "Giảm giá", "Tạm tính" trong HTML.
     */
    public static String detectPriceNear(String pageSource, String... labels) {
        for (String label : labels) {
            // Tìm số tiền trong vòng 200 ký tự sau label
            int idx = pageSource.indexOf(label);
            if (idx < 0) continue;
            String chunk = pageSource.substring(idx, Math.min(idx + 300, pageSource.length()));
            Pattern p = Pattern.compile("(\\d{1,3}(?:[,.]\\d{3})+)");
            Matcher m = p.matcher(chunk);
            if (m.find()) {
                return m.group(1).replace(".", ",");
            }
        }
        return "";
    }

    /**
     * Lấy tất cả giá từ pageSource dưới dạng chuỗi pipe-separated
     */
    public static String allPricesToString(String pageSource) {
        List<String> prices = extractPrices(pageSource);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(prices.size(), 30); i++) {
            if (sb.length() > 0) sb.append("|");
            sb.append(prices.get(i));
        }
        return sb.toString();
    }
}
