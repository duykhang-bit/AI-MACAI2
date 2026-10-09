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

    private static final String UAT_DATA_FILE = "uatdata/products.json";
    private static final String PROD_DATA_FILE = "proddata/products.json";

    /**
     * Lấy mã MUD tiếp theo từ danh sách (auto-rotate).
     * @param mudKey key trong products.json (ví dụ: "mud01", "mud02", ..., "mud31" cho PROD)
     * @return mã MUD chưa dùng
     */
    public static synchronized String getNextMudCode(String mudKey) {
        return getNextMudCode(mudKey, false); // Default: UAT mode
    }

    /**
     * Lấy mã MUD tiếp theo với option chọn UAT hoặc PROD data.
     * @param mudKey key trong products.json (ví dụ: "mud01", "mud02", ..., "mud31")
     * @param useProdData true = dùng proddata/products.json, false = dùng uatdata/products.json
     * @return mã MUD chưa dùng
     */
    public static synchronized String getNextMudCode(String mudKey, boolean useProdData) {
        String dataFile = useProdData ? PROD_DATA_FILE : UAT_DATA_FILE;
        JsonArray codes = loadMudCodes(mudKey, dataFile);
        int totalCodes = codes.size();

        // Đọc counter hiện tại từ file
        int currentIndex = readCounter(mudKey, useProdData);

        // Lấy mã theo index (circular: nếu hết quay lại đầu)
        int safeIndex = currentIndex % totalCodes;
        String code = codes.get(safeIndex).getAsString();

        // Tăng counter và lưu lại
        writeCounter(mudKey, currentIndex + 1, useProdData);

        System.out.println("[MudCodeProvider] " + mudKey + " (" + (useProdData ? "PROD" : "UAT") + ") → index=" + safeIndex + ", code=" + code);
        return code;
    }

    /**
     * Xem mã MUD hiện tại mà KHÔNG tăng counter (chỉ peek).
     */
    public static String peekCurrentMudCode(String mudKey) {
        return peekCurrentMudCode(mudKey, false);
    }

    public static String peekCurrentMudCode(String mudKey, boolean useProdData) {
        String dataFile = useProdData ? PROD_DATA_FILE : UAT_DATA_FILE;
        JsonArray codes = loadMudCodes(mudKey, dataFile);
        int currentIndex = readCounter(mudKey, useProdData);
        int safeIndex = currentIndex % codes.size();
        return codes.get(safeIndex).getAsString();
    }

    private static JsonArray loadMudCodes(String mudKey, String dataFile) {
        try (InputStream is = MudCodeProvider.class.getClassLoader().getResourceAsStream(dataFile);
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return root.getAsJsonObject(mudKey).getAsJsonArray("codes");
        } catch (Exception e) {
            throw new RuntimeException("Không đọc được file " + dataFile + " key=" + mudKey, e);
        }
    }

    private static int readCounter(String mudKey, boolean useProdData) {
        // File counter riêng cho mỗi mudKey: data/mud-counter-{mudKey}.txt
        String counterFileName = "mud-counter-" + mudKey + ".txt";
        Path counterPath = getCounterPath(counterFileName, useProdData);

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

    private static void writeCounter(String mudKey, int value, boolean useProdData) {
        String counterFileName = "mud-counter-" + mudKey + ".txt";
        Path counterPath = getCounterPath(counterFileName, useProdData);

        try {
            Files.createDirectories(counterPath.getParent());
            Files.writeString(counterPath, String.valueOf(value), StandardCharsets.UTF_8);
            System.out.println("[MudCodeProvider] Counter " + mudKey + " → " + value + " (path: " + counterPath.toAbsolutePath() + ")");
        } catch (Exception e) {
            System.err.println("[MudCodeProvider] Không ghi được counter " + mudKey + " tại " + counterPath.toAbsolutePath() + ": " + e.getMessage());
        }
    }

    private static Path getCounterPath(String fileName, boolean useProdData) {
        String dataFolder = useProdData ? "proddata" : "uatdata";
        return Paths.get("src", "test", "resources", dataFolder, "mud-counters", fileName);
    }

    /**
     * Reset counter về 0 (dùng khi muốn bắt đầu lại từ đầu).
     * Mặc định dùng UAT data.
     */
    public static void resetCounter(String mudKey) {
        writeCounter(mudKey, 0, false); // default UAT
    }
    
    /**
     * Reset counter về 0 cho PROD hoặc UAT.
     */
    public static void resetCounter(String mudKey, boolean useProdData) {
        writeCounter(mudKey, 0, useProdData);
    }

    /**
     * Sau khi run xong: log các mã đã dùng. KHÔNG reset counter để lần sau tiếp tục từ chỗ đã dùng.
     * Gọi từ TestListener.onFinish() để tự động log sau mỗi lần chạy suite.
     * Mặc định dùng UAT data.
     *
     * @param mudKey key trong products.json (ví dụ: "mud", "mud2", "mud3")
     */
    public static void clearAfterRun(String mudKey) {
        clearAfterRun(mudKey, false); // default UAT
    }
    
    /**
     * Sau khi run xong: log các mã đã dùng cho PROD hoặc UAT.
     */
    public static void clearAfterRun(String mudKey, boolean useProdData) {
        try {
            String dataFile = useProdData ? PROD_DATA_FILE : UAT_DATA_FILE;
            JsonArray codes = loadMudCodes(mudKey, dataFile);
            int counter = readCounter(mudKey, useProdData);
            int total = codes.size();

            if (counter == 0) {
                System.out.println("[MudCodeProvider] " + mudKey + " → chưa dùng mã nào trong lần chạy này.");
                return;
            }

            // Log các mã đã dùng
            System.out.println("[MudCodeProvider] === LOG AFTER RUN: " + mudKey + " ===");
            System.out.println("[MudCodeProvider] Đã dùng " + counter + " lần tổng cộng, mã tiếp theo sẽ là index=" + (counter % total));
            System.out.println("[MudCodeProvider] → GIỮ NGUYÊN counter để lần sau dùng mã tiếp theo.");

        } catch (Exception e) {
            System.err.println("[MudCodeProvider] clearAfterRun lỗi: " + e.getMessage());
        }
    }

    /**
     * Clear tất cả mud keys sau khi run xong.
     * Keys được lấy tự động từ tên file counter trong thư mục mud-counters/.
     */
    public static void clearAllAfterRun() {
        // Scan cả uatdata và proddata mud-counters
        Path[] dirs = {
            Paths.get("src", "test", "resources", "uatdata", "mud-counters"),
            Paths.get("src", "test", "resources", "proddata", "mud-counters")
        };
        for (Path dir : dirs) {
            if (!Files.exists(dir)) continue;
            try {
                Files.list(dir)
                    .filter(p -> p.getFileName().toString().startsWith("mud-counter-") && p.getFileName().toString().endsWith(".txt"))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        String mudKey = fileName.replace("mud-counter-", "").replace(".txt", "");
                        clearAfterRun(mudKey);
                    });
            } catch (Exception e) {
                System.err.println("[MudCodeProvider] clearAllAfterRun lỗi: " + e.getMessage());
            }
        }
    }
}
