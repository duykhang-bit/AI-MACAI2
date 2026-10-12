public class TestMudProd {
    public static void main(String[] args) {
        try {
            System.out.println("🧪 Test đọc MUD PROD serials:");
            System.out.println("=" .repeat(60));
            
            // Test 5 mud keys đầu tiên
            for (int i = 1; i <= 5; i++) {
                String mudKey = String.format("mud%02d", i);
                try {
                    String serial = utils.MudCodeProvider.getNextMudCode(mudKey, true);
                    System.out.println("✅ " + mudKey + " (PROD) → " + serial);
                } catch (Exception e) {
                    System.out.println("❌ " + mudKey + " → LỖI: " + e.getMessage());
                }
            }
            
            System.out.println("\n🧪 Test đọc MUD UAT serials:");
            System.out.println("=".repeat(60));
            
            // Test UAT pool
            try {
                String serial = utils.MudCodeProvider.getNextMudCode("mud2", false);
                System.out.println("✅ mud2 (UAT) → " + serial);
            } catch (Exception e) {
                System.out.println("❌ mud2 → LỖI: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ LỖI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
