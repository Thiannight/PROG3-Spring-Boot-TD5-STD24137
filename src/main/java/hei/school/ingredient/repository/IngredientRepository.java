package hei.school.ingredient.repository;

import hei.school.ingredient.entity.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;

@Repository
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ingredient> findAll() throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ingredient");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ingredient i = mapIngredient(rs);
                i.setStockMovementList(findStockMovementsByIngredientId(i.getId()));
                ingredients.add(i);
            }
        }
        return ingredients;
    }

    public Optional<Ingredient> findById(int id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ingredient WHERE id = ?")) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Ingredient i = mapIngredient(rs);
                i.setStockMovementList(findStockMovementsByIngredientId(i.getId()));
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public List<StockMovement> findStockMovementsByIngredientId(Integer id) throws SQLException {
        List<StockMovement> stockMovements = new ArrayList<>();
        String sql = "SELECT id, quantity, unit, type, creation_datetime FROM stock_movement WHERE id_ingredient = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StockMovement sm = new StockMovement();
                sm.setId(rs.getInt("id"));
                sm.setType(MovementTypeEnum.valueOf(rs.getString("type")));
                sm.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                StockValue sv = new StockValue();
                sv.setQuantity(rs.getDouble("quantity"));
                sv.setUnit(Unit.valueOf(rs.getString("unit")));
                sm.setValue(sv);

                stockMovements.add(sm);
            }
        }
        return stockMovements;
    }

    public StockValue getStockValueAt(Instant t, Integer ingredientId) throws SQLException {
        String sql = """
            SELECT unit,
                   sum(case
                       when stock_movement.type = 'IN' then quantity
                       when stock_movement.type = 'OUT' then -1 * quantity
                       else 0 END) as actual_quantity
            FROM stock_movement
            WHERE id_ingredient = ? AND unit = ? AND creation_datetime <= ?
            GROUP BY unit
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ingredientId);
            stmt.setString(2, "KG");
            stmt.setTimestamp(3, Timestamp.from(t));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                StockValue sv = new StockValue();
                sv.setQuantity(rs.getDouble("actual_quantity"));
                sv.setUnit(Unit.valueOf(rs.getString("unit")));
                return sv;
            }
            return null;
        }
    }

    private Ingredient mapIngredient(ResultSet rs) throws SQLException {
        Ingredient i = new Ingredient();
        i.setId(rs.getInt("id"));
        i.setName(rs.getString("name"));
        Double price = rs.getDouble("price");
        if (!rs.wasNull()) {
            i.setPrice(price);
        }
        i.setCategory(CategoryEnum.valueOf(rs.getString("category")));
        return i;
    }
}