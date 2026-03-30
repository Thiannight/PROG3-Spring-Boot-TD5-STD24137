package hei.school.ingredient.service;

import hei.school.ingredient.entity.*;
import hei.school.ingredient.repository.DishRepository;
import hei.school.ingredient.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<Dish> getDishById(Integer id) throws SQLException {
        return dishRepository.findById(id);
    }

    public Dish updateDishIngredients(Integer dishId, List<DishIngredient> newIngredients) throws SQLException {
        Optional<Dish> dishOpt = dishRepository.findById(dishId);
        if (dishOpt.isEmpty()) {
            return null;
        }

        Dish dish = dishOpt.get();
        List<DishIngredient> validIngredients = new ArrayList<>();

        for (DishIngredient di : newIngredients) {
            Optional<Ingredient> ingredientOpt = ingredientRepository.findById(di.getIngredient().getId());
            if (ingredientOpt.isPresent()) {
                di.setIngredient(ingredientOpt.get());
                di.setDish(dish);
                validIngredients.add(di);
            }
        }

        dish.setDishIngredients(validIngredients);
        return dishRepository.save(dish);
    }
}