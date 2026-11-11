import dao.BuildEvalFileRepository;
import dao.RencanaAnggaranDAO;
import exception.BudgetOverlimitException;
import model.DetailItem;
import model.Manajer;
import model.Material;
import model.RencanaAnggaran;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Path BACKUP_PATH = Paths.get("buildeval_backup.txt");
    private static final RencanaAnggaranDAO dao = new RencanaAnggaranDAO();
    private static boolean apakahAdaPerubahan = false;

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("            SISTEM EVALUASI ANGGARAN BIAYA PROYEK - BUILDEVAL (MODUL 13)                 ");
        System.out.println("==========================================================================================");

        // Inisialisasi awal proyek dan manajer
        Manajer manajer = new Manajer("M001", "Budi Santoso, S.T.", "081234567890");
        RencanaAnggaran rab = new RencanaAnggaran("RAB-2026-001", "Pembangunan Fasilitas Publik", 50000000.0, manajer);

        System.out.println("Inisialisasi Proyek Default:");
        System.out.println("  ID RAB       : " + rab.getIdRAB());
        System.out.println("  Nama Proyek  : " + rab.getNamaProyek());
        System.out.printf("  Batas Budget : Rp %,.2f%n", rab.getBatasBudget());
        System.out.println("  Manajer      : " + manajer.getNamaManajer());
        System.out.println("==========================================================================================\n");

        boolean berjalan = true;

        while (berjalan) {
            tampilkanMenu();
            System.out.print("Pilih opsi menu [0-6]: ");
            String inputPilihan = scanner.nextLine().trim();

            switch (inputPilihan) {
                case "1":
                    menuTambahMaterial(rab);
                    break;
                case "2":
                    menuTampilkanLaporan(rab);
                    break;
                case "3":
                    menuCariMaterial(rab);
                    break;
                case "4":
                    menuHapusMaterial(rab);
                    break;
                case "5":
                    menuSimpanData(rab);
                    break;
                case "6":
                    menuMuatUlangData(rab);
                    break;
                case "0":
                    berjalan = menuKeluarAplikasi();
                    break;
                default:
                    System.out.println("\n[PERINGATAN] Pilihan menu tidak valid! Silakan masukkan angka 0 sampai 6.\n");
                    break;
            }
        }

        System.out.println("\nTerima kasih telah menggunakan aplikasi BuildEval. Sampai jumpa!");
    }

    private static void tampilkanMenu() {
        System.out.println("==========================================================================================");
        System.out.println("                                  MENU UTAMA BUILDEVAL                                    ");
        System.out.println("==========================================================================================");
        System.out.println(" [1] Tambah Material Baru ke RAB");
        System.out.println(" [2] Tampilkan Laporan Evaluasi RAB (Layar Konsol)");
        System.out.println(" [3] Cari Material berdasarkan Kode Item");
        System.out.println(" [4] Hapus Material dari RAB");
        System.out.println(" [5] Simpan Data (Ke Database MySQL XAMPP & Backup File Teks)");
        System.out.println(" [6] Muat Ulang Data dari File Backup");
        System.out.println(" [0] Keluar Aplikasi");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println(" Status Perubahan Belum Disimpan: " + (apakahAdaPerubahan ? "[YA - Perlu Disimpan]" : "[TIDAK]"));
        System.out.println("------------------------------------------------------------------------------------------");
    }

    private static void menuTambahMaterial(RencanaAnggaran rab) {
        System.out.println("\n--- [1] TAMBAH MATERIAL BARU KE RAB ---");
        try {
            System.out.print("Masukkan Kode Item (contoh: MAT01): ");
            String kode = scanner.nextLine().trim();
            if (kode.isEmpty()) {
                System.out.println("[GAGAL] Kode item tidak boleh kosong!\n");
                return;
            }

            // Validasi apakah kode sudah ada
            if (rab.cariItem(kode) != null) {
                System.out.println("[GAGAL] Item dengan kode '" + kode + "' sudah ada di dalam RAB!\n");
                return;
            }

            System.out.print("Masukkan Nama Material: ");
            String nama = scanner.nextLine().trim();
            if (nama.isEmpty()) {
                System.out.println("[GAGAL] Nama material tidak boleh kosong!\n");
                return;
            }

            System.out.print("Masukkan Satuan (misal: Sak, m3, Batang, Pcs): ");
            String satuan = scanner.nextLine().trim();
            if (satuan.isEmpty()) {
                System.out.println("[GAGAL] Satuan material tidak boleh kosong!\n");
                return;
            }

            System.out.print("Masukkan Harga Satuan (Rp): ");
            String strHarga = scanner.nextLine().trim();
            double hargaSatuan = Double.parseDouble(strHarga.replace(',', '.'));

            System.out.print("Masukkan Jumlah Pembelian: ");
            String strJumlah = scanner.nextLine().trim();
            int jumlah = Integer.parseInt(strJumlah);

            // Buat objek Material & DetailItem
            Material materialBaru = new Material(kode, nama, satuan, hargaSatuan);
            DetailItem detailBaru = new DetailItem(materialBaru, jumlah);

            // Tambahkan ke RAB dengan validasi batas budget
            rab.tambahItem(detailBaru);

            apakahAdaPerubahan = true;
            System.out.println("\n[SUKSES] Material '" + nama + "' berhasil ditambahkan ke RAB.");
            System.out.printf("Total Biaya RAB Saat Ini: Rp %,.2f | Sisa Budget: Rp %,.2f%n\n", 
                rab.hitungTotalBiaya(), (rab.getBatasBudget() - rab.hitungTotalBiaya()));

        } catch (NumberFormatException e) {
            System.out.println("\n[GAGAL INPUT] Input angka tidak valid! Harap masukkan angka yang benar.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("\n[GAGAL VALIDASI] " + e.getMessage() + "\n");
        } catch (BudgetOverlimitException e) {
            System.out.println("\n[GAGAL ANGGARAN] " + e.getMessage() + "\n");
        }
    }

    private static void menuTampilkanLaporan(RencanaAnggaran rab) {
        System.out.println();
        rab.cetakLaporanRAB();
        System.out.println();
    }

    private static void menuCariMaterial(RencanaAnggaran rab) {
        System.out.println("\n--- [3] CARI MATERIAL BERDASARKAN KODE ITEM ---");
        System.out.print("Masukkan Kode Item yang dicari: ");
        String kode = scanner.nextLine().trim();

        if (kode.isEmpty()) {
            System.out.println("[GAGAL] Kode item tidak boleh kosong!\n");
            return;
        }

        DetailItem item = rab.cariItem(kode);
        if (item != null) {
            Material m = item.getMaterial();
            System.out.println("\n[DITEMUKAN]");
            System.out.println("  Kode Item     : " + m.getIdMaterial());
            System.out.println("  Nama Material : " + m.getNamaMaterial());
            System.out.println("  Jumlah        : " + item.getJumlah() + " " + m.getSatuan());
            System.out.printf("  Harga Satuan  : Rp %,.2f%n", m.getHargaSatuan());
            System.out.printf("  Subtotal      : Rp %,.2f%n\n", item.getSubtotal());
        } else {
            System.out.println("\n[TIDAK DITEMUKAN] Material dengan kode '" + kode + "' tidak terdaftar dalam RAB.\n");
        }
    }

    private static void menuHapusMaterial(RencanaAnggaran rab) {
        System.out.println("\n--- [4] HAPUS MATERIAL DARI RAB ---");
        System.out.print("Masukkan Kode Item yang akan dihapus: ");
        String kode = scanner.nextLine().trim();

        if (kode.isEmpty()) {
            System.out.println("[GAGAL] Kode item tidak boleh kosong!\n");
            return;
        }

        boolean berhasilHapus = rab.hapusItem(kode);
        if (berhasilHapus) {
            apakahAdaPerubahan = true;
            System.out.println("\n[SUKSES] Material dengan kode '" + kode + "' berhasil dihapus dari RAB.");
            System.out.printf("Total Biaya RAB Sekarang: Rp %,.2f%n\n", rab.hitungTotalBiaya());
        } else {
            System.out.println("\n[GAGAL] Material dengan kode '" + kode + "' tidak ditemukan dalam daftar RAB.\n");
        }
    }

    private static void menuSimpanData(RencanaAnggaran rab) {
        System.out.println("\n--- [5] SIMPAN DATA (DATABASE & FILE BACKUP) ---");

        boolean suksesDB = false;
        boolean suksesFile = false;

        // 1. Simpan ke MySQL XAMPP Database
        System.out.println("1. Menyimpan ke MySQL Database ('buildeval_db')...");
        try {
            dao.simpanRAB(rab);
            suksesDB = true;
        } catch (SQLException e) {
            System.err.println("   [ERROR DATABASE] Gagal menyimpan ke database MySQL: " + e.getMessage());
        }

        // 2. Simpan ke File Teks Backup
        System.out.println("2. Menyimpan ke File Teks Backup ('" + BACKUP_PATH.getFileName() + "')...");
        try {
            BuildEvalFileRepository.simpanKeFile(BACKUP_PATH, rab);
            suksesFile = true;
            System.out.println("   -> Berkas backup berhasil diperbarui.");
        } catch (IOException e) {
            System.err.println("   [ERROR FILE] Gagal menyimpan ke file backup: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("   [ERROR VALIDASI] " + e.getMessage());
        }

        if (suksesDB || suksesFile) {
            apakahAdaPerubahan = false;
            System.out.println("\n[INFORMASI] Proses penyimpanan selesai.");
        } else {
            System.out.println("\n[GAGAL] Seluruh proses penyimpanan mengalami kendala.");
        }
        System.out.println();
    }

    private static void menuMuatUlangData(RencanaAnggaran rab) {
        System.out.println("\n--- [6] MUAT ULANG DATA DARI FILE BACKUP ---");

        // Konfirmasi jika ada perubahan yang belum disimpan
        if (apakahAdaPerubahan) {
            System.out.println("[PERHATIAN] Ada perubahan yang belum disimpan.");
            System.out.print("Yakin ingin melanjutkan dan menimpa perubahan saat ini? (YA/TIDAK): ");
            String konfirmasi = scanner.nextLine().trim();
            if (!konfirmasi.equalsIgnoreCase("YA")) {
                System.out.println("[BATAL] Operasi muat ulang data dibatalkan.\n");
                return;
            }
        }

        try {
            BuildEvalFileRepository.muatDariFile(BACKUP_PATH, rab);
            apakahAdaPerubahan = false;
            System.out.println("[SUKSES] Data berhasil dimuat ulang dari berkas backup: " + BACKUP_PATH.getFileName());
            System.out.printf("Total Item: %d | Total Biaya: Rp %,.2f%n\n", 
                rab.getListMaterial().size(), rab.hitungTotalBiaya());
        } catch (IOException e) {
            System.err.println("[ERROR FILE] Gagal memuat data dari file backup: " + e.getMessage() + "\n");
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR VALIDASI] " + e.getMessage() + "\n");
        }
    }

    private static boolean menuKeluarAplikasi() {
        if (apakahAdaPerubahan) {
            System.out.println("\n[PERHATIAN] Ada perubahan yang belum disimpan.");
            System.out.print("Yakin ingin keluar dari aplikasi? (YA/TIDAK): ");
            String konfirmasi = scanner.nextLine().trim();
            if (!konfirmasi.equalsIgnoreCase("YA")) {
                System.out.println("[BATAL] Keluar aplikasi dibatalkan. Silakan simpan data terlebih dahulu.\n");
                return true; // Tetap berjalan
            }
        }
        return false; // Berhenti / keluar
    }
}
