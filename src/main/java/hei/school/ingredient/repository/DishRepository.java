package hei.school.ingredient.repository;

import hei.school.ingredient.entity.*;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
public class DishRepository {

    private final DataSource dataSource;
    private final IngredientRepository ingredientRepository;

    public DishRepository(DataSource dataSource, IngredientRepository ingredientRepository) {
        this.dataSource = dataSource;
        this.ingredientRepository = ingredientRepository;
    }

    public List<Dish> findAll() throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT id, name, dish_type, selling_price FROM dish";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setDishIngredients(findIngredientsByDishId(dish.getId()));
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public Optional<Dish> findById(int id) throws SQLException {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setDishIngredients(findIngredientsByDishId(dish.getId()));
                return Optional.of(dish);
            }
        }
        return Optional.empty();
    }

    public Dish save(Dish dish) throws SQLException {
        String sql = """
            INSERT INTO dish (id, name, dish_type, selling_price)
            VALUES (?, ?, ?::dish_type, ?)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                dish_type = EXCLUDED.dish_type,
                selling_price = EXCLUDED.selling_price
            RETURNING id
        """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            Integer dishId;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (dish.getId() != null) {
                    ps.setInt(1, dish.getId());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                ps.setString(2, dish.getName());
                ps.setString(3, dish.getDishType().name());
                if (dish.getPrice() != null) {
                    ps.setDouble(4, dish.getPrice());
                } else {
                    ps.setNull(4, Types.DOUBLE);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                    dish.setId(dishId);
                }
            }

            updateDishIngredients(conn, dish);
            conn.commit();
            return findById(dishId).orElseThrow();
        }
    }

    private void updateDishIngredients(Connection conn, Dish dish) throws SQLException {
        // Delete existing associations
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM dish_ingredient WHERE id_dish = ?")) {
            ps.setInt(1, dish.getId());
            ps.executeUpdate();
        }

        // Insert new associations
        if (dish.getDishIngredients() != null && !dish.getDishIngredients().isEmpty()) {
            String insertSql = "INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit) VALUES (?, ?, ?, ?::unit_type)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (DishIngredient di : dish.getDishIngredients()) {
                    ps.setInt(1, dish.getId());
                    ps.setInt(2, di.getIngredient().getId());
                    ps.setDouble(3, di.getQuantity());
                    ps.setString(4, di.getUnit().name());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private List<DishIngredient> findIngredientsByDishId(Integer dishId) throws SQLException {
        List<DishIngredient> dishIngredients = new ArrayList<>();
        String sql = """
            SELECT i.id, i.name, i.price, i.category, di.quantity_required, di.unit
            FROM ingredient i
            JOIN dish_ingredient di ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dishId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                DishIngredient dishIngredient = new DishIngredient();
                dishIngredient.setIngredient(ingredient);
                dishIngredient.setQuantity(rs.getDouble("quantity_required"));
                dishIngredient.setUnit(Unit.valueOf(rs.getString("unit")));

                dishIngredients.add(dishIngredient);
            }
        }
        return dishIngredients;
    }

    private Dish mapDish(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
        Double price = rs.getDouble("selling_price");
        if (!rs.wasNull()) {
            dish.setPrice(price);
        }
        return dish;
    }
}