package hei.school.ingredient.controller;

import hei.school.ingredient.entity.*;
import hei.school.ingredient.service.IngredientService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<Ingredient> getAllIngredients() throws SQLException {
        return ingredientService.getAllIngredients();
    }

    @GetMapping("/{id}")
    public Ingredient getIngredientById(@PathVariable Integer id) throws SQLException {
        return ingredientService.getIngredientById(id);
    }

    @GetMapping("/{id}/stock")
    public StockValue getStockValueAt(
            @PathVariable Integer id,
            @RequestParam String at,
            @RequestParam String unit) throws SQLException {

        Instant instant = Instant.parse(at);
        Unit unitEnum = Unit.valueOf(unit.toUpperCase());

        return ingredientService.getStockValueAt(id, instant, unitEnum);
    }

    // f) GET /ingredients/{id}/stockMovements?from={from}&to={to}
    @GetMapping("/{id}/stockMovements")
    public List<StockMovement> getStockMovements(
            @PathVariable Integer id,
            @RequestParam String from,
            @RequestParam String to) throws SQLException {

        Instant fromInstant = Instant.parse(from);
        Instant toInstant = Instant.parse(to);

        return ingredientService.getStockMovementsBetween(id, fromInstant, toInstant);
    }

    // g) POST /ingredients/{id}/stockMovements
    @PostMapping("/{id}/stockMovements")
    public List<StockMovement> addStockMovements(
            @PathVariable Integer id,
            @RequestBody List<StockMovementRequest> requests) throws SQLException {

        return ingredientService.addStockMovements(id, requests);
    }
}