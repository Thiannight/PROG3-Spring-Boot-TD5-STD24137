package hei.school.ingredient.controller;

import hei.school.ingredient.entity.*;
import hei.school.ingredient.service.IngredientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public ResponseEntity<List<Ingredient>> getAllIngredients() {
        try {
            List<Ingredient> ingredients = ingredientService.getAllIngredients();
            return ResponseEntity.ok(ingredients);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable Integer id) {
        try {
            return ingredientService.getIngredientById(id)
                    .map(ingredient -> ResponseEntity.ok((Object) ingredient))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Ingredient.id=" + id + " is not found")));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStockValueAt(
            @PathVariable Integer id,
            @RequestParam String at,
            @RequestParam String unit) {

        // Vérification des paramètres obligatoires
        if (at == null || at.isBlank() || unit == null || unit.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Either mandatory query parameter `at` or `unit` is not provided."));
        }

        try {
            Instant instant = Instant.parse(at);
            Unit unitEnum;
            try {
                unitEnum = Unit.valueOf(unit.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid unit. Must be one of: PCS, KG, L"));
            }

            StockValue stockValue = ingredientService.getStockValueAt(id, instant, unitEnum);

            if (stockValue == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ingredient.id=" + id + " is not found"));
            }

            return ResponseEntity.ok(stockValue);
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid date format. Use ISO-8601 format (e.g., 2024-01-05T08:00:00Z)"));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error: " + e.getMessage()));
        }
    }
}