import exception.BudgetOverlimitException;
import model.DetailItem;
import model.Manajer;
import model.Material;
import model.RencanaAnggaran;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("               PENGUJIAN VALIDASI & EXCEPTION HANDLING - BUILDEVAL                        ");
        System.out.println("==========================================================================================\n");

        // Inisialisasi Manajer dan RencanaAnggaran dengan pagu Rp 10.000.000
        Manajer manajer = new Manajer("M001", "Budi Santoso, S.T.", "081234567890");
        RencanaAnggaran rab = new RencanaAnggaran("RAB-SIMULASI-001", "Renovasi Ruang Kantor", 10000000.0, manajer);

        // Data Master Material untuk simulasi
        Material semen = new Material("MAT01", "Semen PPC", "Sak", 70000.0);
        Material cat = new Material("MAT02", "Cat Tembok 20kg", "Pail", 650000.0);
        Material keramik = new Material("MAT03", "Keramik Granit 60x60", "Dus", 200000.0);

        System.out.printf("Objek Rencana Anggaran diinisialisasi dengan Batas Budget: Rp %,.2f%n%n", rab.getBatasBudget());

        // -------------------------------------------------------------------------
        // Skenario a: Skenario Berhasil (Semen PPC 30 sak -> 30 * 70.000 = Rp 2.100.000)
        // -------------------------------------------------------------------------
        System.out.println(">>> [SKENARIO A] Percobaan Penambahan Item yang Valid (Semen PPC 30 sak)...");
        try {
            DetailItem item1 = new DetailItem(semen, 30);
            rab.tambahItem(item1);
            System.out.println("[SUKSES] Item '" + semen.getNamaMaterial() + "' (30 sak) berhasil ditambahkan.");
        } catch (BudgetOverlimitException e) {
            System.err.println("[DITANGKAP BudgetOverlimitException] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[DITANGKAP IllegalArgumentException] " + e.getMessage());
        } finally {
            System.out.printf("[FINALLY A] Total Biaya Saat Ini: Rp %,.2f | Total Item: %d%n%n", 
                rab.hitungTotalBiaya(), rab.getListMaterial().size());
        }

        // -------------------------------------------------------------------------
        // Skenario b: Skenario Gagal 1 - Input jumlah negatif (-5)
        // -------------------------------------------------------------------------
        System.out.println(">>> [SKENARIO B] Percobaan Penambahan Item dengan Jumlah Negatif (-5)...");
        try {
            DetailItem itemNegatif = new DetailItem(cat, -5);
            rab.tambahItem(itemNegatif);
            System.out.println("[SUKSES] Item berhasil ditambahkan.");
        } catch (BudgetOverlimitException e) {
            System.err.println("[DITANGKAP BudgetOverlimitException] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[DITANGKAP IllegalArgumentException] " + e.getMessage());
        } finally {
            System.out.printf("[FINALLY B] Total Biaya Saat Ini: Rp %,.2f | Total Item: %d%n%n", 
                rab.hitungTotalBiaya(), rab.getListMaterial().size());
        }

        // -------------------------------------------------------------------------
        // Skenario c: Skenario Gagal 2 - Penambahan item berlebih melebihi budget
        // Budget = 10.000.000, Total saat ini = 2.100.000, Sisa = 7.900.000
        // Tambah Keramik 45 dus * 200.000 = Rp 9.000.000 -> Total estimasi = 11.100.000 (> 10.000.000)
        // -------------------------------------------------------------------------
        System.out.println(">>> [SKENARIO C] Percobaan Penambahan Item Melebihi Budget (Keramik Granit 45 dus @ Rp 200.000 = Rp 9.000.000)...");
        try {
            DetailItem itemOver = new DetailItem(keramik, 45);
            rab.tambahItem(itemOver);
            System.out.println("[SUKSES] Item berhasil ditambahkan.");
        } catch (BudgetOverlimitException e) {
            System.err.println("[DITANGKAP BudgetOverlimitException] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[DITANGKAP IllegalArgumentException] " + e.getMessage());
        } finally {
            System.out.printf("[FINALLY C] Total Biaya Saat Ini: Rp %,.2f | Total Item: %d%n%n", 
                rab.hitungTotalBiaya(), rab.getListMaterial().size());
        }

        // -------------------------------------------------------------------------
        // Laporan Akhir RAB
        // -------------------------------------------------------------------------
        System.out.println(">>> Menampilkan Laporan Akhir Rencana Anggaran Biaya (RAB):");
        rab.cetakLaporanRAB();
    }
}
