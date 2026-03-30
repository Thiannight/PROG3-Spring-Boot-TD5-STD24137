package hei.school.ingredient.service;

import hei.school.ingredient.entity.*;
import hei.school.ingredient.exception.NotFoundException;
import hei.school.ingredient.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        return ingredientRepository.findAll();
    }

    public Ingredient getIngredientById(Integer id) throws SQLException {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient.id=" + id + " not found"));
    }

    public StockValue getStockValueAt(Integer ingredientId, Instant at, Unit unit) throws SQLException {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingredient.id=" + ingredientId + " not found"));

        if (ingredient.getStockMovementList() == null || ingredient.getStockMovementList().isEmpty()) {
            StockValue sv = new StockValue();
            sv.setQuantity(0.0);
            sv.setUnit(unit);
            return sv;
        }

        double sum = ingredient.getStockMovementList().stream()
                .filter(sm -> sm.getValue().getUnit() == unit)
                .filter(sm -> !sm.getCreationDatetime().isAfter(at))
                .mapToDouble(sm -> sm.getType() == MovementTypeEnum.IN
                        ? sm.getValue().getQuantity()
                        : -sm.getValue().getQuantity())
                .sum();

        StockValue result = new StockValue();
        result.setQuantity(sum);
        result.setUnit(unit);
        return result;
    }
}