-- Database: buildeval_db
CREATE DATABASE IF NOT EXISTS buildeval_db;
USE buildeval_db;

-- Tabel Manajer
CREATE TABLE IF NOT EXISTS manajer (
    id_manajer VARCHAR(20) PRIMARY KEY,
    nama_manajer VARCHAR(100) NOT NULL,
    kontak VARCHAR(50)
);

-- Tabel Material
CREATE TABLE IF NOT EXISTS material (
    id_material VARCHAR(20) PRIMARY KEY,
    nama_material VARCHAR(100) NOT NULL,
    satuan VARCHAR(20) NOT NULL,
    harga_satuan DOUBLE NOT NULL
);

-- Tabel Rencana Anggaran
CREATE TABLE IF NOT EXISTS rencana_anggaran (
    id_rab VARCHAR(30) PRIMARY KEY,
    nama_proyek VARCHAR(150) NOT NULL,
    batas_pagu DOUBLE NOT NULL,
    id_manajer VARCHAR(20),
    FOREIGN KEY (id_manajer) REFERENCES manajer(id_manajer) ON UPDATE CASCADE ON DELETE SET NULL
);

-- Tabel Detail Item
CREATE TABLE IF NOT EXISTS detail_item (
    id_detail INT AUTO_INCREMENT PRIMARY KEY,
    id_rab VARCHAR(30) NOT NULL,
    id_material VARCHAR(20),
    volume DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (id_rab) REFERENCES rencana_anggaran(id_rab) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (id_material) REFERENCES material(id_material) ON UPDATE CASCADE ON DELETE SET NULL
);

