package dao;

import config.DatabaseConfig;
import exception.DataNotFoundException;
import model.DetailItem;
import model.Manajer;
import model.Material;
import model.RencanaAnggaran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RencanaAnggaranDAO {

    public void simpanRAB(RencanaAnggaran rab) throws SQLException {
        // Tabel manajer: id_manajer, nama
        String sqlManajer = "INSERT INTO manajer (id_manajer, nama) " +
                            "VALUES (?, ?) ON DUPLICATE KEY UPDATE nama = VALUES(nama)";

        // Tabel material: kode_item, nama_item, satuan, harga_satuan
        String sqlMaterial = "INSERT INTO material (kode_item, nama_item, satuan, harga_satuan) " +
                             "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE nama_item = VALUES(nama_item), satuan = VALUES(satuan), harga_satuan = VALUES(harga_satuan)";

        // Tabel rencana_anggaran: id_rab, nama_proyek, batas_budget, id_manajer
        String sqlRAB = "INSERT INTO rencana_anggaran (id_rab, nama_proyek, batas_budget, id_manajer) " +
                        "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE nama_proyek = VALUES(nama_proyek), batas_budget = VALUES(batas_budget), id_manajer = VALUES(id_manajer)";

        // Tabel detail_item: id, id_rab, kode_item, jumlah
        String sqlHapusDetailLama = "DELETE FROM detail_item WHERE id_rab = ?";
        String sqlDetail = "INSERT INTO detail_item (id_rab, kode_item, jumlah) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Memulai transaksi

            // 1. Simpan / Perbarui data Manajer jika ada
            if (rab.getManajer() != null) {
                try (PreparedStatement psManajer = conn.prepareStatement(sqlManajer)) {
                    psManajer.setString(1, rab.getManajer().getIdManajer());
                    psManajer.setString(2, rab.getManajer().getNamaManajer());
                    psManajer.executeUpdate();
                }
            }

            // 2. Simpan master Material yang digunakan
            for (DetailItem detail : rab.getListMaterial()) {
                if (detail.getMaterial() != null) {
                    try (PreparedStatement psMat = conn.prepareStatement(sqlMaterial)) {
                        psMat.setString(1, detail.getMaterial().getIdMaterial());
                        psMat.setString(2, detail.getMaterial().getNamaMaterial());
                        psMat.setString(3, detail.getMaterial().getSatuan());
                        psMat.setDouble(4, detail.getMaterial().getHargaSatuan());
                        psMat.executeUpdate();
                    }
                }
            }

            // 3. Simpan data Rencana Anggaran
            try (PreparedStatement psRAB = conn.prepareStatement(sqlRAB)) {
                psRAB.setString(1, rab.getIdRAB());
                psRAB.setString(2, rab.getNamaProyek());
                psRAB.setDouble(3, rab.getBatasBudget());
                psRAB.setString(4, rab.getManajer() != null ? rab.getManajer().getIdManajer() : null);
                psRAB.executeUpdate();
            }

            // 4. Bersihkan detail lama (agar sinkron saat pembaruan)
            try (PreparedStatement psDel = conn.prepareStatement(sqlHapusDetailLama)) {
                psDel.setString(1, rab.getIdRAB());
                psDel.executeUpdate();
            }

            // 5. Simpan item detail_item dengan batch
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                for (DetailItem detail : rab.getListMaterial()) {
                    psDetail.setString(1, rab.getIdRAB());
                    psDetail.setString(2, detail.getMaterial() != null ? detail.getMaterial().getIdMaterial() : null);
                    int qty = detail.getJumlah() > 0 ? detail.getJumlah() : (int) Math.round(detail.getVolume());
                    psDetail.setInt(3, qty);
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            conn.commit(); // Berhasil: Commit transaksi
            System.out.println("-> Data RAB [" + rab.getIdRAB() + "] berhasil disimpan ke database (Commit berhasil).");
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // Gagal: Rollback perubahan
                    System.err.println("-> Terjadi kesalahan, transaksi di-rollback: " + ex.getMessage());
                } catch (SQLException rbEx) {
                    System.err.println("-> Gagal melakukan rollback: " + rbEx.getMessage());
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("-> Gagal menutup koneksi: " + closeEx.getMessage());
                }
            }
        }
    }

    public RencanaAnggaran getRABById(String idRAB) throws SQLException, DataNotFoundException {
        String query = "SELECT r.id_rab, r.nama_proyek, r.batas_budget, " +
                       "       m.id_manajer, m.nama AS nama_manajer, " +
                       "       d.id AS id_detail, d.jumlah, " +
                       "       mat.kode_item, mat.nama_item AS nama_material, mat.satuan, mat.harga_satuan " +
                       "FROM rencana_anggaran r " +
                       "LEFT JOIN manajer m ON r.id_manajer = m.id_manajer " +
                       "LEFT JOIN detail_item d ON r.id_rab = d.id_rab " +
                       "LEFT JOIN material mat ON d.kode_item = mat.kode_item " +
                       "WHERE r.id_rab = ?";

        RencanaAnggaran rab = null;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idRAB);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rab == null) {
                        rab = new RencanaAnggaran();
                        rab.setIdRAB(rs.getString("id_rab"));
                        rab.setNamaProyek(rs.getString("nama_proyek"));
                        rab.setBatasBudget(rs.getDouble("batas_budget"));

                        String idManajer = rs.getString("id_manajer");
                        if (idManajer != null) {
                            Manajer manajer = new Manajer();
                            manajer.setIdManajer(idManajer);
                            manajer.setNamaManajer(rs.getString("nama_manajer"));
                            rab.setManajer(manajer);
                        }
                    }

                    int idDetail = rs.getInt("id_detail");
                    if (idDetail > 0) {
                        Material mat = null;
                        String kodeItem = rs.getString("kode_item");
                        if (kodeItem != null) {
                            mat = new Material(
                                kodeItem,
                                rs.getString("nama_material"),
                                rs.getString("satuan"),
                                rs.getDouble("harga_satuan")
                            );
                        }

                        DetailItem item = new DetailItem();
                        item.setIdDetail(idDetail);
                        item.setIdRAB(rab.getIdRAB());
                        item.setMaterial(mat);
                        item.setJumlah(rs.getInt("jumlah"));
                        item.hitungSubtotal();

                        rab.getListMaterial().add(item);
                    }
                }
            }
        }

        if (rab == null) {
            throw new DataNotFoundException("Data RAB dengan ID '" + idRAB + "' tidak ditemukan di database.");
        }

        return rab;
    }
}
