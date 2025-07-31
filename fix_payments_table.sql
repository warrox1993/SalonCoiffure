-- Suppression de la colonne salon_id pour la migration mono-salon
USE afrostyle_db;

-- Vérifier si la colonne existe avant de la supprimer
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS 
     WHERE TABLE_SCHEMA='afrostyle_db' AND TABLE_NAME='payments' AND COLUMN_NAME='salon_id') > 0,
    'ALTER TABLE payments DROP COLUMN salon_id',
    'SELECT "Column salon_id does not exist" as message'
));

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Vérifier la structure de la table après modification
DESCRIBE payments;