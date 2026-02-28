package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse db = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (db == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' not found");
    }
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.archivedAt = warehouse.archivedAt;
    getEntityManager().flush();
  }

  @Override
  public void remove(Warehouse warehouse) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  /**
   * Search active (non-archived) warehouses with optional filters, sort and pagination.
   * All filters are AND logic. sortBy is "createdAt" (default) or "capacity"; sortOrder is "asc" (default) or "desc".
   */
  public List<Warehouse> search(
      String location,
      Integer minCapacity,
      Integer maxCapacity,
      String sortBy,
      String sortOrder,
      int page,
      int pageSize) {
    String orderField = "createdAt".equalsIgnoreCase(sortBy) ? "w.createdAt" : "w.capacity";
    String direction = "desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
    String jpql =
        "SELECT w FROM DbWarehouse w WHERE w.archivedAt IS NULL"
            + " AND (:location IS NULL OR w.location = :location)"
            + " AND (:minCap IS NULL OR w.capacity >= :minCap)"
            + " AND (:maxCap IS NULL OR w.capacity <= :maxCap)"
            + " ORDER BY " + orderField + " " + direction;

    Query query = getEntityManager().createQuery(jpql);
    query.setParameter("location", location);
    query.setParameter("minCap", minCapacity);
    query.setParameter("maxCap", maxCapacity);
    query.setFirstResult(page * pageSize);
    query.setMaxResults(pageSize);

    @SuppressWarnings("unchecked")
    List<DbWarehouse> results = query.getResultList();
    return results.stream().map(DbWarehouse::toWarehouse).toList();
  }
}
