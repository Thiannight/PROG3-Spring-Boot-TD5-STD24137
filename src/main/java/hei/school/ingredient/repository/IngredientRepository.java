package hei.school.ingredient.repository;

import hei.school.ingredient.entity.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ingredient> findAll() throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();

        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ingredient");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Ingredient i = new Ingredient();
            i.setId(rs.getInt("id"));
            i.setName(rs.getString("name"));
            i.setPrice(rs.getDouble("price"));
            i.setCategory(CategoryEnum.valueOf(rs.getString("category")));

            ingredients.add(i);
        }

        return ingredients;
    }

    public Optional<Ingredient> findById(int id) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ingredient WHERE id = ?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Ingredient i = new Ingredient();
            i.setId(rs.getInt("id"));
            i.setName(rs.getString("name"));
            i.setPrice(rs.getDouble("price"));
            i.setCategory(CategoryEnum.valueOf(rs.getString("category")));

            return Optional.of(i);
        }

        return Optional.empty();
    }
}