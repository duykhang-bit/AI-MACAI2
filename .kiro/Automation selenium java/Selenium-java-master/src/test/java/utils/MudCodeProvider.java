package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tự động lấy mã MUD tiếp theo mỗi lần run test.
 * Dùng file counter (mud-counter.txt) để nhớ đã dùng đến mã nào.
 * Mỗi lần gọi getNextMudCode() → trả mã tiếp theo + tăng counter.
 * Khi hết mã → quay lại từ đầu (circular).
 */
public class MudCodeProvider {

    private static final String COUNTER_FILE = "data/mud-counter.txt";
    private static final String DATA_FILE = "data/products.json";

    /**
     * Lấy mã MUD tiếp theo từ danh sách (auto-rotate).
     * @param mudKey key trong products.json (ví dụ: "mud" hoặc "mud2")
     * @return mã MUD chưa dùng
     */
    public static synchronized String getNextMudCode(String mudKey) {
        JsonArray codes = loadMudCodes(mudKey);
        int totalCodes = codes.size();

        // Đọc counter hiện tại từ file
        int currentIndex = readCounter(mudKey);

        // Lấy mã theo index (circular: nếu hết quay lại đầu)
        int safeIndex = currentIndex % totalCodes;
        String code = codes.get(safeIndex).getAsString();

        // Tăng counter và lưu lại
        writeCounter(mudKey, currentIndex + 1);

        System.out.println("[MudCodeProvider] " + mudKey + " → index=" + safeIndex + ", code=" + code);
        return code;
    }

    /**
     * Xem mã MUD hiện tại mà KHÔNG tăng counter (chỉ peek).
     */
    public static String peekCurrentMudCode(String mudKey) {
        JsonArray codes = loadMudCodes(mudKey);
        int currentIndex = readCounter(mudKey);
        int safeIndex = currentIndex % codes.size();
        return codes.get(safeIndex).getAsString();
    }

    private static JsonArray loadMudCodes(String mudKey) {
        try (InputStream is = MudCodeProvider.class.getClassLoader().getResourceAsStream(DATA_FILE);
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return root.getAsJsonObject(mudKey).getAsJsonArray("codes");
        } catch (Exception e) {
            throw new RuntimeException("Không đọc được file " + DATA_FILE + " key=" + mudKey, e);
        }
    }

    private static int readCounter(String mudKey) {
        // File counter riêng cho mỗi mudKey: data/mud-counter-{mudKey}.txt
        String counterFileName = "mud-counter-" + mudKey + ".txt";
        Path counterPath = getCounterPath(counterFileName);

        if (!Files.exists(counterPath)) {
            return 0;
        }

        try {
            String content = Files.readString(counterPath, StandardCharsets.UTF_8).trim();
            return Integer.parseInt(content);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void writeCounter(String mudKey, int value) {
        String counterFileName = "mud-counter-" + mudKey + ".txt";
        Path counterPath = getCounterPath(counterFileName);

        try {
            Files.createDirectories(counterPath.getParent());
            Files.writeString(counterPath, String.valueOf(value), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[MudCodeProvider] Không ghi được counter: " + e.getMessage());
        }
    }

    private static Path getCounterPath(String fileName) {
        // Lưu counter ở src/test/resources/data/mud-counters/ (KHÔNG bị mvn clean xóa)
        return Paths.get("src", "test", "resources", "data", "mud-counters", fileName);
    }

    /**
     * Reset counter về 0 (dùng khi muốn bắt đầu lại từ đầu).
     */
    public static void resetCounter(String mudKey) {
        writeCounter(mudKey, 0);
    }

    /**
     * Sau khi run xong: log các mã đã dùng rồi reset counter về 0.
     * Gọi từ TestListener.onFinish() để tự động clear sau mỗi lần chạy suite.
     *
     * @param mudKey key trong products.json (ví dụ: "mud", "mud2", "mud3")
     */
    public static void clearAfterRun(String mudKey) {
        try {
            JsonArray codes = loadMudCodes(mudKey);
            int counter = readCounter(mudKey);
            int total = codes.size();

            if (counter == 0) {
                System.out.println("[MudCodeProvider] " + mudKey + " → chưa dùng mã nào trong lần chạy này.");
                return;
            }

            // Log các mã đã dùng
            System.out.println("[MudCodeProvider] === CLEAR AFTER RUN: " + mudKey + " ===");
            System.out.println("[MudCodeProvider] Đã dùng " + counter + " lần, gồm các mã:");
            for (int i = 0; i < counter; i++) {
                int idx = i % total;
                System.out.println("  [" + (i + 1) + "] index=" + idx + " → " + codes.get(idx).getAsString());
            }
            System.out.println("[MudCodeProvider] → Reset counter về 0 để lần chạy sau bắt đầu từ đầu.");

            // Reset về 0
            resetCounter(mudKey);

        } catch (Exception e) {
            System.err.println("[MudCodeProvider] clearAfterRun lỗi: " + e.getMessage());
        }
    }

    /**
     * Clear tất cả mud keys sau khi run xong.
     * Keys được lấy tự động từ tên file counter trong thư mục mud-counters/.
     */
    public static void clearAllAfterRun() {
        Path dir = Paths.get("src", "test", "resources", "data", "mud-counters");
        if (!Files.exists(dir)) return;

        try {
            Files.list(dir)
                .filter(p -> p.getFileName().toString().startsWith("mud-counter-") && p.getFileName().toString().endsWith(".txt"))
                .forEach(p -> {
                    String fileName = p.getFileName().toString(); // mud-counter-mud2.txt
                    String mudKey = fileName.replace("mud-counter-", "").replace(".txt", ""); // mud2
                    clearAfterRun(mudKey);
                });
        } catch (Exception e) {
            System.err.println("[MudCodeProvider] clearAllAfterRun lỗi: " + e.getMessage());
        }
    }
}
