package utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Gọi API generate MUD voucher code trên UAT internal network.
 *
 * API endpoint:
 *   POST http://uat-offline-swagger.lc.frt.local/lc-voucher-core/api/voucher/generate
 *
 * Request body (JSON array):
 *   [{ "code": "<voucherCode>", "voucherType": 2, "quantity": 1, "phoneNumber": "<phone>" }]
 *
 * Response: JSON array chứa các object có field "code" hoặc "voucherCode".
 *
 * Dùng trong TC25 (KM-0926-280 — test mud5):
 *   String mudCode = MudApiGenerator.generateMudCode("3725", "0835089255");
 */
public class MudApiGenerator {

    private static final String API_URL =
            "http://uat-offline-swagger.lc.frt.local/lc-voucher-core/api/voucher/generate";
    private static final int TIMEOUT_MS = 15_000; // 15 giây

    /**
     * Generate 1 MUD code từ API.
     *
     * @param voucherCode mã ưu đãi trong CTKM (ví dụ: "3725")
     * @param phoneNumber SĐT khách hàng test (ví dụ: "0835089255")
     * @return mã MUD vừa được gen (ví dụ: "ABCD1234XY")
     * @throws RuntimeException nếu API lỗi hoặc không parse được response
     */
    public static String generateMudCode(String voucherCode, String phoneNumber) {
        String[] codes = generateMudCodes(voucherCode, phoneNumber, 1);
        if (codes == null || codes.length == 0) {
            throw new RuntimeException("[MudApiGenerator] API trả về danh sách mã rỗng!");
        }
        System.out.println("[MudApiGenerator] Generated MUD code: " + codes[0]
                + " | voucherCode=" + voucherCode + " | phone=" + phoneNumber);
        return codes[0];
    }

    /**
     * Generate nhiều MUD codes từ API cùng lúc (dùng khi muốn nạp bulk vào products.json).
     *
     * @param voucherCode mã ưu đãi
     * @param phoneNumber SĐT khách hàng
     * @param quantity    số lượng mã cần gen
     * @return mảng các mã MUD
     */
    public static String[] generateMudCodes(String voucherCode, String phoneNumber, int quantity) {
        String requestBody = buildRequestBody(voucherCode, quantity, phoneNumber);

        try {
            String responseBody = postJson(API_URL, requestBody);
            System.out.println("[MudApiGenerator] Response: " + responseBody);
            return parseCodes(responseBody);
        } catch (IOException e) {
            throw new RuntimeException("[MudApiGenerator] Lỗi khi gọi API: " + e.getMessage(), e);
        }
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    /** Build JSON request body theo đúng format API yêu cầu. */
    private static String buildRequestBody(String code, int quantity, String phoneNumber) {
        return "[\n"
                + "  {\n"
                + "    \"code\": \"" + code + "\",\n"
                + "    \"voucherType\": 2,\n"
                + "    \"quantity\": " + quantity + ",\n"
                + "    \"phoneNumber\": \"" + phoneNumber + "\"\n"
                + "  }\n"
                + "]";
    }

    /** Gửi POST request và trả về response body dạng String. */
    private static String postJson(String apiUrl, String jsonBody) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setDoOutput(true);
        conn.setRequestProperty("accept", "text/plain");
        conn.setRequestProperty("Content-Type", "application/json");

        // Ghi request body
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int statusCode = conn.getResponseCode();
        InputStream is = (statusCode >= 200 && statusCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        if (is == null) {
            throw new IOException("[MudApiGenerator] HTTP " + statusCode + " — không có response body");
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("[MudApiGenerator] HTTP " + statusCode + " error: " + sb);
        }

        return sb.toString().trim();
    }

    /**
     * Parse JSON response để lấy seriesCode đầu tiên.
     *
     * Response structure thực tế:
     * [
     *   {
     *     "seriesCodes": [
     *       { "seriesCode": "RPQ12UA1UU", "status": 2, ... }
     *     ],
     *     "code": "3725",
     *     "voucherType": 2,
     *     ...
     *   }
     * ]
     *
     * Cần lấy: [0].seriesCodes[0].seriesCode
     */
    private static String[] parseCodes(String json) {
        json = json.trim();
        if (json.equals("[]") || json.isEmpty()) return new String[0];

        try {
            com.google.gson.JsonArray rootArr = com.google.gson.JsonParser.parseString(json).getAsJsonArray();
            java.util.List<String> serials = new java.util.ArrayList<>();

            for (int i = 0; i < rootArr.size(); i++) {
                com.google.gson.JsonObject item = rootArr.get(i).getAsJsonObject();

                // Ưu tiên lấy từ seriesCodes[].seriesCode — đây là serial thực tế cần apply
                if (item.has("seriesCodes")) {
                    com.google.gson.JsonArray seriesCodes = item.getAsJsonArray("seriesCodes");
                    for (int j = 0; j < seriesCodes.size(); j++) {
                        com.google.gson.JsonObject sc = seriesCodes.get(j).getAsJsonObject();
                        if (sc.has("seriesCode")) {
                            String serial = sc.get("seriesCode").getAsString().trim();
                            if (!serial.isEmpty()) serials.add(serial);
                        }
                    }
                }
                // Fallback: field "code" hoặc "voucherCode" nếu không có seriesCodes
                else if (item.has("voucherCode")) {
                    serials.add(item.get("voucherCode").getAsString().trim());
                } else if (item.has("code")) {
                    String c = item.get("code").getAsString().trim();
                    // Không add campaign code (4 ký tự số) vào danh sách serial
                    if (!c.isEmpty() && !c.matches("\\d{4,6}")) serials.add(c);
                }
            }
            return serials.toArray(new String[0]);

        } catch (Exception e) {
            System.err.println("[MudApiGenerator] Gson parse lỗi, fallback manual: " + e.getMessage());
            return parseCodesManual(json);
        }
    }

    /** Manual parse đơn giản — fallback khi Gson không available. */
    private static String[] parseCodesManual(String json) {
        // Tìm "seriesCode":"XXXX" trước — đây là serial thực tế
        java.util.List<String> codes = new java.util.ArrayList<>();
        String[] patterns = {"\"seriesCode\":", "\"voucherCode\":", "\"code\":"};

        for (String pattern : patterns) {
            int pos = 0;
            while ((pos = json.indexOf(pattern, pos)) != -1) {
                int start = json.indexOf("\"", pos + pattern.length()) + 1;
                int end = json.indexOf("\"", start);
                if (start > 0 && end > start) {
                    String code = json.substring(start, end).trim();
                    // Bỏ qua campaign code (toàn số, 4-6 ký tự) và UUID
                    if (!code.isEmpty() && !code.matches("\\d{4,6}") && !code.contains("-")) {
                        if (!codes.contains(code)) codes.add(code);
                    }
                }
                pos += pattern.length();
            }
            if (!codes.isEmpty()) break; // Dừng khi tìm thấy serial từ pattern ưu tiên cao nhất
        }

        return codes.toArray(new String[0]);
    }
}
