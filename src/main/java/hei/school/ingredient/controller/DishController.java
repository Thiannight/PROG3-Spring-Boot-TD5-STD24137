package hei.school.ingredient.controller;

import hei.school.ingredient.entity.*;
import hei.school.ingredient.service.DishService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    public List<Dish> getAllDishes() throws SQLException {
        return dishService.getAllDishes();
    }

    @GetMapping("/{id}")
    public Dish getDishById(@PathVariable Integer id) throws SQLException {
        return dishService.getDishById(id);
    }

    @PutMapping("/{id}/ingredients")
    public Dish updateDishIngredients(
            @PathVariable Integer id,
            @RequestBody List<DishIngredient> ingredients) throws SQLException {

        return dishService.updateDishIngredients(id, ingredients);
    }
}