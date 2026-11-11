import exception.BudgetOverlimitException;
import model.DetailItem;
import model.Manajer;
import model.Material;
import model.RencanaAnggaran;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("          SIMULASI PENGUJIAN MANAJEMEN KOLEKSI MATERIAL MODUL 11 - BUILDEVAL             ");
        System.out.println("==========================================================================================\n");

        // 1. Inisialisasi Objek Manajer dan RencanaAnggaran
        Manajer manajer = new Manajer("M001", "Budi Santoso, S.T.", "081234567890");
        RencanaAnggaran rab = new RencanaAnggaran("RAB-2026-M11", "Proyek Renovasi Gedung Serbaguna", 25000000.0, manajer);

        System.out.println("Status Awal: Objek Rencana Anggaran berhasil diinisialisasi.");
        System.out.printf("Batas Budget: Rp %,.2f | Total Item Saat Ini: %d%n%n", 
            rab.getBatasBudget(), rab.getListMaterial().size());

        // 2. Tambah beberapa material ke dalam RAB
        System.out.println(">>> 1. Menambahkan beberapa material ke dalam RAB...");
        Material semen = new Material("MAT01", "Semen Portland 50kg", "Sak", 75000.0);
        Material pasir = new Material("MAT02", "Pasir Pasang", "m3", 250000.0);
        Material besi = new Material("MAT03", "Besi Beton 10mm", "Batang", 90000.0);
        Material cat = new Material("MAT04", "Cat Tembok Eksterior 20kg", "Pail", 650000.0);

        try {
            rab.tambahItem(new DetailItem(semen, 40));   // 40 * 75.000 = 3.000.000
            rab.tambahItem(new DetailItem(pasir, 12));   // 12 * 250.000 = 3.000.000
            rab.tambahItem(new DetailItem(besi, 50));    // 50 * 90.000 = 4.500.000
            rab.tambahItem(new DetailItem(cat, 8));      // 8 * 650.000 = 5.200.000
            System.out.println("[SUKSES] 4 material berhasil ditambahkan ke RAB.");
        } catch (BudgetOverlimitException e) {
            System.err.println("[ERROR] Gagal menambah item: " + e.getMessage());
        }

        // Tampilkan total item dan total biaya
        System.out.printf("Total Item di RAB : %d item%n", rab.getListMaterial().size());
        System.out.printf("Total Biaya RAB   : Rp %,.2f%n%n", rab.hitungTotalBiaya());

        // 3. Pencarian Material dengan cariItem() (case-insensitive)
        System.out.println(">>> 2. Pengujian cariItem()...");
        
        // Pencarian sukses (uji huruf kecil 'mat02')
        String cariKode1 = "mat02";
        System.out.println("Mencari item dengan kode: '" + cariKode1 + "' (case-insensitive)...");
        DetailItem itemDitemukan = rab.cariItem(cariKode1);
        if (itemDitemukan != null) {
            System.out.printf("  -> [DITEMUKAN] Kode: %s | Nama: %s | Jumlah: %d %s | Subtotal: Rp %,.2f%n",
                itemDitemukan.getMaterial().getIdMaterial(),
                itemDitemukan.getMaterial().getNamaMaterial(),
                itemDitemukan.getJumlah(),
                itemDitemukan.getMaterial().getSatuan(),
                itemDitemukan.getSubtotal()
            );
        } else {
            System.out.println("  -> [TIDAK DITEMUKAN] Item tidak ada dalam daftar.");
        }

        // Pencarian gagal (kode tidak terdaftar)
        String cariKode2 = "MAT99";
        System.out.println("Mencari item dengan kode: '" + cariKode2 + "'...");
        DetailItem itemTidakAda = rab.cariItem(cariKode2);
        if (itemTidakAda != null) {
            System.out.printf("  -> [DITEMUKAN] %s%n", itemTidakAda.getMaterial().getNamaMaterial());
        } else {
            System.out.println("  -> [TIDAK DITEMUKAN] Item dengan kode '" + cariKode2 + "' tidak terdaftar dalam RAB.");
        }
        System.out.println();

        // 4. Pengujian hapusItem() untuk item yang ada
        System.out.println(">>> 3. Pengujian hapusItem() untuk item yang ada di RAB...");
        String hapusKode1 = "MAT03"; // Besi Beton 10mm
        System.out.println("Menghapus material dengan kode: '" + hapusKode1 + "'...");
        boolean statusHapus1 = rab.hapusItem(hapusKode1);
        System.out.printf("  -> Status penghapusan: %s%n", statusHapus1 ? "BERHASIL DIHAPUS" : "GAGAL DIHAPUS");
        System.out.printf("  -> Total Item Sekarang: %d item | Total Biaya: Rp %,.2f%n%n", 
            rab.getListMaterial().size(), rab.hitungTotalBiaya());

        // 5. Pengujian hapusItem() untuk item yang TIDAK terdaftar
        System.out.println(">>> 4. Pengujian hapusItem() untuk item yang TIDAK terdaftar...");
        String hapusKode2 = "MAT99";
        System.out.println("Menghapus material dengan kode: '" + hapusKode2 + "'...");
        boolean statusHapus2 = rab.hapusItem(hapusKode2);
        System.out.printf("  -> Status penghapusan: %s (Item tidak ditemukan)%n", statusHapus2 ? "BERHASIL" : "GAGAL (false)");
        System.out.printf("  -> Total Item Sekarang: %d item | Total Biaya: Rp %,.2f%n%n", 
            rab.getListMaterial().size(), rab.hitungTotalBiaya());

        // 6. Uji Immutability / UnmodifiableList getListMaterial()
        System.out.println(">>> 5. Pengujian Keamanan Koleksi (UnmodifiableList)...");
        try {
            rab.getListMaterial().add(new DetailItem(besi, 10));
            System.out.println("  -> [PERINGATAN] Koleksi dapat dimodifikasi langsung!");
        } catch (UnsupportedOperationException e) {
            System.out.println("  -> [TERLINDUNGI] Koleksi internal aman dari manipulasi langsung via getListMaterial().add(): " 
                + e.getClass().getSimpleName() + " dilempar.");
        }
        System.out.println();

        // 7. Cetak Laporan Akhir RAB
        System.out.println(">>> 6. Laporan Akhir Rencana Anggaran Biaya (RAB):");
        rab.cetakLaporanRAB();
    }
}
