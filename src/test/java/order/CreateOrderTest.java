package order;

import client.OrderClient;
import client.UserClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import model.LoginCredentials;
import model.Order;
import model.User;
import org.junit.jupiter.api.*;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.*;

public class CreateOrderTest {

    UserClient userClient;
    OrderClient orderClient;
    User testUser;
    String accessToken;

    private String validBunId;
    private String validNonBunIngredientId;


    @BeforeEach
    @Step("Настройка перед тестом: инициализация клиентов, получение ID ингредиентов, создание и логин пользователя")
    void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        userClient = new UserClient();
        orderClient = new OrderClient();

        validBunId = orderClient.getRandomBunId();
        validNonBunIngredientId = orderClient.getRandomNonBunIngredientId();

        testUser = User.getRandomUser();

        userClient.createUser(testUser).then().statusCode(200);
        LoginCredentials creds = new LoginCredentials(testUser.getEmail(), testUser.getPassword());
        accessToken = userClient.loginUser(creds).then().extract().path("accessToken");
    }

    @AfterEach
    @Step("Удаление тестового пользователя после теста")
    void tearDown() {
        userClient.deleteUser(testUser);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и валидными ингредиентами")
    void createOrderWithAuthAndValidIngredients() {
        List<String> ingredients = Arrays.asList(validBunId, validNonBunIngredientId);
        Order order = Order.withValidIngredients(ingredients);

        orderClient.createOrderAuthorized(order, accessToken)
                .then().statusCode(200)
                .body("success", is(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации и с валидными ингредиентами")
    void createOrderWithoutAuthAndValidIngredients() {
        List<String> ingredients = Arrays.asList(validBunId, validNonBunIngredientId);
        Order order = Order.withValidIngredients(ingredients);

        orderClient.createOrderUnauthorized(order)
                .then().statusCode(200)
                .body("success", is(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией, но с невалидными ингредиентами")
    void createOrderWithAuthAndInvalidIngredients() {
        Order order = Order.withInvalidIngredient();

        orderClient.createOrderAuthorized(order, accessToken)
                .then().statusCode(500);
    }

    @Test
    @DisplayName("Создание заказа без авторизации и с невалидными ингредиентами")
    void createOrderWithoutAuthAndInvalidIngredients() {
        Order order = Order.withInvalidIngredient();

        orderClient.createOrderUnauthorized(order)
                .then().statusCode(500);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией, но без ингредиентов")
    void createOrderWithAuthAndNoIngredients() {
        Order order = Order.empty();

        orderClient.createOrderAuthorized(order, accessToken)
                .then().statusCode(400)
                .body("success", is(false))
                .body("message", is("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа без авторизации и без ингредиентов")
    void createOrderWithoutAuthAndNoIngredients() {
        Order order = Order.empty();

        orderClient.createOrderUnauthorized(order)
                .then().statusCode(400)
                .body("success", is(false))
                .body("message", is("Ingredient ids must be provided"));
    }
}