import dao.RencanaAnggaranDAO;
import model.DetailItem;
import model.Manajer;
import model.Material;
import model.RencanaAnggaran;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("                     APLIKASI EVALUASI ANGGARAN PROYEK - BUILDEVAL                        ");
        System.out.println("==========================================================================================\n");

        try {
            // 1. Inisialisasi Data Manajer
            Manajer manajer = new Manajer("M001", "Budi Santoso, S.T.", "081234567890");

            // 2. Inisialisasi Data Material
            Material semen = new Material("MAT01", "Semen Portland 50kg", "Sak", 75000.0);
            Material pasir = new Material("MAT02", "Pasir Pasang", "m3", 260000.0);
            Material besi = new Material("MAT03", "Besi Beton 10mm", "Batang", 85000.0);
            Material bata = new Material("MAT04", "Bata Merah Standar", "Pcs", 900.0);

            // 3. Inisialisasi Rencana Anggaran Biaya (RAB)
            // Batas pagu anggaran proyek: Rp 75.000.000,00
            RencanaAnggaran rabBaru = new RencanaAnggaran("RAB-2026-001", "Pembangunan Ruko 2 Lantai", 75000000.0, manajer);

            // Tambahkan rincian detail item
            rabBaru.tambahDetailItem(new DetailItem(semen, 200));   // 200 sak * 75.000 = 15.000.000
            rabBaru.tambahDetailItem(new DetailItem(pasir, 45));    // 45 m3 * 260.000 = 11.700.000
            rabBaru.tambahDetailItem(new DetailItem(besi, 250));    // 250 btg * 85.000 = 21.250.000
            rabBaru.tambahDetailItem(new DetailItem(bata, 15000));  // 15.000 pcs * 900 = 13.500.000

            // 4. Simpan ke Database MySQL (XAMPP) via DAO dengan mekanisme transaksi
            RencanaAnggaranDAO dao = new RencanaAnggaranDAO();
            System.out.println("[1] Menyimpan data RAB ke database 'buildeval_db'...");
            dao.simpanRAB(rabBaru);
            System.out.println();

            // 5. Ambil kembali data dari Database MySQL berbasis JOIN
            System.out.println("[2] Mengambil dan merekonstruksi kembali objek RAB dari database...");
            RencanaAnggaran rabDariDB = dao.getRABById("RAB-2026-001");

            // 6. Cetak laporan kelayakan dari data yang telah direkonstruksi
            if (rabDariDB != null) {
                System.out.println("\n[3] Menampilkan Laporan Hasil Evaluasi:");
                rabDariDB.cetakLaporanRAB();
            } else {
                System.err.println("Data RAB tidak ditemukan di database!");
            }

        } catch (Exception e) {
            System.err.println("Terjadi kesalahan sistem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

