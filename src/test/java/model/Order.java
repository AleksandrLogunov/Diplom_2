package model;

import java.util.Collections;
import java.util.List;

public class Order {
    private List<String> ingredients;

    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public static Order empty() {
        return new Order(Collections.emptyList());
    }

    public static Order withInvalidIngredient() {
        return new Order(Collections.singletonList("invalid_ingredient_hash_12345"));
    }

    public static Order withValidIngredients(List<String> ingredientIds) {
        return new Order(ingredientIds);
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}