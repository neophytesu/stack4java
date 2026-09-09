package mysql.core.transaction;

import mysql.storage.Catalog;

public record Transaction(long id, Catalog rollbackSnapshot) {
}
