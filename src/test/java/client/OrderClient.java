package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.Order;
import java.util.List;
import java.util.Random;
import static io.restassured.RestAssured.given;

public class OrderClient {

    private final String ORDER_PATH = "/api/orders";
    private final String INGREDIENTS_PATH = "/api/ingredients";

    @Step("Создание заказа с авторизацией")
    public Response createOrderAuthorized(Order order, String token) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .body(order)
                .post(ORDER_PATH);
    }

    @Step("Создание заказа без авторизации")
    public Response createOrderUnauthorized(Order order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .post(ORDER_PATH);
    }

    @Step("Получение заказов авторизованного пользователя")
    public Response getOrdersAuthorized(String token) {
        return given()
                .header("Authorization", token)
                .get(ORDER_PATH);
    }

    @Step("Получение заказов неавторизованного пользователя")
    public Response getOrdersUnauthorized() {
        return given()
                .get(ORDER_PATH);
    }

    @Step("Получение списка всех ингредиентов")
    public Response getAllIngredients() {
        return given()
                .get(INGREDIENTS_PATH);
    }

    @Step("Получение ID случайной булки")
    public String getRandomBunId() {
        List<String> bunIds = getAllIngredients()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList("data.findAll { it.type == 'bun' }._id");

        if (bunIds.isEmpty()) {
            throw new AssertionError("Не удалось получить ID булки.");
        }
        return bunIds.get(new Random().nextInt(bunIds.size()));
    }

    @Step("Получение ID случайного не-булочного ингредиента")
    public String getRandomNonBunIngredientId() {
        List<String> nonBunIds = getAllIngredients()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList("data.findAll { it.type != 'bun' }._id");

        if (nonBunIds.isEmpty()) {
            throw new AssertionError("Не удалось получить ID не-булочного ингредиента.");
        }
        return nonBunIds.get(new Random().nextInt(nonBunIds.size()));
    }
}