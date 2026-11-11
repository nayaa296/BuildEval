package dao;

import model.DetailItem;
import model.Material;
import model.RencanaAnggaran;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BuildEvalFileRepository {

    private static final String DELIMITER = "|";
    private static final String PREFIX = "ITEM";

    /**
     * Menyimpan data item dari objek RencanaAnggaran ke file.
     * Format: ITEM|kodeItem|namaItem|satuan|hargaSatuan|jumlah
     *
     * @param lokasi path file tujuan
     * @param rab    objek RencanaAnggaran sumber data
     * @throws IOException              jika terjadi kegagalan I/O
     * @throws IllegalArgumentException jika kode atau nama material mengandung karakter '|'
     */
    public static void simpanKeFile(Path lokasi, RencanaAnggaran rab) throws IOException {
        if (lokasi == null) {
            throw new IllegalArgumentException("Lokasi file tidak boleh null.");
        }
        if (rab == null) {
            throw new IllegalArgumentException("Objek RencanaAnggaran tidak boleh null.");
        }

        // Pastikan direktori induk ada jika ditentukan
        Path parent = lokasi.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(lokasi, StandardCharsets.UTF_8)) {
            for (DetailItem item : rab.getListMaterial()) {
                Material m = item.getMaterial();
                if (m == null) {
                    continue;
                }

                String kodeItem = m.getIdMaterial();
                String namaItem = m.getNamaMaterial();
                String satuan = m.getSatuan();
                double hargaSatuan = m.getHargaSatuan();
                int jumlah = item.getJumlah() > 0 ? item.getJumlah() : (int) Math.round(item.getVolume());

                // Validasi: Cegah penulisan jika kode atau nama material mengandung karakter pemisah '|'
                if (kodeItem != null && kodeItem.contains(DELIMITER)) {
                    throw new IllegalArgumentException("Kode material '" + kodeItem + "' tidak boleh mengandung karakter delimiter '|'");
                }
                if (namaItem != null && namaItem.contains(DELIMITER)) {
                    throw new IllegalArgumentException("Nama material '" + namaItem + "' tidak boleh mengandung karakter delimiter '|'");
                }
                if (satuan != null && satuan.contains(DELIMITER)) {
                    throw new IllegalArgumentException("Satuan material '" + satuan + "' tidak boleh mengandung karakter delimiter '|'");
                }

                // Format: ITEM|kodeItem|namaItem|satuan|hargaSatuan|jumlah
                String baris = String.format("%s|%s|%s|%s|%.2f|%d",
                    PREFIX,
                    kodeItem != null ? kodeItem : "",
                    namaItem != null ? namaItem : "",
                    satuan != null ? satuan : "",
                    hargaSatuan,
                    jumlah
                );

                writer.write(baris);
                writer.newLine();
            }
        }
    }

    /**
     * Membaca data item dari file dan merekonstruksinya ke rabTarget.
     * Menggunakan strategi membaca seluruh baris ke List sementara terlebih dahulu.
     * Jika seluruh baris valid, barulah rabTarget dibersihkan dan diisi data baru.
     *
     * @param lokasi    path file sumber
     * @param rabTarget objek RencanaAnggaran penampung hasil pembacaan
     * @throws IOException jika terjadi kegagalan I/O atau format file tidak valid
     */
    public static void muatDariFile(Path lokasi, RencanaAnggaran rabTarget) throws IOException {
        if (lokasi == null || !Files.exists(lokasi)) {
            throw new IOException("File tidak ditemukan pada lokasi: " + lokasi);
        }
        if (rabTarget == null) {
            throw new IllegalArgumentException("Objek target RencanaAnggaran tidak boleh null.");
        }

        // BACA DAHULU seluruh baris file ke dalam koleksi sementara
        List<DetailItem> listSementara = new ArrayList<>();
        int nomorBaris = 0;

        try (BufferedReader reader = Files.newBufferedReader(lokasi, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                nomorBaris++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Abaikan baris kosong atau komentar
                }

                // Gunakan regex split literal "\\|"
                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) {
                    throw new IOException(String.format("Format baris %d tidak valid: '%s'. Minimal harus ada 6 kolom.", nomorBaris, line));
                }

                String header = parts[0];
                if (!PREFIX.equalsIgnoreCase(header)) {
                    throw new IOException(String.format("Prefix baris %d tidak valid: '%s' (harus '%s')", nomorBaris, header, PREFIX));
                }

                String kodeItem = parts[1].trim();
                String namaItem = parts[2].trim();
                String satuan = parts[3].trim();
                double hargaSatuan;
                int jumlah;

                try {
                    // Mendukung pembacaan angka desimal titik maupun koma
                    hargaSatuan = Double.parseDouble(parts[4].trim().replace(',', '.'));
                } catch (NumberFormatException e) {
                    throw new IOException(String.format("Format harga satuan tidak valid pada baris %d: '%s'", nomorBaris, parts[4]), e);
                }

                try {
                    jumlah = Integer.parseInt(parts[5].trim());
                } catch (NumberFormatException e) {
                    throw new IOException(String.format("Format jumlah tidak valid pada baris %d: '%s'", nomorBaris, parts[5]), e);
                }

                // Buat objek Material & DetailItem
                Material material = new Material(kodeItem, namaItem, satuan, hargaSatuan);
                DetailItem detail = new DetailItem(material, jumlah);
                detail.setIdRAB(rabTarget.getIdRAB());

                listSementara.add(detail);
            }
        }

        // JIKA SELURUH BARIS VALID: bersihkan isi material pada rabTarget, lalu masukkan koleksi sementara
        rabTarget.kosongkanItem();
        for (DetailItem item : listSementara) {
            rabTarget.tambahItemInternal(item);
        }
    }
}

