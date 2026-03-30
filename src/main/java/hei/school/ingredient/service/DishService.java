package hei.school.ingredient.service;

import hei.school.ingredient.entity.*;
import hei.school.ingredient.exception.BadRequestException;
import hei.school.ingredient.exception.NotFoundException;
import hei.school.ingredient.repository.DishRepository;
import hei.school.ingredient.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DishService {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;

    public DishService(DishRepository dishRepository, IngredientRepository ingredientRepository) {
        this.dishRepository = dishRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public List<Dish> getAllDishes() throws SQLException {
        return dishRepository.findAll();
    }

    public Dish getDishById(Integer id) throws SQLException {
        return dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish.id=" + id + " not found"));
    }

    public Dish updateDishIngredients(Integer dishId, List<DishIngredient> newIngredients) throws SQLException {

        if (newIngredients == null || newIngredients.isEmpty()) {
            throw new BadRequestException("Ingredients list cannot be null or empty");
        }

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish.id=" + dishId + " not found"));

        List<DishIngredient> validIngredients = new ArrayList<>();

        for (DishIngredient di : newIngredients) {

            if (di.getIngredient() == null || di.getIngredient().getId() == null) {
                throw new BadRequestException("Ingredient id is required");
            }

            Ingredient ingredient = ingredientRepository.findById(di.getIngredient().getId())
                    .orElseThrow(() -> new BadRequestException(
                            "Ingredient.id=" + di.getIngredient().getId() + " not found"));

            if (di.getQuantity() == null || di.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be positive");
            }

            if (di.getUnit() == null) {
                throw new BadRequestException("Unit is required");
            }

            di.setIngredient(ingredient);
            di.setDish(dish);
            validIngredients.add(di);
        }

        dish.setDishIngredients(validIngredients);
        return dishRepository.save(dish);
    }
}