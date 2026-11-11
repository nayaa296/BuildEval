import dao.BuildEvalFileRepository;
import exception.BudgetOverlimitException;
import model.DetailItem;
import model.Manajer;
import model.Material;
import model.RencanaAnggaran;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("               PENGUJIAN REPOSITORI BERKAS MODUL 12 - BUILDEVAL                           ");
        System.out.println("==========================================================================================\n");

        Path lokasiFile = Paths.get("buildeval_backup.txt");

        try {
            // Skenario a: Inisialisasi RAB dan tambahkan beberapa material
            System.out.println(">>> [A] Menginisialisasi Objek RAB Sumber dan Menambahkan Material...");
            Manajer manajer = new Manajer("M001", "Budi Santoso, S.T.", "081234567890");
            RencanaAnggaran rabSumber = new RencanaAnggaran("RAB-2026-F12", "Pembangunan Gedung Laboratorium", 50000000.0, manajer);

            Material semen = new Material("MAT01", "Semen Portland 50kg", "Sak", 75000.0);
            Material pasir = new Material("MAT02", "Pasir Pasang", "m3", 260000.0);
            Material besi = new Material("MAT03", "Besi Beton 10mm", "Batang", 85000.0);
            Material genteng = new Material("MAT04", "Genteng Keramik", "Pcs", 4500.0);

            rabSumber.tambahItem(new DetailItem(semen, 50));     // 50 * 75.000 = 3.750.000
            rabSumber.tambahItem(new DetailItem(pasir, 15));     // 15 * 260.000 = 3.900.000
            rabSumber.tambahItem(new DetailItem(besi, 80));      // 80 * 85.000 = 6.800.000
            rabSumber.tambahItem(new DetailItem(genteng, 1200)); // 1200 * 4.500 = 5.400.000

            System.out.printf("  -> RAB Sumber berhasil diisi: %d item | Total Biaya: Rp %,.2f%n%n", 
                rabSumber.getListMaterial().size(), rabSumber.hitungTotalBiaya());

            // Skenario b: Simpan data RAB ke file buildeval_backup.txt
            System.out.println(">>> [B] Menyimpan data RAB ke file berkas: '" + lokasiFile.toAbsolutePath() + "'...");
            BuildEvalFileRepository.simpanKeFile(lokasiFile, rabSumber);
            System.out.println("  -> [SUKSES] Data berhasil disimpan ke berkas.");
            System.out.println();

            // Skenario c: Buat objek RAB target baru (kosong), lalu muat kembali dari file
            System.out.println(">>> [C] Membuat Objek RAB Target Baru dan Memuat Data dari Berkas...");
            RencanaAnggaran rabTarget = new RencanaAnggaran("RAB-2026-F12", "Pembangunan Gedung Laboratorium (Dari Backup)", 50000000.0, manajer);
            System.out.printf("  -> Kondisi awal objek target sebelum muat: %d item%n", rabTarget.getListMaterial().size());

            BuildEvalFileRepository.muatDariFile(lokasiFile, rabTarget);
            System.out.println("  -> [SUKSES] Data berhasil dimuat kembali ke dalam objek RAB target!");
            System.out.println();

            // Skenario d: Cetak laporan RAB hasil pembacaan file
            System.out.println(">>> [D] Menampilkan Laporan RAB Hasil Rekonstruksi dari Berkas:");
            rabTarget.cetakLaporanRAB();

        } catch (BudgetOverlimitException e) {
            System.err.println("[ERROR Budget] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR Validasi] " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[ERROR I/O Berkas] Terjadi kesalahan berkas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
