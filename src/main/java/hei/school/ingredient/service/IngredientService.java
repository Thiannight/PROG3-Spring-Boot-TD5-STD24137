package hei.school.ingredient.service;

import hei.school.ingredient.entity.*;
import hei.school.ingredient.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        return ingredientRepository.findAll();
    }

    public Optional<Ingredient> getIngredientById(Integer id) throws SQLException {
        return ingredientRepository.findById(id);
    }

    public StockValue getStockValueAt(Integer ingredientId, Instant at, Unit unit) throws SQLException {
        Optional<Ingredient> ingredientOpt = ingredientRepository.findById(ingredientId);
        if (ingredientOpt.isEmpty()) {
            return null;
        }

        Ingredient ingredient = ingredientOpt.get();
        if (ingredient.getStockMovementList() == null || ingredient.getStockMovementList().isEmpty()) {
            StockValue sv = new StockValue();
            sv.setQuantity(0.0);
            sv.setUnit(unit);
            return sv;
        }

        // Filter by unit and calculate stock at given time
        List<StockMovement> filteredMovements = ingredient.getStockMovementList().stream()
                .filter(sm -> sm.getValue().getUnit() == unit)
                .filter(sm -> !sm.getCreationDatetime().isAfter(at))
                .toList();

        double sum = 0;
        for (StockMovement sm : filteredMovements) {
            if (sm.getType() == MovementTypeEnum.IN) {
                sum += sm.getValue().getQuantity();
            } else {
                sum -= sm.getValue().getQuantity();
            }
        }

        StockValue result = new StockValue();
        result.setQuantity(sum);
        result.setUnit(unit);
        return result;
    }
}